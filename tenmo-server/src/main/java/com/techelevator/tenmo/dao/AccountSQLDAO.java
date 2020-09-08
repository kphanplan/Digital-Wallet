package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Accounts;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.OverdraftException;

@Component
public class AccountSQLDAO implements AccountDAO {

	JdbcTemplate template;

	public AccountSQLDAO(DataSource datasource) {
		this.template = new JdbcTemplate(datasource);
	}

	@Override
	public BigDecimal getBalanceByAccountID(int id) {

		// BigDecimal balance = new BigDecimal(0.00);
		Accounts account = null;
		String sqlInsert = "SELECT * from accounts where account_id = ?";

		SqlRowSet results = template.queryForRowSet(sqlInsert, id);

		while (results.next()) {
			account = mapToRowAccount(results);
			// balance.add(new BigDecimal(account.getAccountBalance()));
		}
		BigDecimal bd = account.getAccountBalance();
		return bd;
	}

	@Override
	public Accounts getAccountByUserID(int userID) {
		Accounts account = new Accounts();
		String sqlInsert = "Select * from accounts where user_id = ?";
		SqlRowSet results = template.queryForRowSet(sqlInsert, userID);

		if (results.next()) {
			account = mapToRowAccount(results);
		}

		return account;
	}

	@Override
	public void sendMoney(Transfers transfer) {
		BigDecimal money = transfer.getAmount();
		int sendTo = transfer.getAccountTo();
		int takeFrom = transfer.getAccountFrom();
		
		String sql = "UPDATE accounts " + "SET balance = (balance + ?) " + "WHERE account_id = ?";
		template.update(sql, money, sendTo);
		sql = "UPDATE accounts SET balance = (balance - ?) WHERE account_id = ? AND ((balance - ?) > 0)";
		template.update(sql, money, takeFrom, money);

	}

	@Override
	public List<Accounts> getAllAccounts() {
		Accounts account;
		List<Accounts> allAccounts = new ArrayList<Accounts>();
		String sqlInsert = "SELECT * from accounts";
		SqlRowSet results = template.queryForRowSet(sqlInsert);

		while (results.next()) {
			account = mapToRowAccount(results);
			allAccounts.add(account);
		}

		return allAccounts;
	}


	public Accounts mapToRowAccount(SqlRowSet results) {
		Accounts account;
		account = new Accounts();
		account.setAccountID(results.getInt("account_id"));
		account.setUserID(results.getInt("user_id"));
		account.setAccountBalance(results.getBigDecimal("balance"));

		return account;
	}

	@Override
	public void getNewAccountBalance(int accountFrom, BigDecimal amount) throws OverdraftException {
		String sql = "UPDATE accounts SET balance = ? "
				+ "WHERE account_id = ?";
		Accounts account = getAccountByUserID(accountFrom);
		if(amount.compareTo(new BigDecimal(0.00)) < 0) {
			if(account.getAccountBalance().compareTo(amount.negate()) < 0) {
				throw new OverdraftException();
			}
		}
		account.setAccountBalance(account.getAccountBalance().add(amount));
		template.update(sql, account.getAccountBalance(), account.getAccountID());
	}


}
