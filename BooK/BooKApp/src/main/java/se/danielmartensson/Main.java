package se.danielmartensson;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.SplashView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.danielmartensson.views.ConferenceroomView;
import se.danielmartensson.views.LoginView;
import se.danielmartensson.views.MembersTableView;
import se.danielmartensson.views.NewUserView;
import se.danielmartensson.views.UserTableView;

public class Main extends MobileApplication {
	
	public static final String LOGIN_VIEW = HOME_VIEW;
    public static final String CONFERANCE_VIEW = "Conference View";
    public static final String USERTABLE_VIEW = "User table View";
    public static final String NEWUSER_VIEW = "New user View";
    public static final String MEMBERSTABLE_VIEW = "Members table View";
    
    /**
     * THIRST THING YOU NEED TO DO IS TO SET 'ADDRESS' AND 'PORT' IN HTTPClient.java
     */
    
    @Override
    public void init() {
    	// Add splash screen
    	addViewFactory(MobileApplication.SPLASH_VIEW, () -> {
    		Image image = new Image("icon.png");
    		ImageView imageView  = new ImageView(image);
    	     SplashView splashView = new SplashView(imageView);
    	     splashView.setOnShown(e -> {
    	         PauseTransition pause = new PauseTransition(Duration.seconds(3));
    	         pause.setOnFinished(e1 -> splashView.hideSplashView());
    	         pause.play();
    	     }); 
    	     return splashView;
    	 });
    	
    	addViewFactory(LOGIN_VIEW, () -> (View) new LoginView().getView());
        addViewFactory(CONFERANCE_VIEW, () -> (View) new ConferenceroomView().getView());
        addViewFactory(USERTABLE_VIEW, () -> (View) new UserTableView().getView());
        addViewFactory(NEWUSER_VIEW, () -> (View) new NewUserView().getView());
        addViewFactory(MEMBERSTABLE_VIEW, () -> (View) new MembersTableView().getView());
        
        DrawerManager.buildDrawer(this);
        
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.RED.assignTo(scene);

        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));
    }
}
