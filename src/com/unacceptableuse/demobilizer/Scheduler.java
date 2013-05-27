package com.unacceptableuse.demobilizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class Scheduler implements MouseListener{

	protected Shell shlLinkdemobilizerbotScheduler;
	Spinner spinner_1, spinner;
	Button btnRunFor;
	Label lblRanXTimes, lblCorrectedXLinks, lblXErrors, lblXDupes, lblXTotalScanned;
	int hours, times, posts, errors, scanned, dupes, timesran;
	boolean doInfinite = true;
	Reddit r = new Reddit(this);

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Scheduler window = new Scheduler();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlLinkdemobilizerbotScheduler.open();
		shlLinkdemobilizerbotScheduler.layout();
		while (!shlLinkdemobilizerbotScheduler.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlLinkdemobilizerbotScheduler = new Shell();
		shlLinkdemobilizerbotScheduler.setSize(450, 274);
		shlLinkdemobilizerbotScheduler.setText("LinkDemobilizerBot scheduler");
		
		Button btnNewButton = new Button(shlLinkdemobilizerbotScheduler, SWT.NONE);
		btnNewButton.addMouseListener(this);
		btnNewButton.setBounds(10, 10, 414, 84);
		btnNewButton.setText("Start");
		
		spinner = new Spinner(shlLinkdemobilizerbotScheduler, SWT.BORDER);
		spinner.setSelection(3);
		spinner.setBounds(88, 119, 47, 22);
		
		spinner_1 = new Spinner(shlLinkdemobilizerbotScheduler, SWT.BORDER);
		spinner_1.setSelection(2);
		spinner_1.setBounds(52, 185, 47, 18);
		
		lblRanXTimes = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblRanXTimes.setBounds(246, 121, 144, 15);
		lblRanXTimes.setText("Ran "+timesran+" times");
		
		lblCorrectedXLinks = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblCorrectedXLinks.setBounds(246, 145, 96, 16);
		lblCorrectedXLinks.setText("Corrected "+posts+" links");
		
		lblXErrors = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblXErrors.setBounds(256, 167, 55, 15);
		lblXErrors.setText(errors+" errors");
		
		lblXDupes = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblXDupes.setBounds(256, 188, 55, 15);
		lblXDupes.setText(dupes+" dupes");
		
		lblXTotalScanned = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblXTotalScanned.setBounds(256, 211, 116, 15);
		lblXTotalScanned.setText(scanned+" total scanned");
		
		btnRunFor = new Button(shlLinkdemobilizerbotScheduler, SWT.CHECK);
		btnRunFor.setBounds(10, 187, 36, 16);
		btnRunFor.setText("Run");
		
		Label lblTimes = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblTimes.setBounds(105, 188, 55, 15);
		lblTimes.setText("times.");
		
		Label lblHours = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblHours.setBounds(141, 122, 55, 15);
		lblHours.setText("hours.");
		
		Label lblRunEvery = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblRunEvery.setBounds(29, 122, 55, 15);
		lblRunEvery.setText("Run every");
		
		Label lblNoteUiIs = new Label(shlLinkdemobilizerbotScheduler, SWT.NONE);
		lblNoteUiIs.setBounds(0, 221, 250, 15);
		lblNoteUiIs.setText("Note: UI is unusable when scan is running.");

	}

	@Override
	public void mouseDoubleClick(MouseEvent arg0) {

		
	}

	@Override
	public void mouseDown(MouseEvent arg0) {
		hours = spinner.getSelection();
		times = spinner_1.getSelection();
		timesran++;
		
		System.out.println("Running "+times+" times");
		
		while(times != timesran){
			Display.getDefault().syncExec(new Runnable()
			{
				
				@Override
				public void run() {
					try {
						r.modhash = r.login("LinkDemobilizerBot", "01189998819991197253");
						ArrayList<String>subreddits = r.getSubscribedReddits();
					
						
						
						for(String s : subreddits)
						{
							r.scanSubreddit(s, 25);
							r.run();
							lblRanXTimes.setText("Ran "+timesran+" times");
							lblXErrors.setText(errors+" errors");
							lblXDupes.setText(dupes+" dupes");
							lblXTotalScanned.setText(scanned+" total scanned");
							lblCorrectedXLinks.setText("Corrected "+posts+" links");
							lblRanXTimes.redraw(); lblCorrectedXLinks.redraw(); lblXErrors.redraw(); lblXDupes.redraw(); lblXTotalScanned.redraw();
							shlLinkdemobilizerbotScheduler.redraw();
							
						}
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
			});
			if(!btnRunFor.getEnabled())times++;
			System.out.println("Waiting...");
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS));
			} catch (InterruptedException e) {e.printStackTrace();}
		
			
		}

	}

	@Override
	public void mouseUp(MouseEvent arg0) {

		
	}
}
