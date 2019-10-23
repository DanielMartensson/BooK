package se.danielmartensson.views;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import se.danielmartensson.book.Main;
import se.danielmartensson.tools.http.HTTPClient;
import se.danielmartensson.tools.http.HTTPMessage;
import se.danielmartensson.tools.popup.Dialogs;
import se.danielmartensson.tools.popup.ScrollableButton;
import se.danielmartensson.views.containers.ConferenceBookingsTable;
import se.danielmartensson.views.entity.ConferenceRoom;
import se.danielmartensson.views.globals.SelectedCompanyUsers;

public class ConferenceroomPresenter {
	
	private static String[] times = {
			"00:00", "00:15", "00:30", "00:45",
			"01:00", "01:15", "01:30", "01:45",
			"02:00", "02:15", "02:30", "02:45",
			"03:00", "03:15", "03:30", "03:45",
			"04:00", "04:15", "04:30", "04:45",
			"05:00", "05:15", "05:30", "05:45",
			"06:00", "06:15", "06:30", "06:45",
			"07:00", "07:15", "07:30", "07:45",
			"08:00", "08:15", "08:30", "08:45",
			"09:00", "09:15", "09:30", "09:45",
			"10:00", "10:15", "10:30", "10:45",
			"11:00", "11:15", "11:30", "11:45",
			"12:00", "12:15", "12:30", "12:45",
			"13:00", "13:15", "13:30", "13:45",
			"14:00", "14:15", "14:30", "14:45",
			"15:00", "15:15", "15:30", "15:45",
			"16:00", "16:15", "16:30", "16:45",
			"17:00", "17:15", "17:30", "17:45",
			"18:00", "18:15", "18:30", "18:45",
			"19:00", "19:15", "19:30", "19:45",
			"20:00", "20:15", "20:30", "20:45",
			"21:00", "21:15", "21:30", "21:45",
			"22:00", "22:15", "22:30", "22:45",
			"23:00", "23:15", "23:30", "23:45",
			
			
			                          };

	@FXML
	private View view;

	@FXML
	private DatePicker dateSelect;

	@FXML
	private Button timeFrom;

	@FXML
	private Button timeTo;

	@FXML
	private TableView<ConferenceBookingsTable> conferenseroomTable;

	@FXML
	private TableColumn<ConferenceBookingsTable, String> fromColumn;

	@FXML
	private TableColumn<ConferenceBookingsTable, String> toColumn;

	@FXML
	private TableColumn<ConferenceBookingsTable, String> emailColumn;

	@Inject
	private SelectedCompanyUsers selectedCompanyUsers;

	@Inject
	private HTTPClient hTTPClient;

	@Inject
	private Dialogs dialogs;
	
	@Inject
	private ScrollableButton scrollableButton;

	private ObservableList<ConferenceBookingsTable> conferenseroomTableListener;

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
				appBar.setTitleText("Boka konferensrum");
				appBar.getActionItems().add(MaterialDesignIcon.GROUP.button(e -> MobileApplication.getInstance().switchView(Main.USERTABLE_VIEW)));
				appBar.getActionItems().add(MaterialDesignIcon.DELETE.button(e -> deleteMeeting()));
				appBar.setOpacity(1); // Show AppBar - Because we enter this page as first page

