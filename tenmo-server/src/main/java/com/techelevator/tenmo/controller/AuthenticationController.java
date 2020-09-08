package com.techelevator.tenmo.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.TransfersDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Accounts;
import com.techelevator.tenmo.model.LoginDTO;
import com.techelevator.tenmo.model.OverdraftException;
import com.techelevator.tenmo.model.RegisterUserDTO;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserAlreadyExistsException;
import com.techelevator.tenmo.security.jwt.JWTFilter;
import com.techelevator.tenmo.security.jwt.TokenProvider;

/**
 * Controller to authenticate users.
 */
@RestController
public class AuthenticationController {

	@Autowired
	AccountDAO accountsDAO;
	@Autowired
	TransfersDAO transfersDAO;

	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private UserDAO userDAO;

	public AuthenticationController(TokenProvider tokenProvider,
			AuthenticationManagerBuilder authenticationManagerBuilder, UserDAO userDAO) {
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
		this.userDAO = userDAO;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDTO loginDto) {

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginDto.getUsername(), loginDto.getPassword());

		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.createToken(authentication, false);

		User user = userDAO.findByUsername(loginDto.getUsername());

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
		return new ResponseEntity<>(new LoginResponse(jwt, user), httpHeaders, HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public void register(@Valid @RequestBody RegisterUserDTO newUser) {
		try {
			User user = userDAO.findByUsername(newUser.getUsername());
			throw new UserAlreadyExistsException();
		} catch (UsernameNotFoundException e) {
			userDAO.create(newUser.getUsername(), newUser.getPassword());
		}
	}

	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public List<User> findAll() {
		List<User> output = userDAO.findAll();
		return output;
	}

	@RequestMapping(path = "/accounts", method = RequestMethod.GET)
	public List<Accounts> findAllAccounts() {
		List<Accounts> accountList = accountsDAO.getAllAccounts();

		return accountList;
	}

	@RequestMapping(path = "/accounts/{id}", method = RequestMethod.GET)
	public BigDecimal getAccountBalanceById(@PathVariable int id) {
		BigDecimal bd = accountsDAO.getBalanceByAccountID(id);
		return bd;
	}

	@RequestMapping(path = "/users/{id}/accounts", method = RequestMethod.GET)
	public Accounts getAccountById(@PathVariable int id) {
		Accounts output = accountsDAO.getAccountByUserID(id);
		return output;
	}

	@RequestMapping(path = "/accounts/{id}/transfers", method = RequestMethod.GET)
	public List<Transfers> getAllTransfersByAccountId(@PathVariable int id) {
		List<Transfers> output = transfersDAO.getAllTransfersByAccountId(id);
		return output;
	}

	@RequestMapping(path = "/accounts/{id}/requests", method = RequestMethod.GET)
	public List<Transfers> getReqeustsByAccountId(@PathVariable int id) {
		List<Transfers> output = transfersDAO.getPendingRequestsById(id);
		return output;
	}

	@RequestMapping(path = "/accounts/{id}/sending", method = RequestMethod.PUT)
	public void sendingMoney(@Valid @RequestBody Transfers transfer, @PathVariable int id) {
    	accountsDAO.sendMoney(transfer);
	}

	@RequestMapping(path = "/transfers", method = RequestMethod.POST)
	public void createTransfer(@Valid @RequestBody Transfers transfer) {
		transfersDAO.createTransfer(transfer);
		if(transfer.getTransferStatusId() == 2) {
			try {
				accountsDAO.getNewAccountBalance(transfer.getAccountFrom(), transfer.getAmount().negate());
			} catch (OverdraftException e1) {
				e1.printStackTrace();
			}
			try {
				accountsDAO.getNewAccountBalance(transfer.getAccountTo(), transfer.getAmount());
			} catch (OverdraftException e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(path = "/transfers", method = RequestMethod.PUT)
	public void updateTransfer(@Valid @RequestBody Transfers transfer) {
		transfersDAO.updateTransfer(transfer);
		if(transfer.getTransferStatusId() == 2) {
			try {
				accountsDAO.getNewAccountBalance(transfer.getAccountFrom(), transfer.getAmount().negate());
			} catch (OverdraftException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				accountsDAO.getNewAccountBalance(transfer.getAccountTo(), transfer.getAmount());
			} catch (OverdraftException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	

	/**
	 * Object to return as body in JWT Authentication.
	 */
	static class LoginResponse {

		private String token;
		private User user;

		LoginResponse(String token, User user) {
			this.token = token;
			this.user = user;
		}

		@JsonProperty("token")
		String getToken() {
			return token;
		}

		void setToken(String token) {
			this.token = token;
		}

		@JsonProperty("user")
		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}
	}
}
