package se.danielmartensson.book.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.DropdownButton;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import se.danielmartensson.book.Main;
import se.danielmartensson.book.tools.http.HTTPClient;
import se.danielmartensson.book.tools.http.HTTPMessage;
import se.danielmartensson.book.tools.popup.Dialogs;
import se.danielmartensson.book.tools.popup.FileHandler;
import se.danielmartensson.book.views.entity.Role;
import se.danielmartensson.book.views.entity.User;
import se.danielmartensson.book.views.savings.LastLogin;

public class NewUserPresenter {

	private static final String[] ports = { "80", "8080", "8085", "8090" };

	@FXML
	private View view;

	@FXML
	private TextField mail;

	@FXML
	private PasswordField password;

	@FXML
	private TextField serverAddress;

	@FXML
	private DropdownButton port;

	@FXML
	private VBox configurationBox;

	@Inject
	private Dialogs dialogs;

	@Inject
	private FileHandler fileHandler;

	@Inject
	private LastLogin lastLogin;

	@FXML
	void createUser(ActionEvent event) {
		try {
			// Set the timeout
			int timeout = 30; // seconds
			RequestConfig config = RequestConfig.custom()
			  .setConnectTimeout(timeout * 1000)
			  .setConnectionRequestTimeout(timeout * 1000)
			  .setSocketTimeout(timeout * 1000).build();
			
			// This will send a HTTP request to the server, without auth
			BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
			CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultRequestConfig(config).build();
			//CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("http://" + HTTPClient.ADDRESS + ":" + HTTPClient.PORT + "/unauth/createnewuser");

			// Create a role
			Role role = new Role();
			role.setRole("USER");
			Set<Role> roles = new HashSet<Role>();
			roles.add(role);

			// Create user object
			User user = new User();
			user.setUsername(mail.getText());
			user.setEmail(mail.getText());
			user.setPassword(password.getText());
			user.setRoles(roles);

			// Post the object to server and get reponse

			httppost.setEntity(new StringEntity(new Gson().toJson(user)));
			httppost.setHeader("Content-type", "application/json");
			CloseableHttpResponse response = httpclient.execute(httppost);

			// Get the HTTPMessage object
			String json = EntityUtils.toString(response.getEntity());
			Type type = new TypeToken<HTTPMessage>() {
			}.getType();
			HTTPMessage hTTPMessage = new Gson().fromJson(json, type);

			// Check result
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
					dialogs.alertDialog(AlertType.INFORMATION, "Success", hTTPMessage.getMessage());
					MobileApplication.getInstance().switchView(Main.LOGIN_VIEW);
				} else {
					dialogs.alertDialog(AlertType.ERROR, "Fail", hTTPMessage.getMessage());
				}
			} else {
				dialogs.alertDialog(AlertType.ERROR, "Fail", "No connection to server");
			}

		} catch (Exception e) {
			dialogs.exception("Cannot create new user", e);
		}

	}

	@FXML
	public void initialize() {
		// Slide in smooth
		view.setShowTransitionFactory(BounceInRightTransition::new);

		// Listener for when we slide in or leave page
		view.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				// When we slide in
				AppBar appBar = MobileApplication.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK
						.button(e -> MobileApplication.getInstance().switchView(Main.LOGIN_VIEW)));
				appBar.getActionItems().add(MaterialDesignIcon.BUILD.button(e -> enableConfiguration()));
				appBar.setTitleText("Ny medlem");
				appBar.setOpacity(1); // Show AppBar - Because we enter this page as first page
			} else {
				// When we leave the page
			}
		});

		// Add ports
		for (int i = 0; i < ports.length; i++)
			port.getItems().add(new MenuItem(ports[i]));

	}

	/**
	 * This enable the Vbox and the opacity will be 1, press again and the Vbox will
	 * disapear
	 */
	private void enableConfiguration() {
		if (configurationBox.isDisable() == true) {
			configurationBox.setDisable(false);
			configurationBox.setOpacity(1.0);
		} else {
			configurationBox.setDisable(true);
			configurationBox.setOpacity(0.0);
		}
	}

	/**
	 * This will save the configuration for server address and port
	 * 
	 * @param event
	 */
	@FXML
	void saveConfiguration(ActionEvent event) {
		HTTPClient.ADDRESS = serverAddress.getText();
		HTTPClient.PORT = Integer.parseInt(port.getSelectedItem().getText());

		// First load the user email
		loadLoginParameters();
		// Then replace with new port and ip address
		saveLoginParameters();
		
		dialogs.alertDialog(AlertType.INFORMATION, "Configurations set", "Will be saved to json file.");
	}

	/**
	 * This will save the login parameters
	 */
	private void saveLoginParameters() {
		if (fileHandler.exist(LoginPresenter.LOGIN_PARAMETERS) == true) {
			try {
				File file = fileHandler.loadNewFile(LoginPresenter.LOGIN_PARAMETERS);
				lastLogin.setServerAddress(HTTPClient.ADDRESS);
				lastLogin.setPort(port.getSelectedItem().getText());
				String json = new Gson().toJson(lastLogin);
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
				bufferedWriter.write(json);
				bufferedWriter.close();
			} catch (Exception e) {
				dialogs.exception("Cannot load: " + LoginPresenter.LOGIN_PARAMETERS, e);
			}

		} else {
			// No file exist, create
			fileHandler.createNewFile(LoginPresenter.LOGIN_PARAMETERS, true);
		}
	}

	/**
	 * This function will ONLY get the login email
	 */
	private void loadLoginParameters() {
		if (fileHandler.exist(LoginPresenter.LOGIN_PARAMETERS) == true) {
			try {
				File file = fileHandler.loadNewFile(LoginPresenter.LOGIN_PARAMETERS);
				LastLogin lastLogin_SER = new Gson().fromJson(new JsonReader(new FileReader(file)), LastLogin.class);

				lastLogin.setEmail(lastLogin_SER.getEmail());

			} catch (Exception e) {
				dialogs.exception("Cannot load: " + LoginPresenter.LOGIN_PARAMETERS, e);
			}

		} else {
			// No file exist, create
			fileHandler.createNewFile(LoginPresenter.LOGIN_PARAMETERS, true);
		}
	}
}
