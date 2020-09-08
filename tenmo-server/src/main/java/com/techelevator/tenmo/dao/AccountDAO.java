package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Accounts;


import com.techelevator.tenmo.model.Transfers;


import com.techelevator.tenmo.model.OverdraftException;

@Component
public interface AccountDAO {

	public BigDecimal getBalanceByAccountID(int id);
	
	public Accounts getAccountByUserID(int userID);
	
	public List<Accounts> getAllAccounts();

	void sendMoney(Transfers transfer);
	
	public void getNewAccountBalance(int accountFrom, BigDecimal amount) throws OverdraftException;
	
	//public Accounts create();
}
