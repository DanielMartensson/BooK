package se.danielmartensson.tools.popup;

import com.gluonhq.charm.glisten.layout.layer.PopupView;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ScrollableButton {
	
	/**
	 * Turn a button to a scrollable button and connect timeTo to timeFrom action event
	 * @param timeFrom
	 * @param timeTo
	 * @param items
	 */
	public void turnToScrollablee(Button timeFrom, Button timeTo, String[] items) {
		PopupView timeFromPop = setStyleButton(timeFrom);
		PopupView timeToPop = setStyleButton(timeTo);

	    VBox timeFromVbox = new VBox();
	    VBox timeToVbox = new VBox();
	    ScrollPane timeFromScroll = new ScrollPane(timeFromVbox);
	    ScrollPane timeToScroll = new ScrollPane(timeToVbox);
	    
	    // insert items
	    for (int i = 0; i < items.length; i++) {
	    	// Button item for timeFrom
	        Button itemTimeFrom = new Button(items[i]);
	        itemTimeFrom.setPrefWidth(100);
	        itemTimeFrom.getStyleClass().add("flat");
	        
	        // Button item for timeTo
	        Button itemTimeTo = new Button(items[i]);
	        itemTimeTo.setPrefWidth(100);
	        itemTimeTo.getStyleClass().add("flat");
	        
	        // Action for timeFrom
	        itemTimeFrom.setOnAction(e -> {
	        	timeFrom.setText(itemTimeFrom.getText()); // Item text to button
	        	timeTo.setText(itemTimeFrom.getText()); // When we select timeFrom, we set the same text on timeTo
	        	double position = timeFromScroll.getVvalue();
	        	timeToScroll.setVvalue(position);
	        	timeFromPop.hide();
	        });
	        
	        // Action for timeTo
	        itemTimeTo.setOnAction(e ->{
	        	timeTo.setText(itemTimeTo.getText());
	        	timeToPop.hide();
	        });
	        
	        // To Vbox
	        timeFromVbox.getChildren().add(itemTimeFrom);
	        timeToVbox.getChildren().add(itemTimeTo);
	    }
	    
	    // Set the action
	    setTheAction(timeFrom, timeFromPop, timeFromVbox, timeFromScroll);
	    setTheAction(timeTo, timeToPop, timeToVbox, timeToScroll);

	    
	}
	
	/**
	 * Gluon PopupView object of a button
	 * @param button
	 * @return
	 */
	private PopupView setStyleButton(Button button) {
		button.getStyleClass().add("flat");
	    button.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0");
	    button.setContentDisplay(ContentDisplay.RIGHT);

	    return new PopupView(button);
	}
	
	/**
	 * Set action to our button so it become scrollable
	 * @param button
	 * @param popupView
	 * @param vbox
	 */
	private void setTheAction(Button button, PopupView popupView, VBox vbox, ScrollPane scrollPane) {
		// Implement scroll pane
	    scrollPane.setMaxHeight(200);
	    scrollPane.setPrefWidth(110);

	    // Set the scroll pane and implement an action event
	    popupView.setContent(scrollPane);
	    button.setOnAction(event -> popupView.show());
	}
}
