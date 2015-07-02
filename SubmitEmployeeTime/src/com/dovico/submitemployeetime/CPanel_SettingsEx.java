package com.dovico.submitemployeetime;

import com.dovico.commonlibrary.CPanel_Settings;


public class CPanel_SettingsEx extends CPanel_Settings {
	private static final long serialVersionUID = 1L;
	

	// Default constructor
	public CPanel_SettingsEx() {
		// Call the parent class's constructor (handles setting up the controls) 
		super();
	}
	
	
	@Override
	public void setSettingsData(String sConsumerSecret, String sDataAccessToken, String sCompanyName, String sUserName, String sPassword, 
			String sApiVersionTargeted, Long lEmployeeID, String sEmployeeFirstName, String sEmployeeLastName) {
		// Pass the information to the parent class that it needs
		super.setSettingsData(sConsumerSecret, sDataAccessToken, sCompanyName, sUserName, sPassword, sApiVersionTargeted, lEmployeeID, 
				sEmployeeFirstName, sEmployeeLastName);
	}

	
	@Override
	public boolean validateSettingsData() {
		// If the parent class validation fails then...(checks the URI, Consumer Secret, and Data Access Token values)
		if(!super.validateSettingsData()) { return false; }
		
		// TO DO: Custom validation if need be (return 'false' if there is an issue)
				
		// Return true since there were no issues
		return true;
	}
}
