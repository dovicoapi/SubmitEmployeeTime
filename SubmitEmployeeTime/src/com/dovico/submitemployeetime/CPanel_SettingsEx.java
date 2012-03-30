package com.dovico.submitemployeetime;

import java.awt.*;
import javax.swing.*;

import com.dovico.commonlibrary.CPanel_Settings;


public class CPanel_SettingsEx extends CPanel_Settings {
	private static final long serialVersionUID = 1L;
	
	// Default constructor
	public CPanel_SettingsEx() {
		// Call the parent class's constructor (handles setting up the controls) 
		super();
		
		JLabel lblNewLabel = new JLabel("This App requires administrator API tokens from DOVICO Timesheet\u2122 or");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		lblNewLabel.setBounds(10, 84, 430, 14);
		add(lblNewLabel);
		
		JLabel lblDovicoPlanningAnd = new JLabel("DOVICO Planning and Timesheet\u2122 via the Database Options");
		lblDovicoPlanningAnd.setHorizontalAlignment(SwingConstants.CENTER);
		lblDovicoPlanningAnd.setFont(new Font("Arial", Font.PLAIN, 11));
		lblDovicoPlanningAnd.setBounds(10, 101, 430, 14);
		add(lblDovicoPlanningAnd);
		
		JLabel lblmenuSetup = new JLabel("(Menu > Setup > Database Options > API)");
		lblmenuSetup.setHorizontalAlignment(SwingConstants.CENTER);
		lblmenuSetup.setFont(new Font("Arial", Font.PLAIN, 11));
		lblmenuSetup.setBounds(10, 118, 430, 14);
		add(lblmenuSetup);
	}
	
	
	@Override
	public void setSettingsData(String sConsumerSecret, String sDataAccessToken, String sApiVersionTargeted, Long lEmployeeID, String sEmployeeFirstName, 
			String sEmployeeLastName) {
		// Pass the information to the parent class that it needs
		super.setSettingsData(sConsumerSecret, sDataAccessToken, sApiVersionTargeted, lEmployeeID, sEmployeeFirstName, sEmployeeLastName);
		
		// TO DO: Initialize our own controls if need be
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
