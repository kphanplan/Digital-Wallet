package com.techelevator.tenmo.services;

import java.math.BigDecimal;

import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.techelevator.tenmo.models.Accounts;
import com.techelevator.tenmo.models.Transfers;

public class AccountsServices {

	public static String AUTH_TOKEN = "";
	private String BASE_URL;
	private RestTemplate restTemplate = new RestTemplate();

	public AccountsServices(String url) {
		this.BASE_URL = url;
	}



	public BigDecimal getBalanceByAccountID(int id) throws AccountsServicesException {
		BigDecimal bd = null;
		try {
			bd = restTemplate.exchange(BASE_URL + "/accounts/" + id, HttpMethod.GET, makeAuthEntity(), BigDecimal.class)
					.getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountsServicesException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return bd;
	}

	public Transfers[] getTransfersByID(int id) {
		Transfers[] result = null;
		try {
			result = restTemplate.getForObject(BASE_URL + "/accounts/" + id + "/transfers", Transfers[].class);
		} catch (Exception e) {
			System.out.println("ISSUE IN ACCOUNTSERVICES");
		}
		return result;
	}

	public Transfers[] getPendingRequestsByID(int id) {
		Transfers[] result = null;
		try {
			result = restTemplate.getForObject(BASE_URL + "/accounts/" + id + "/requests", Transfers[].class);
		} catch (Exception e) {
			System.out.println("ISSUE IN ACCOUNTSERVICES");
		}
		return result;
	} 

	public void sendMoney(int id, Transfers transfer) {
		restTemplate.exchange(BASE_URL + "/transfers", HttpMethod.POST, makeTransferEntity(transfer), Transfers.class);
	}
	
	
	public void updateTransfer(Transfers transfer) {
		
		restTemplate.exchange(BASE_URL + "/transfers", HttpMethod.PUT, makeTransferEntity(transfer),Transfers.class);
		//erroring out.  Do we need to include a transfer ojbect in the arguments so we can call makeTransferEntity
	}
	
	//

	//

	private HttpEntity<Transfers> makeTransferEntity(Transfers transfer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(AUTH_TOKEN);
		HttpEntity<Transfers> entity = new HttpEntity<>(transfer, headers);
		return entity;
	}

	private HttpEntity makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(AUTH_TOKEN);
		HttpEntity entity = new HttpEntity<>(headers);
		return entity;
	}
}
