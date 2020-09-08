package com.techelevator.tenmo.model;

public class TransferTypes {
	
	private Long transferTypeId;
	private String transferTypeDesc;
	
	public TransferTypes() { }
	
	public TransferTypes(Long transferTypeId, String transferTypeDesc) {
		super();
		this.transferTypeId = transferTypeId;
		this.transferTypeDesc = transferTypeDesc;
	}
	
	public Long getTransfer_type_id() {
		return transferTypeId;
	}
	public String getTransfer_type_desc() {
		return transferTypeDesc;
	}
	public void setTransfer_type_id(Long transfer_type_id) {
		this.transferTypeId = transfer_type_id;
	}
	public void setTransfer_type_desc(String transfer_type_desc) {
		this.transferTypeDesc = transfer_type_desc;
	}
}
