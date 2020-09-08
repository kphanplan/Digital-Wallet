package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Accounts {

	private int accountID;
	private BigDecimal accountBalance;
	private int userID;
	
	
	public Accounts(int userID, int accountID, BigDecimal accountBalance) {
		this.accountID = accountID;
		this.accountBalance = accountBalance;
	}
	
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public Accounts() {
		
	}
	public int getAccountID() {
		return accountID;
	}
	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}
	public BigDecimal getAccountBalance() {
		return accountBalance;
	}
	public void setAccountBalance(BigDecimal accountBalance) {
		this.accountBalance = accountBalance;
	}


	@Override
	public String toString() {
		return "Accounts [accountID=" + accountID + ", accountBalance=" + accountBalance + "]";
	}
	
	
	
}
