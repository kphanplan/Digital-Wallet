package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfers;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AccountsServices;
import com.techelevator.tenmo.services.AccountsServicesException;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.UserServices;
import com.techelevator.view.ConsoleService;

public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };

	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	private AccountsServices accountServices;
	private UserServices userServices;

	//

	private int userID;
	Scanner scan = new Scanner(System.in);

	public static void main(String[] args) {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),
				new AccountsServices(API_BASE_URL), new UserServices(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService, AccountsServices accountServices,
			UserServices userServices) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.accountServices = accountServices;
		this.userServices = userServices;
		
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while (true) {
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		try {
			System.out.println("Current balance: $" + accountServices.getBalanceByAccountID(userID));
		} catch (AccountsServicesException e) {
			e.printStackTrace();
		}
	}

	// Cheap way to make values human-readable

	List<User> userList = new ArrayList<User>();

	Map<Integer, String> typeMap = new HashMap<Integer, String>() {
		{
			put(1, "Request");
			put(2, "Send");
		}
	};

	Map<Integer, String> statusMap = new HashMap<Integer, String>() {
		{
			put(1, "Pending");
			put(2, "Approved");
			put(3, "Declined");
		}
	};

	private void viewTransferHistory() {
		int userid = userID;
		try {
			System.out.println("ID\tType\tStatus   \tFrom\tTo\tAmount");
			for (Transfers holder : accountServices.getTransfersByID(userid)) {
				System.out.println(holder.getTransferId() + "\t" + typeMap.get(holder.getTransferTypeId()) + "\t"
						+ statusMap.get(holder.getTransferStatusId()) + "   \t"
						+ userList.get(holder.getAccountFrom()-1).getUsername() + "\t"
						+ userList.get(holder.getAccountTo()-1).getUsername() + "\t" + "$" + holder.getAmount());
			}
		} catch (Exception e) {
			System.out.println("!!!ERROR IN APP!!!");
		}
	}

	private void viewPendingRequests() {
		int userid = userID;
		List<Transfers> transfersList = new ArrayList<Transfers>();
		System.out.println("ID \t Type \t Status \t From \t To \t Amount");
		for (Transfers holder : accountServices.getPendingRequestsByID(userid)) {
			System.out.println(holder.getTransferId() + "\t" + typeMap.get(holder.getTransferTypeId()) + "\t"
					+ statusMap.get(holder.getTransferStatusId()) + "\t\t"
					+ userList.get(holder.getAccountFrom()-1).getUsername() + "\t"
					+ userList.get(holder.getAccountTo()-1).getUsername() + "\t" + "$" + holder.getAmount());
			transfersList.add(holder);
		}

		int transferIDSelection = 0;
		Transfers holder = null;

		System.out.println("\nSelect transfer by ID: ");
		try {
			transferIDSelection = Integer.parseInt(scan.nextLine());
			for (Transfers transfer : transfersList) {
				if (transfer.getTransferId() == transferIDSelection) {
					holder = transfer;
				}
			}
		} catch (Exception e) {
			System.out.println("Invalid transferIDSelection");
		}

		System.out.println("Enter 1 to Approve Request \n" + "Enter 2 to Decline Request");
		int decision = Integer.parseInt(scan.nextLine());
		try {
			if (decision == 1) {
				holder.setTransferStatusId(2);
				accountServices.updateTransfer(holder);
			} else if (decision == 2) {
				holder.setTransferStatusId(3);
				System.out.println("Denied");
				accountServices.updateTransfer(holder);
			}
		} catch (NullPointerException e) {
			System.out.println("No pending request.");

		}

	}

	// ISSUE: TRANSFERS ARE NOT BEING RECORDED INTO SQL
	private void sendBucks() {
		int userid = userID;

		for (int i = 0; i < userServices.getAllUsers().length; i++) {
			System.out.println(
					"User: " + userList.get(i).getId() + "\t" + "username: " + userList.get(i).getUsername() + "\n");
		}

		System.out.println("Select and ID you want to send TE Bucks to: \n");

		int sendTo = Integer.parseInt(scan.nextLine());

		System.out.println("How much money?");
		BigDecimal money = (new BigDecimal(scan.nextLine()));

		try {
			if (accountServices.getBalanceByAccountID(userid).compareTo(money) < 0) {
				System.out.println("You do not have enough funds to make transfer");
				sendBucks();

			}
		} catch (AccountsServicesException e1) {
			e1.printStackTrace();
		}

		Transfers holder = sendTransfer(userid, sendTo, money);
		accountServices.sendMoney(userid, holder);

		try {
			System.out.println("$" + money + " sent to " + sendTo);
			System.out.println("New balance: $" + accountServices.getBalanceByAccountID(userid));
		} catch (AccountsServicesException e) {
			e.printStackTrace();
		}
	}

	public void userListStart() {
		// GM: created list to loop through to prit our user id and username
		//userList = null;
		for (int i = 0; i < userServices.getAllUsers().length; i++) {
			userList.add(userServices.getAllUsers()[i]);
		}
	}

	private void requestBucks() {
		int userid = userID;

		System.out.println("Select an ID you want to request TE Bucks from: \n");

		List<User> userList = new ArrayList<User>();

		for (int i = 0; i < userServices.getAllUsers().length; i++) {
			userList.add(userServices.getAllUsers()[i]);

			System.out.println(
					"User: " + userList.get(i).getId() + "\t" + "username: " + userList.get(i).getUsername() + "\n");
		}

		int getFrom = Integer.parseInt(scan.nextLine());

		System.out.println("How much money? ");
		try {
			BigDecimal money = (new BigDecimal(scan.nextLine()));
			Transfers transfer = requestTransfer(userid, getFrom, money);
			accountServices.sendMoney(userid, transfer);
		} catch (NumberFormatException e) {
			System.out.println("Invalid number format.");
		}
	}

	public Transfers requestTransfer(int userid2, int getFrom, BigDecimal money) {
		Transfers transfer = new Transfers();

		transfer.setTransferTypeId(1);
		transfer.setTransferStatusId(1);
		transfer.setAccountFrom(getFrom);
		transfer.setAccountTo(userid2);
		transfer.setAmount(money);

		return transfer;

	}

	public Transfers sendTransfer(int userid, int sendTo, BigDecimal money) {
		Transfers holder = new Transfers();
		// type = 2 === "Send"
		// status 2 === "Approved"
		holder.setTransferTypeId(2);
		holder.setTransferStatusId(2);
		holder.setAccountFrom(userid);
		holder.setAccountTo(sendTo);
		holder.setAmount(money);

		return holder;
	}

	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while (!isAuthenticated()) {
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
		while (!isRegistered) // will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("Registration successful. You can now login.");
			} catch (AuthenticationServiceException e) {
				System.out.println("REGISTRATION ERROR: " + e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login() {
		userListStart();
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) // will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				currentUser = authenticationService.login(credentials);
				userID = currentUser.getUser().getId();
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}

	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
