package se.danielmartensson.book.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.inject.Inject;


import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import cz.msebera.android.httpclient.HttpStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import se.danielmartensson.book.Main;
import se.danielmartensson.book.tools.http.HTTPClient;
import se.danielmartensson.book.tools.http.HTTPMessage;
import se.danielmartensson.book.tools.popup.Dialogs;
import se.danielmartensson.book.tools.popup.FileHandler;
import se.danielmartensson.book.views.savings.LastLogin;

public class LoginPresenter {

	public static final String LOGIN_PARAMETERS = "/BooKStorage/loginparameters.json";
	private static final String TEST_FILE_PATH  = "/BooKStorage/test.json";

	@FXML
    private View view;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private Button login;
    
    @Inject
    private HTTPClient hTTPClient;
    
    @Inject
	private Dialogs dialogs;
    
    @Inject
    private FileHandler fileHandler;
    
    @Inject
    private LastLogin lastLogin;

    /**
     * When we press the URL link "New user"
     * @param event
     */
    @FXML
    void newUser(ActionEvent event) {
    	MobileApplication.getInstance().switchView(Main.NEWUSER_VIEW);
    }
    
    /**
     * When we press the button "login"
     * @param event
     */
    @FXML
    void login(ActionEvent event) {
    	HTTPMessage hTTPMessage = hTTPClient.login(email.getText(), password.getText());
    	if(hTTPMessage == null) {
    		dialogs.alertDialog(AlertType.ERROR, "Login", "Cannot login");
    	}else {
    		if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
    			MobileApplication.getInstance().switchView(Main.CONFERANCE_VIEW);
    		}else {
        		dialogs.alertDialog(AlertType.WARNING, "Login", "Wrong password: " + hTTPMessage.getConnectionStatus());
    		}
    	}
    	
    }
    
    /**
	 * This function will set the parameters of last login
	 */
	private void loadLoginParameters() {
		if(fileHandler.exist(LOGIN_PARAMETERS) == true) {
			try {
				File file = fileHandler.loadNewFile(LOGIN_PARAMETERS);
				LastLogin lastLogin_SER = new Gson().fromJson(new JsonReader(new FileReader(file)), LastLogin.class);
				
				HTTPClient.ADDRESS = lastLogin_SER.getServerAddress();
		    	HTTPClient.PORT = Integer.parseInt(lastLogin_SER.getPort());
				
				lastLogin.setEmail(lastLogin_SER.getEmail());
				lastLogin.setPort(lastLogin_SER.getPort());
				lastLogin.setServerAddress(lastLogin_SER.getServerAddress());
				email.setText(lastLogin.getEmail());
				
			} catch (Exception e) {
				dialogs.exception("Cannot load: " + LOGIN_PARAMETERS, e);
			}
			
		}else {
			// No file exist, create
			fileHandler.createNewFile(LOGIN_PARAMETERS, true);
		}
	}
	
	/**
	 * This will save the login parameters
	 */
	private void saveLoginParameters() {
		if(fileHandler.exist(LOGIN_PARAMETERS) == true) {
			try {
				File file = fileHandler.loadNewFile(LOGIN_PARAMETERS);
				lastLogin.setEmail(email.getText());
				lastLogin.setServerAddress(HTTPClient.ADDRESS);
				lastLogin.setPort(String.valueOf(HTTPClient.PORT));
				String json = new Gson().toJson(lastLogin);
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
				bufferedWriter.write(json);
				bufferedWriter.close();
			} catch (Exception e) {
				dialogs.exception("Cannot load: " + LOGIN_PARAMETERS, e);
			}
			
		}else {
			// No file exist, create
			fileHandler.createNewFile(LOGIN_PARAMETERS, true);
		}
	}
    

    /**
     * Initial start up
     */
    @FXML
    public void initialize() {
    	// Check test - Need to be done
    	fileHandler.runCreateDeleteTest(TEST_FILE_PATH);
    			
    	// Slide in smooth
    	view.setShowTransitionFactory(BounceInRightTransition::new);
        
        // Listener for when we slide in or leave page
    	view.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                // When we slide in
            	AppBar appBar = MobileApplication.getInstance().getAppBar();
            	appBar.setOpacity(0); // Hide AppBar
            	
            	// Get the last successful login parameters - email
            	loadLoginParameters();
            }else {
            	// When we leave this page
            	saveLoginParameters();
            }
        });
    }
}
