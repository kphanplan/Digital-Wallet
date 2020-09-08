package com.techelevator.tenmo.model;

public class TransferStatuses {
	
	private int transferStatusesId;
	private String transferStatusDescription;

	public TransferStatuses(int transferStatusesId, String transferStatusDescription) {
		this.transferStatusesId = transferStatusesId;
		this.transferStatusDescription = transferStatusDescription;
	}

	public int getTransferStatusesId() {
		return transferStatusesId;
	}

	public void setTransferStatusesId(int transferStatusesId) {
		this.transferStatusesId = transferStatusesId;
	}

	public String getTransferStatusDescription() {
		return transferStatusDescription;
	}

	public void setTransferStatusDescription(String transferStatusDescription) {
		this.transferStatusDescription = transferStatusDescription;
	}
	
	

}
