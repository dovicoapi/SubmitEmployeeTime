package com.dovico.submitemployeetime;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;

import com.dovico.commonlibrary.CPanel_About;


public class CCommonUILogic {
	// Listener from the UI class that gets called when the settings data has changed so that it can update its UI as need be
	private ActionListener m_alSettingsChanged = null; 
	
	private JTabbedPane m_pTabControl = null;
	
	private CPanel_TimeEntries m_pTimeEntriesTab = null;	
	private CPanel_SettingsEx m_pSettingsTab = null;
	private CPanel_About m_pAboutTab = null;
	private int m_iPreviousTabIndex = -1;
	private int m_iSettingsTabIndex = 1;
	
	
	private String m_sCompanyName = "";
	private String m_sUserName = "";
	private String m_sPassword = "";
	private String m_sDataAccessToken = "";
	private Long m_lEmployeeID = null;
	private String m_sEmployeeFirstName = "";
	private String m_sEmployeeLastName = "";
	
	
	// Overloaded constructor
	public CCommonUILogic(Container cContainer, ActionListener alSettingsChanged) {
		// Change the look from the Metal UI which I find kind of ugly
		try {
			// Loop through the various LookAndFeel items to see if 'Nimbus' exists. If yes then...
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        } // End if ("Nimbus".equals(info.getName()))
		    } // End of the for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) loop.
		}catch (Exception e) {
			// Switch the look to the system default
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
			catch (UnsupportedLookAndFeelException e2) { }
			catch (ClassNotFoundException e2) { }
			catch (InstantiationException e2) { }
			catch (IllegalAccessException e2) { }
		} // End of the catch (Exception e) statement.
			
		
		// Remember the action listener for when the settings are changed (so that we can tell the proper class that the settings have changed and they need to be
		// saved
		m_alSettingsChanged = alSettingsChanged;
		
		
		// Cause the controls to be created
		initializeControls(cContainer);
	}
	
	
	// Initialization function to call to have the controls created and added to the container specified
	private void initializeControls(Container cContainer){
		// Create our tab control
		m_pTabControl = new JTabbedPane();
		m_pTabControl.setFont(new Font("Arial", Font.PLAIN, 11));
		m_pTabControl.addChangeListener(new ChangeListener() {		    
		    public void stateChanged(ChangeEvent evt) { handleTabsChanged(); }
		});
		
		// Add the tab control to the container passed in
		cContainer.add(m_pTabControl);

		
		// Create our Time Entry panel and add it to our tab control
		m_pTimeEntriesTab = new CPanel_TimeEntries(this);
		m_pTabControl.addTab("Time Entries", null, m_pTimeEntriesTab, null);

		// Create our Settings Tab panel and add it to our tab control
		m_pSettingsTab = new CPanel_SettingsEx(); 
		m_pTabControl.addTab("Settings", null, m_pSettingsTab, null);
		
		// Create our About Tab panel and add it to our tab control
		m_pAboutTab = new CPanel_About("Submit Employee Time", "1.5"); 
		m_pTabControl.addTab("About", null, m_pAboutTab, null);
	}
	
	
	// Called when the form is first displayed (windowOpened event)
	public void handlePageLoad(String sDataAccessToken, String sCompanyName, String sUserName, String sPassword, Long lEmployeeID, String sEmployeeFirstName, String sEmployeeLastName) 
	{ 
		m_sDataAccessToken = sDataAccessToken;
		m_sCompanyName = sCompanyName;
		m_sUserName = sUserName;
		m_sPassword = sPassword;
		
		m_lEmployeeID = lEmployeeID;
		m_sEmployeeFirstName = sEmployeeFirstName;
		m_sEmployeeLastName = sEmployeeLastName;
		
		// Determine if the tokens have values or not
		//boolean bIsConsumerSecretEmpty = m_sConsumerSecret.isEmpty();
		boolean bIsDataAccessTokenEmpty = m_sDataAccessToken.isEmpty();
		
		
		// Tell the Settings pane what the settings are (we are not concerned about the logged in employee's First and Last name in this app but rather than have
		// to write upgrade code, like the code to come below, if that every changes, we grab and store the values just in case)
		m_pSettingsTab.setSettingsData(Constants.CONSUMER_SECRET_API_TOKEN, sDataAccessToken, sCompanyName, sUserName, sPassword, Constants.API_VERSION_TARGETED, m_lEmployeeID, m_sEmployeeFirstName, m_sEmployeeLastName);
		
		// If either token value is empty then...
		if(bIsDataAccessTokenEmpty) {
			// Make sure the Settings tab is selected
			m_iSettingsTabIndex = 1;
			m_pTabControl.setSelectedIndex(m_iSettingsTabIndex);
		}else {// We're good to go...
			// Cause the Time Entry panel to load in its data
			m_pTimeEntriesTab.initializeControls();
		} // End if(bIsConsumerSecretEmpty || bIsDataAccessTokenEmpty)
	}	
	
	
	// Tab's selection has been changed (NOTE: This gets called when the control is initially displayed and when the tab's selection is changed via code)
	private void handleTabsChanged() {
		// If the previous tab was the Settings tab then...(user just tabbed off of the settings tab)
	    if(m_iPreviousTabIndex == m_iSettingsTabIndex)
	    {
	    	// If everything validates OK for the Settings tab then...
	    	if(m_pSettingsTab.validateSettingsData()) 
	    	{
	    		// Grab the new settings values
	    		m_sDataAccessToken = m_pSettingsTab.getDataAccessToken();
	    		m_sCompanyName = m_pSettingsTab.getCompanyName();
	    		m_sUserName = m_pSettingsTab.getUserName();
	    		
	    		m_lEmployeeID = m_pSettingsTab.getEmployeeID();
	    		m_sEmployeeFirstName = m_pSettingsTab.getEmployeeFirstName();
				m_sEmployeeLastName = m_pSettingsTab.getEmployeeLastName();
	    		
	    		// Update the UI class telling it that the settings have been changed (so that the settings can be saved and data reloaded - we need to do it this
	    		// way rather than handling the load/save in the Setting panel because if the settings panel is used by an Applet, having a reference to 
	    		// 'java.util.prefs.Preferences' will throw an exception)
	    		m_alSettingsChanged.actionPerformed(null);
	    		
	    		// Cause the Time Entry panel to load in its data now that we may have new login credentials 
				m_pTimeEntriesTab.initializeControls();
	    	} 
	    	else // Validation failed... 
	    	{ 		        	
	    		// Reselect the Settings tab (indicate that the previous index is not the settings tab so that the validation is not hit again) 
	    		m_iPreviousTabIndex = -1; 
	    		m_pTabControl.setSelectedIndex(m_iSettingsTabIndex);
	    	} // End if(m_pSettingsTab.validateSettingsData())
	    } // End if(m_iPreviousTabIndex == m_iSettingsTabIndex)
	    
	    
	    // Remember the selected tab index
	    m_iPreviousTabIndex = m_pTabControl.getSelectedIndex();
	}
	
		
	// Methods returning the setting values
	public String getCompanyName() { return m_sCompanyName; }
	public String getUserName() { return m_sUserName; }
	public String getPassword() { return m_sPassword; }
	public String getConsumerSecret() { return Constants.CONSUMER_SECRET_API_TOKEN; }
	public String getDataAccessToken() { return m_sDataAccessToken; }
	public Long getEmployeeID() { return m_lEmployeeID; }
	public String getEmployeeFirstName() { return m_sEmployeeFirstName; }
	public String getEmployeeLastName() { return m_sEmployeeLastName; }
}
