package se.danielmartensson.views;

import java.lang.reflect.Type;
import java.util.List;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import se.danielmartensson.book.Main;
import se.danielmartensson.tools.http.HTTPClient;
import se.danielmartensson.tools.http.HTTPMessage;
import se.danielmartensson.tools.popup.Dialogs;
import se.danielmartensson.views.containers.UserTable;
import se.danielmartensson.views.entity.Company;
import se.danielmartensson.views.globals.SelectedCompanyUsers;

public class UserTablePresenter {

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
    
    @Inject
    private SelectedCompanyUsers selectedCompanyUsers;

	

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
				appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> MobileApplication.getInstance().switchView(Main.CONFERANCE_VIEW)));
				appBar.setTitleText("Medlemmar");
				
				// Update the table
				updateTable();
				// Update selected table
				markSelectedMembers();
			} else {
				// When we leave this page - Save the selected members - We are going to send them an email 
				ObservableList<UserTable> userTables = users.getSelectionModel().getSelectedItems();
				if(userTables == null) {
					selectedCompanyUsers.setMembers(""); // No selected
				}else {
					String members = "";
					for(UserTable userTable : userTables) {
						members += userTable.getEmail() + ";"; // Load the selected
					}
					if(members.length() > 0)
						members = members.substring(0, members.length() - 1); // Remove the last ";"
					selectedCompanyUsers.setMembers(members);
				}
			}
		});
		
		// Init the table first
		nameColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("username"));
		companyColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("company"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<UserTable, String>("email"));
		tablewViewListener = FXCollections.observableArrayList();
		users.setItems(tablewViewListener);
		
		// Add functionalty to multiple selection
		users.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // We can select multiple rows
		
		// This code will handle multiple selection events. Press on a selected -> unselect
		users.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
		    Node node = evt.getPickResult().getIntersectedNode();

		    // go up from the target node until a row is found or it's clear the
		    // target node wasn't a node.
		    while (node != null && node != users && !(node instanceof TableRow)) {
		        node = node.getParent();
		    }

		    // if is part of a row or the row,
		    // handle event instead of using standard handling
		    if (node instanceof TableRow) {
		        // prevent further handling
		        evt.consume();

		        @SuppressWarnings("unchecked")
				TableRow<UserTable> row = (TableRow<UserTable>) node;
		        TableView<UserTable> tv = row.getTableView();

		        // focus the tableview
		        tv.requestFocus();

		        if (!row.isEmpty()) {
		            // handle selection for non-empty nodes
		            int index = row.getIndex();
		            if (row.isSelected()) {
		                tv.getSelectionModel().clearSelection(index);
		            } else {
		                tv.getSelectionModel().select(index);
		            }
		        }
		    }
		});
	}
	

	/**
	 * This function loops thru selectedCompanyUsers and select member if it's exist
	 */
	private void markSelectedMembers() {
		String[] members = selectedCompanyUsers.getMembers().split(";");
		if(members == null) {
			return; // No members selected at the meeting
		}
		
		for(int i = 0; i < tablewViewListener.size(); i++) {
			for(String member : members) {
				String selectedMember = tablewViewListener.get(i).getEmail();
				if(member.equals(selectedMember) == true) {
					users.getSelectionModel().select(i); // Select our 
				}
			}
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
