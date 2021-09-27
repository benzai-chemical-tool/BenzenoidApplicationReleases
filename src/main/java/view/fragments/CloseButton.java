package view.fragments;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.Utils;

public class CloseButton extends Button{

	@SuppressWarnings("unused")
	private FragmentListPane parent;
	private int index;
	
	public CloseButton(FragmentListPane parent, int index) {
		
		this.parent = parent;
		this.index = index;
		
		Image image;
		
		if (Utils.onWindows())
			image = new Image("file:graphics\\close_button.png");
		else
			image = new Image("file:graphics/close_button.png");
				
		ImageView view = new ImageView(image);
	    
		this.resize(30, 30);
		
		this.setPadding(new Insets(0));
		
		this.setGraphic(view);
		
	    this.setOnAction(e -> {
	    	if (parent.getNbItems() == 1) {
	    		Utils.alert("You cannot delete the last fragment.");
	    	}
	    	else {
	    		parent.removeEntry(index);
	    	}
	    });
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return "CloseButton: " + index;
	}
}
