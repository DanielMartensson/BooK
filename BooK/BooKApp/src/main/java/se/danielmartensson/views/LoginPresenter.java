package se.danielmartensson.views;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import se.danielmartensson.Main;
import se.danielmartensson.tools.http.HTTPClient;
import se.danielmartensson.tools.http.HTTPMessage;
import se.danielmartensson.tools.popup.Dialogs;

public class LoginPresenter {

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
            	appBar.setOpacity(0); // Hide AppBar
            }else {
            	// When we leave this page
            }
        });
    }
}
