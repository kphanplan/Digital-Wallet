package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transfers;

public interface TransfersDAO {
	
	public void createTransfer(Transfers transfer);
	public List<Transfers> getAllTransfersByAccountId(int id);
	public List<Transfers> getPendingRequestsById(int id);
	public void updateTransfer(Transfers transfer);
}
