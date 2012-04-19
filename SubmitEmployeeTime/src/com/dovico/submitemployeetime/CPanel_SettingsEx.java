package com.dovico.submitemployeetime;

import java.awt.*;
import javax.swing.*;

import com.dovico.commonlibrary.CPanel_Settings;


public class CPanel_SettingsEx extends CPanel_Settings {
	private static final long serialVersionUID = 1L;
	
	private JLabel m_lblLine1 = null;
	private JLabel m_lblLine2 = null;
	private JLabel m_lblLine3 = null;
	
	// Default constructor
	public CPanel_SettingsEx() {
		// Call the parent class's constructor (handles setting up the controls) 
		super();
		
		m_lblLine1 = new JLabel("This App requires administrator API tokens from DOVICO Timesheet\u2122 or DOVICO Planning and Timesheet\u2122");
		m_lblLine1.setHorizontalAlignment(SwingConstants.CENTER);
		m_lblLine1.setFont(new Font("Arial", Font.PLAIN, 11));
		m_lblLine1.setBounds(10, 84, 698, 14);
		add(m_lblLine1);
		
		m_lblLine2 = new JLabel("via Database Options (Menu > Setup > Database Options > API)");
		m_lblLine2.setHorizontalAlignment(SwingConstants.CENTER);
		m_lblLine2.setFont(new Font("Arial", Font.PLAIN, 11));
		m_lblLine2.setBounds(10, 101, 698, 14);
		add(m_lblLine2);
		
		m_lblLine3 = new JLabel("");
		m_lblLine3.setHorizontalAlignment(SwingConstants.CENTER);
		m_lblLine3.setFont(new Font("Arial", Font.PLAIN, 11));
		m_lblLine3.setBounds(10, 118, 698, 14);
		add(m_lblLine3);
	}
	
	
	@Override
	public void setSettingsData(String sConsumerSecret, String sDataAccessToken, String sApiVersionTargeted, Long lEmployeeID, String sEmployeeFirstName, 
			String sEmployeeLastName) {
		// We will hide the Consumer Secret field if the constant for the token is not an empty string. Pass the proper consumer secret value to our parent class
		// if the constant was specified. If not, use the token that was last saved by the user.
		boolean bHideConsumerSecretField = !Constants.CONSUMER_SECRET_API_TOKEN.isEmpty();
		String sConsumerSecretToUse = (bHideConsumerSecretField ? Constants.CONSUMER_SECRET_API_TOKEN : sConsumerSecret);
		
		// Pass the information to the parent class that it needs
		super.setSettingsData(sConsumerSecretToUse, sDataAccessToken, sApiVersionTargeted, lEmployeeID, sEmployeeFirstName, sEmployeeLastName, bHideConsumerSecretField);
		
		
		// If we hid the Consumer Secret field then...
		if(bHideConsumerSecretField){
			// We don't need to tell the user where to find the Admin tokens, we need to tell the user where to find the Data Access Token (we only need to adjust
			// the text in lines 1 and 3 - line 2 is fine)
			m_lblLine1.setText("This App requires a Data Access Token from DOVICO Timesheet\u2122 or DOVICO Planning and Timesheet\u2122");
			m_lblLine3.setText("or via My Time & Expenses / Options (Menu > Views > My Time & Expenses > Options)");
			
			// Let's also tweak the vertical position of the controls to place them a bit higher on the page since the Consumer Secret field is not present
			m_lblLine1.setBounds(10, (84 - 30), 698, 14);
			m_lblLine2.setBounds(10, (101 - 30), 698, 14);
			m_lblLine3.setBounds(10, (118 - 30), 698, 14);
		} // End if(bHideConsumerSecretField)
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
