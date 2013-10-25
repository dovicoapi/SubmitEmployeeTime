package com.dovico.submitemployeetime;

import javax.swing.JApplet;
import java.awt.event.*;
import netscape.javascript.*; // Needed for JavaScript communication (found in plugin.jar of 'C:\Program Files (x86)\Java\jre6\lib\')


public class Applet_Main extends JApplet {
	private static final long serialVersionUID = 1L;

	// Our UI Logic class (handles creating the controls, wiring up event handlers as need be, etc)
	private CCommonUILogic m_UILogic = null;
	
	
	// Default constructor
	public Applet_Main(){
		// Have the tab control, and related controls, created (NOTE: If you pass the content pane in as a parameter, it works at run-time but you can't use the
		// Google WindowBuilder Design tab - nothing shows up. When you return the root control, the JTabbedPane in this case, add add it to the content pane then
		// everything shows up but you still can't edit it using the Design tab.)
		m_UILogic = new CCommonUILogic(getContentPane(), GetActionListenerForSettingsChange());
	}
		
	
	// Action Listener for when the settings are changed (callback function from the CommonUILogic class)
	private ActionListener GetActionListenerForSettingsChange(){
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Grab the current Consumer Secret we have. If it matches our constant then clear the variable so that we don't save the value to a cookie potentially
				// exposing sensitive information
				String sConsumerSecretToSave = m_UILogic.getConsumerSecret();
				if(sConsumerSecretToSave.equals(Constants.CONSUMER_SECRET_API_TOKEN)){ sConsumerSecretToSave = ""; }
				
				// Call our Save Settings function
				saveSettings(m_UILogic.getDataAccessToken(), m_UILogic.getCompanyName(), m_UILogic.getUserName(), Long.toString(m_UILogic.getEmployeeID()), m_UILogic.getEmployeeFirstName(), 
						m_UILogic.getEmployeeLastName());
			}
		};
	}
	
	
	@Override
	public void init() {
		// I'm not sure what's the deal but this throws an exception when run from the IDE. When run from a web page it works fine.		
		try{
			JSObject winMain = JSObject.getWindow(this);
			if(winMain != null){  winMain.call("passUsTheSettingsData", null); }	
		}
		catch(Throwable e){}
	}
	
	
	// Called by the JavaScript to tell us what the settings are from the cookie/localStorage (without signing this app we don't have permission to access Preferences
	// so we're doing a workaround instead) 
	public void JSCallBackReturningSettingsData(String sUserToken, String sCompanyName, String sUserName, String sPassword, String sEmployeeID, String sEmployeeFirstName, String sEmployeeLastName){
		m_UILogic.handlePageLoad(sUserToken, sCompanyName, sUserName, sPassword, Long.valueOf(sEmployeeID), sEmployeeFirstName, sEmployeeLastName);
	}
	
	
	// Called when the user clicks on the Main tab after having been on the Settings tab and everything validated OK 
	private void saveSettings(String sUserToken, String sCompanyName, String sUserName, String sEmployeeID, String sEmployeeFirstName, String sEmployeeLastName) {
		try{
			// Tell the JS to save the settings data to a cookie or localStorage
			JSObject winMain = JSObject.getWindow(this);
			if(winMain != null){  winMain.call("saveTheSettingsData", new String[] { sUserToken, sCompanyName, sUserName, sEmployeeID, sEmployeeFirstName, sEmployeeLastName }); }	
		}
		catch(Throwable e){}
	}
}