				// Load the date picker
				loadDatePicker();
			} else {
				// When we leave this page
			}
		});

		// Init the table first
		fromColumn.setCellValueFactory(new PropertyValueFactory<ConferenceBookingsTable, String>("from"));
		toColumn.setCellValueFactory(new PropertyValueFactory<ConferenceBookingsTable, String>("to"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<ConferenceBookingsTable, String>("email"));
		conferenseroomTableListener = FXCollections.observableArrayList();
		conferenseroomTable.setItems(conferenseroomTableListener);
		
		// We cannot use Dropdown Buttons in mobile because they isin't scrollable. So we turn button to a scrollable vbox
		scrollableButton.turnToScrollablee(timeFrom, timeTo, times);
		
	}

	/**
	 * This method will delete the selected meeting if it's created by you
	 */
	private void deleteMeeting() {
		
		// Make sure we have select a row
		ConferenceBookingsTable selectedRow = conferenseroomTable.getSelectionModel().getSelectedItem();
		if(selectedRow == null)
			return;
		
		// Ask if you want to book a meeting
		boolean removeMeeting = dialogs.question("Remove", "Do you want to remove this meeting?");
		if(removeMeeting == false)
			return;
		
		// Get the time in unix time stamp
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String from = selectedRow.getFrom();
		String to = selectedRow.getTo();
		try {
			long start = simpleDateFormat.parse(from).getTime();
			long end = simpleDateFormat.parse(to).getTime();
			
			// Create an object that we are going to search for in the database
			ConferenceRoom conferenceRoom = new ConferenceRoom();
			conferenceRoom.setStart(start);
			conferenceRoom.setEnd(end);
			conferenceRoom.setEmail(HTTPClient.USERNAME); // Only what you have created
			HTTPMessage hTTPMessage = hTTPClient.postObject("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/user/removeconferenceroombooking", conferenceRoom);
			if (hTTPMessage == null) {
				dialogs.alertDialog(AlertType.ERROR, "Connection", "No connection to databse");
			} else {
				if (hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
					// Deleted from database. Now remove to our list to
					conferenseroomTableListener.remove(selectedRow);
				} else {
					dialogs.alertDialog(AlertType.WARNING, "Meeting", "Cannot delete another meeting: " + hTTPMessage.getConnectionStatus());
				}
			}
		} catch (ParseException e) {
			dialogs.exception("Cannot delete row(s)", e);
		}

	}

	/**
	 * This will load the schedule of bookings in the conference room
	 */
	private void loadDatePicker() {
		// Call the server and get all meetings in form of a json string
		HTTPMessage hTTPMessage = hTTPClient.sendGet("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/user/getconferenceroombookings");
		if (hTTPMessage == null) {
			dialogs.alertDialog(AlertType.WARNING, "Not connected", "Could not connect to database");
			return;
		}
		String json = hTTPMessage.getMessage();

		// Load the object
		Type type = new TypeToken<List<ConferenceRoom>>() {
		}.getType();
		List<ConferenceRoom> ConferenceRoomBookings = new Gson().fromJson(json, type);

		// Clear the table and create a date formater
		conferenseroomTableListener.clear();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		// Loop all meetings
		for (ConferenceRoom conferenceRoom : ConferenceRoomBookings) {
			// Get the unix time stamps
			long start = conferenceRoom.getStart();
			long end = conferenceRoom.getEnd();
			String email = conferenceRoom.getEmail();

			// Turn them into dates
			Date dateStart = new Date(start);
			Date dateEnd = new Date(end);

			// Add new row
			conferenseroomTableListener.add(new ConferenceBookingsTable(simpleDateFormat.format(dateStart), simpleDateFormat.format(dateEnd), email));
		}

	}

	@FXML
	void save(ActionEvent event) {
		// Get the selected dates
		LocalDate fromDate = dateSelect.getValue();
		if (fromDate == null)
			return;
		
		// Ask if you want to book a meeting
		boolean bookAmeeting = dialogs.question("Booking", "Do you want to book a meeting?");
		if(bookAmeeting == false)
			return;

		// Get the selected time
		String fromTime = timeFrom.getText();
		String toTime = timeTo.getText();
		
		// Check if we have select start = stop as the same time
		if(fromTime.equals(toTime) == true) {
			dialogs.alertDialog(AlertType.WARNING, "Same time", "You cannot have the same time on start and stop");
			return;
		}
		
		// Create date strings at the form yyyy-MM-dd HH:mm
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String startDateString = fromDate.getYear() + "-" + fromDate.getMonthValue() + "-" + fromDate.getDayOfMonth() + " " + fromTime;
		String toDateString = fromDate.getYear() + "-" + fromDate.getMonthValue() + "-" + fromDate.getDayOfMonth() + " " + toTime;

		try {
			// Try to find the unix time stamp
			long start = simpleDateFormat.parse(startDateString).getTime();
			long end = simpleDateFormat.parse(toDateString).getTime();
			ConferenceRoom conferenceRoom = new ConferenceRoom();
			conferenceRoom.setStart(start);
			conferenceRoom.setEnd(end);
			conferenceRoom.setEmail(HTTPClient.USERNAME); // Which will be the email of the user
			conferenceRoom.setMembers(selectedCompanyUsers.getMembers()); // Email addresses of the members

			// Post and check our message
			HTTPMessage hTTPMessage = hTTPClient.postObject("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/user/setconferenceroombooking", conferenceRoom);
			if (hTTPMessage == null) {
				dialogs.alertDialog(AlertType.ERROR, "Connection", "No connection to databse");
			} else {
				if (hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
					// Added to database. Now add to our list to
					conferenseroomTableListener.add(new ConferenceBookingsTable(startDateString, toDateString, HTTPClient.USERNAME));
					loadDatePicker();

				} else {
					dialogs.alertDialog(AlertType.WARNING, "Meeting", hTTPMessage.getMessage());
				}
			}

		} catch (ParseException e) {
			dialogs.exception("Cannot create time staps for meeting", e);
		}

	}

	/**
	 * When we click on a row, then we will find which members are selected
	 * They will be deleted when we delete the meeting
	 * @param event
	 */
	@FXML
	void clickedOnTable(MouseEvent event) {
		ConferenceBookingsTable selectedRow = conferenseroomTable.getSelectionModel().getSelectedItem();
		if(selectedRow == null) {
			return;
		}
		// Get the time in unix time stamp
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String from = selectedRow.getFrom();
		String to = selectedRow.getTo();
		try {
			long start = simpleDateFormat.parse(from).getTime();
			long end = simpleDateFormat.parse(to).getTime();
					
			// Create an object that we are going to search for in the database
			ConferenceRoom conferenceRoom = new ConferenceRoom();
			conferenceRoom.setStart(start);
			conferenceRoom.setEnd(end);
			conferenceRoom.setEmail(HTTPClient.USERNAME); // Only what you have created
			
			// Notice that hTTPMessage's field message contains the string of members
			HTTPMessage hTTPMessage = hTTPClient.postObject("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/user/getselectedmembers", conferenceRoom);
			if (hTTPMessage == null) {
				dialogs.alertDialog(AlertType.ERROR, "Connection", "No connection to databse");
			} else {
				if (hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
					// Got the members now - Insert them so when we look at the members, we can see who are selected
					selectedCompanyUsers.setMembers(hTTPMessage.getMessage());
				} else {
					dialogs.alertDialog(AlertType.INFORMATION, "Meeting","Cannot watch other meetings members");
				}
			}
			} catch (ParseException e) {
				dialogs.exception("Cannot delete row(s)", e);
		}
	}
}
