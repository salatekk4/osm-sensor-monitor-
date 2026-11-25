package ru.miet.osmsensors.view;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

public class TextField extends JTextField {
	public TextField() {
		super();
	}
	
	public TextField(String placeholderText) {
		super(placeholderText);
		setForeground(Color.GRAY);
		setEditable(false);
		setCaretPosition(0);
		this.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				setForeground(Color.BLACK);
				if (!isEditable()) setText("");
		        setEditable(true);
		    }
			
			public void focusLost(FocusEvent e) {
				if (getText().isEmpty()) {
					setForeground(Color.GRAY);
					setText(placeholderText);
					setEditable(false);
					setCaretPosition(0);
				}
			}
		});
	}
}
