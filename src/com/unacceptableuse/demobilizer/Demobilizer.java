package com.unacceptableuse.demobilizer;

import javax.swing.JFrame;

import org.eclipse.swt.SWT;


public class Demobilizer extends SWT{

	
	

	public Demobilizer()
	{
		
		
	}
		
	
	
	public static void main(String[] args)
	{
/*		System.out.println("Attempting to login ");
		t*/
		System.setProperty("http.agent", "");
		new Demobilizer();
		
	}
	
	public static void log(String s)
	{
		System.out.println(s);
	}
}
