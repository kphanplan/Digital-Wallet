package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfers;

@Component
public class TransfersSqlDAO implements TransfersDAO {

	JdbcTemplate template;

	public TransfersSqlDAO(DataSource datasource) {
		this.template = new JdbcTemplate(datasource);
	}

	@Override
	public List<Transfers> getAllTransfersByAccountId(int id) {
		String sql = "Select accounts.account_id, transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount from transfers JOIN accounts ON accounts.account_id = transfers.account_from "
				+ "WHERE transfers.account_to = ? OR transfers.account_from = ?";
		SqlRowSet results = template.queryForRowSet(sql, id, id);
		List<Transfers> output = new ArrayList<>();

		while (results.next()) {
			Transfers transfer = mapToRowTransfers(results);
			output.add(transfer);
		}
		return output;
	}

	@Override
	public List<Transfers> getPendingRequestsById(int id) {
		String sql = "Select accounts.account_id, transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount from transfers JOIN accounts ON accounts.account_id = transfers.account_from "
				+ "WHERE accounts.account_id = ? AND transfer_status_id = 1 AND transfer_type_id = 1";
		// transfer_status_id = 1 === "Pending"
		// transfer_type_id = 1 === "Request"
		SqlRowSet result = template.queryForRowSet(sql, id);
		List<Transfers> list = new ArrayList<>();
		while (result.next()) {
			Transfers transfer = mapToRowTransfers(result);
			list.add(transfer);
		}
		return list;
	}

	public Transfers mapToRowTransfers(SqlRowSet results) {
		Transfers transfers = new Transfers(0, 0, 0, 0, 0, null);

		transfers.setTransferId(results.getInt("transfer_id"));
		transfers.setTransferTypeId(results.getInt("transfer_type_id"));
		transfers.setTransferStatusId(results.getInt("transfer_status_id"));
		transfers.setAccountFrom(results.getInt("account_from"));
		transfers.setAccountTo(results.getInt("account_to"));
		transfers.setAmount(results.getBigDecimal("amount"));

		return transfers;
	}

	@Override
	public void createTransfer(Transfers transfer) {
		String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?,?,?,?,?)";
		template.update(sql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(),
				transfer.getAccountTo(), transfer.getAmount());
	}

	@Override
	public void updateTransfer(Transfers transfer) {
		String sql = "UPDATE transfers "
				+ "SET transfer_status_id = ? "
				+ "WHERE transfer_id = ?";
		template.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
	}
	
	
		
	
}
