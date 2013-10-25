package com.dovico.submitemployeetime;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.prefs.Preferences;

import javax.swing.JFrame;


public class Form_Main {
	
	// The main window that will be displayed to the user
	private JFrame m_frmSubmitEmployeeTime = null;
	
	// Our UI Logic class (handles creating the controls, wiring up event handlers as need be, etc)
	private CCommonUILogic m_UILogic = null;
	
	
	// Main entry point of the application (when run as a desktop application)
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Form_Main window = new Form_Main();
					window.m_frmSubmitEmployeeTime.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	// Constructor
	public Form_Main() {
		// Create the main frame of the application
		m_frmSubmitEmployeeTime = new JFrame();
		m_frmSubmitEmployeeTime.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) 
			{
				// Grab the Preferences values (have to do this here rather than in the CommonUILogic since this will break an unsigned applet)
				Preferences prefs = Preferences.userNodeForPackage(Form_Main.class);
				
				String sCompanyName = prefs.get("CompanyName", "");;
				String sUserName = prefs.get("UserName", "");
				String sPassword = "";
				
				//String sConsumerSecret = prefs.get(Constants.PREFS_KEY_CONSUMER_SECRET, "");
				String sDataAccessToken = prefs.get(Constants.PREFS_KEY_USER_TOKEN, "");
				String sEmployeeID = prefs.get(Constants.PREFS_KEY_EMPLOYEE_ID, "0");
				String sEmployeeFirstName = prefs.get(Constants.PREFS_KEY_EMPLOYEE_FIRST, "");
				String sEmployeeLastName = prefs.get(Constants.PREFS_KEY_EMPLOYEE_LAST, "");
												
				m_UILogic.handlePageLoad(sDataAccessToken, sCompanyName, sUserName, sPassword, Long.valueOf(sEmployeeID), sEmployeeFirstName, sEmployeeLastName); 
			}
		});
		m_frmSubmitEmployeeTime.setTitle("Submit Employee Time");
		m_frmSubmitEmployeeTime.setBounds(100, 100, 740, 485);
		m_frmSubmitEmployeeTime.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Prevent the application from being resized too small (causes the layout to mess up for the CPanel_TimeEntries if the width passes a certain point)
		m_frmSubmitEmployeeTime.setMinimumSize(new Dimension(600, 485)); 
		
		// Have the tab control, and related controls, created (NOTE: If you pass the content pane in as a parameter, it works at run-time but you can't use the
		// Google WindowBuilder Design tab - nothing shows up. When you return the root control, the JTabbedPane in this case, add add it to the content pane then
		// everything shows up but you still can't edit it using the Design tab.)
		m_UILogic = new CCommonUILogic(m_frmSubmitEmployeeTime.getContentPane(), getActionListenerForSettingsChange());
	}
	
	
	// Action Listener for when the settings are changed (callback function from the CommonUILogic class)
	private ActionListener getActionListenerForSettingsChange(){
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {  
				// Grab the current Consumer Secret we have. If it matches our constant then clear the variable so that we don't save the value potentially
				// exposing sensitive information
				String sConsumerSecretToSave = m_UILogic.getConsumerSecret();
				if(sConsumerSecretToSave.equals(Constants.CONSUMER_SECRET_API_TOKEN)){ sConsumerSecretToSave = ""; }

				
				// Save the settings
				Preferences prefs = Preferences.userNodeForPackage(Form_Main.class);
				prefs.put("CompanyName", m_UILogic.getCompanyName());
				prefs.put("UserName",  m_UILogic.getUserName());
				
				//prefs.put(Constants.PREFS_KEY_CONSUMER_SECRET, sConsumerSecretToSave);
				prefs.put(Constants.PREFS_KEY_USER_TOKEN, m_UILogic.getDataAccessToken());
				prefs.put(Constants.PREFS_KEY_EMPLOYEE_ID, Long.toString(m_UILogic.getEmployeeID()));
				prefs.put(Constants.PREFS_KEY_EMPLOYEE_FIRST, m_UILogic.getEmployeeFirstName());
				prefs.put(Constants.PREFS_KEY_EMPLOYEE_LAST, m_UILogic.getEmployeeLastName());
			}
		};
	}	
}
