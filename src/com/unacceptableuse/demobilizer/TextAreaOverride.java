package com.unacceptableuse.demobilizer;

import javax.swing.JTextArea;

public class TextAreaOverride extends JTextArea{


	public TextAreaOverride(int rows, int cols) {
	super(rows,cols);
	}
	public TextAreaOverride() {

	}
	
	public void append(String text) {
	super.append(text);
	//this.setCaretPosition(text.length());
	//this.setCaretPosition(this.getCaretPosition()+text.length());
	}
	

}
