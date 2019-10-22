package se.danielmartensson.book;

import static se.danielmartensson.book.Main.CONFERANCE_VIEW;
import static se.danielmartensson.book.Main.LOGIN_VIEW;
import static se.danielmartensson.book.Main.MEMBERSTABLE_VIEW;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.scene.image.Image;
import se.danielmartensson.tools.popup.Dialogs;

public class DrawerManager {
	
	private static Dialogs dialogs = new Dialogs();

    public static void buildDrawer(MobileApplication app) {
        NavigationDrawer drawer = app.getDrawer();
        
        NavigationDrawer.Header header = new NavigationDrawer.Header("BooK", "Spektrakon bookningssystem", new Avatar(21, new Image(DrawerManager.class.getResourceAsStream("/icon.png"))));
        drawer.setHeader(header);
        
        final Item conferenceroomItem = new ViewItem("Booka konferensrum", MaterialDesignIcon.DATE_RANGE.graphic(), CONFERANCE_VIEW, ViewStackPolicy.SKIP);
        final Item loginItem = new ViewItem("Logga ut", MaterialDesignIcon.FULLSCREEN_EXIT.graphic(), LOGIN_VIEW, ViewStackPolicy.SKIP);
        final Item memberstableItem = new ViewItem("Medlemmar", MaterialDesignIcon.VERIFIED_USER.graphic(), MEMBERSTABLE_VIEW, ViewStackPolicy.SKIP);

        drawer.getItems().addAll(conferenceroomItem, memberstableItem, loginItem);
        
        if (true) { // Used to be Platform.isDesktop()
            final Item quitItem = new Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
            quitItem.selectedProperty().addListener((obs, oldValue, newValue) -> {
            	if (newValue)
                	if(dialogs.question("Quit", "Do you want to exit?") == true)
                		Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                
            });
            drawer.getItems().add(quitItem);
        }
    }
}