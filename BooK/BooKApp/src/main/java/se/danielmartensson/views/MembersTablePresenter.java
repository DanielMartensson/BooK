package se.danielmartensson.views;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cz.msebera.android.httpclient.HttpStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import se.danielmartensson.tools.http.HTTPClient;
import se.danielmartensson.tools.http.HTTPMessage;
import se.danielmartensson.tools.popup.Dialogs;
import se.danielmartensson.views.containers.UserTable;
import se.danielmartensson.views.entity.Company;
import se.danielmartensson.views.entity.Role;
import se.danielmartensson.views.entity.User;

public class MembersTablePresenter {

    @FXML
    private View view;

    @FXML
    private TableView<UserTable> users;

    @FXML
    private TableColumn<UserTable, String> nameColumn;

    @FXML
    private TableColumn<UserTable, String> companyColumn;
    
    @FXML
    private TableColumn<UserTable, String> emailColumn;
    
    private ObservableList<UserTable> tablewViewListener;
    
    @Inject
    private HTTPClient hTTPClient;
    
    @Inject
    private Dialogs dialogs;
	

	/**
	 * Initial start up
	 */
	@FXML
	public void initialize() {
		// Slide in smooth
		view.setShowTransitionFactory(BounceInRightTransition::new);

		// Listener for when we slide in or leave page
		view.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				// When we slide in
				AppBar appBar = MobileApplication.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().getDrawer().open()));
				appBar.setTitleText("Medlemmar");
				appBar.getActionItems().add(MaterialDesignIcon.EDIT.button(e -> editMember()));
				appBar.getActionItems().add(MaterialDesignIcon.DELETE.button(e -> deleteMember()));
				
				// Update the table
				updateTable();

			} else {
				// When we leave this page 
			}
		});
		
		// Init the table first
		nameColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("username"));
		companyColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("company"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("email"));
		tablewViewListener = FXCollections.observableArrayList();
		users.setItems(tablewViewListener);
		
	}

	/**
	 * Ask if we should delete member?
	 */
	private void deleteMember() {
		// Get selected email
		UserTable userTable = users.getSelectionModel().getSelectedItem();
		if(userTable == null)
			return;
		String selectedEmail = userTable.getEmail();
				
		HTTPMessage hTTPMessage = hTTPClient.sendPost("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/admin/deleteuser?username=" + selectedEmail);
		if(hTTPMessage.getConnectionStatus() == HttpStatus.SC_FORBIDDEN) {
			dialogs.alertDialog(AlertType.WARNING, "Not ADMIN", "You are not authorized for this");
			return;
		}
		if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
			dialogs.alertDialog(AlertType.INFORMATION, "Deleted", "User: " + selectedEmail + " deleted");
		}else {
			dialogs.alertDialog(AlertType.ERROR, "Member", hTTPMessage.getMessage());	
		}
		
		updateTable();
	}

	/**
	 * Ask what we should do to the member
	 */
	private void editMember() {
		
		// Get selected email
		UserTable userTable = users.getSelectionModel().getSelectedItem();
		if(userTable == null)
			return;
		String selectedEmail = userTable.getEmail();
		
		// Get the user object
		HTTPMessage hTTPMessage = hTTPClient.sendGet("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/admin/getuser?username=" + selectedEmail);
		if(hTTPMessage.getConnectionStatus() == HttpStatus.SC_FORBIDDEN) {
			dialogs.alertDialog(AlertType.WARNING, "Not ADMIN", "You are not authorized for this");
			return;
		}
		if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
			Type type = new TypeToken<User>() {}.getType();
			User user = new Gson().fromJson(hTTPMessage.getMessage(), type); // message contains json of user object

			// Select choise
			String theChoise = dialogs.choice();
			if(theChoise.equals("") == true)
				return;
			
			boolean updatePassword = false;
			if(theChoise.equals("Email") == true) {
				String newEmail = dialogs.input("Enter new email", "your email..."); // Actully username
				user.setEmail(newEmail);
				user.setUsername(newEmail);
				HTTPClient.USERNAME = newEmail;
				
			}else if(theChoise.equals("Password") == true) {
				String newPassoword = dialogs.password();
				user.setPassword(newPassoword);
				updatePassword = true;
				
			}else if(theChoise.equals("Role") == true) {
				String newRole = dialogs.input("Enter new role", "ADMIN or USER");
				if(newRole.equals("ADMIN") == false && newRole.equals("USER") == false)
					return;
				// Set it now
				System.out.println("New role = " + newRole);
				Set<Role> roles = user.getRoles();
				for(Role role : roles)
					role.setRole(newRole);
			}
			
			// OK! Send back the object user
			hTTPMessage = hTTPClient.postObject("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/admin/updateuser?updatepassword=" + updatePassword, user);
			
		}else {
			dialogs.alertDialog(AlertType.ERROR, "Member", hTTPMessage.getMessage());
			return;
		}
		
		
		// Check message
		if(hTTPMessage.getConnectionStatus() == HttpStatus.SC_OK) {
			if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
				dialogs.alertDialog(AlertType.INFORMATION, "Success", hTTPMessage.getMessage());
				updateTable();
			}else {
				dialogs.alertDialog(AlertType.WARNING, "Not success", hTTPMessage.getMessage());
			}
		}else {
			dialogs.alertDialog(AlertType.WARNING, "Not connected", "Missing connection to database");
		}
		
	}

	/**
	 * This method calls the user data table and update it the table in JavaFX
	 */
	private void updateTable() {
		// First get the database data
		HTTPMessage hTTPMessage = hTTPClient.sendGet("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/user/searchcompanyusers");
		if(hTTPMessage == null) {
			dialogs.alertDialog(AlertType.ERROR, "No connection", "Cannot acces the database");
    		MobileApplication.getInstance().switchView(MobileApplication.HOME_VIEW);
    	}else {
    		if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
    			// Good - Get the company table now from the message, which contains json table
    			Type type = new TypeToken<List<Company>>() {}.getType();
    			List<Company> listOfCompaniesUsers = new Gson().fromJson(hTTPMessage.getMessage(), type);
    			tablewViewListener.clear();
    			for(Company company : listOfCompaniesUsers) {
    				tablewViewListener.add(new UserTable(company.getUsername(), company.getCompany(), company.getEmail()));
    			}
    		}else {
        		dialogs.alertDialog(AlertType.WARNING, "Connected", "But not database avaiable: " + hTTPMessage.getMessageStatusCode());
    		}
    	}
	}

}
