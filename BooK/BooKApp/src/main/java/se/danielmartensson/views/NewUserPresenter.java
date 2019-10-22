package se.danielmartensson.views;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import javafx.scene.control.Alert.AlertType;
import se.danielmartensson.book.Main;
import se.danielmartensson.tools.http.HTTPClient;
import se.danielmartensson.tools.http.HTTPMessage;
import se.danielmartensson.tools.popup.Dialogs;
import se.danielmartensson.views.entity.Role;
import se.danielmartensson.views.entity.User;

public class NewUserPresenter {

	@FXML
    private View view;

    @FXML
    private TextField mail;

    @FXML
    private PasswordField password;
    
    @Inject
    private Dialogs dialogs;

    @FXML
    void createUser(ActionEvent event) {
    	try {
	    	// This will send a HTTP request to the server, without auth
	    	CloseableHttpClient httpclient = HttpClients.createDefault();
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
			Type type = new TypeToken<HTTPMessage>() {}.getType();
			HTTPMessage hTTPMessage = new Gson().fromJson(json, type);
			
			// Check result
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if(hTTPMessage.getMessageStatusCode() == HttpStatus.SC_OK) {
					dialogs.alertDialog(AlertType.INFORMATION, "Success", hTTPMessage.getMessage());
				}else {
					dialogs.alertDialog(AlertType.ERROR, "Fail", hTTPMessage.getMessage());
				}
			}else {
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
				appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> MobileApplication.getInstance().switchView(Main.LOGIN_VIEW)));
				appBar.setTitleText("Ny medlem");
				appBar.setOpacity(1); // Show AppBar - Because we enter this page as first page
            }else {
            	// When we leave the page
            }
        });
    }
}
