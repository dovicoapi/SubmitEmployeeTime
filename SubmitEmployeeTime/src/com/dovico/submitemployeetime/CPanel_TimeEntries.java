package com.dovico.submitemployeetime;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CDatePicker;
import com.dovico.commonlibrary.data.CEmployee;
import com.dovico.commonlibrary.data.CTimeEntry;

import java.awt.Color;


public class CPanel_TimeEntries extends JPanel {
	private static final long serialVersionUID = 1L;
	
	// Will hold a reference to the UI Logic parent class
	private CCommonUILogic m_UILogic = null;
	
	private JComboBox m_ddlEmployees = null;
	private CEmployeeComboBoxModel m_EmployeeDataModel = null;	
	private JButton m_cmdDateRangeStart = null;
	private JButton m_cmdDateRangeFinish = null;
	private JTable m_tblTimeEntries = null; 
	private JPanel m_pLoading = null; 
	
	// The Start and End dates for the time entries	
	private Date m_dtDateRangeStart = null;
	private Date m_dtDateRangeEnd = null;
	
	private DefaultTableModel m_tmTimeEntryGridModel = null; 
	
	private boolean m_bHasTimeThatCanBeSubmitted = false;
	private JButton m_cmdSubmitTime = null; 

	
	// Default constructor
	public CPanel_TimeEntries(CCommonUILogic UILogic) {
		// Remember the reference to the UI Logic parent class
		m_UILogic = UILogic;
		
		// Default the date range to be today's date
		m_dtDateRangeStart = new Date();
		m_dtDateRangeEnd = new Date();
	
		setLayout(null);
		
		
		JLabel lblTimeEntriesFor = new JLabel("Time entries for");
		lblTimeEntriesFor.setFont(new Font("Arial", Font.PLAIN, 11));
		lblTimeEntriesFor.setBounds(10, 11, 82, 14);
		add(lblTimeEntriesFor);
		
		// Employee drop-down list
		m_EmployeeDataModel = new CEmployeeComboBoxModel();
		m_ddlEmployees = new JComboBox(m_EmployeeDataModel);
		m_ddlEmployees.setFont(new Font("Arial", Font.PLAIN, 11));
		m_ddlEmployees.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) { OnSelChanged_ddlEmployees(); }
		});
		m_ddlEmployees.setBounds(89, 8, 343, 20);
		add(m_ddlEmployees);
		
		JLabel lblForTheDate = new JLabel("for the date range of");
		lblForTheDate.setFont(new Font("Arial", Font.PLAIN, 11));
		lblForTheDate.setBounds(10, 40, 113, 14);
		add(lblForTheDate);
				
		m_cmdDateRangeStart = new JButton(Constants.NO_DATE_SELECTED);
		m_cmdDateRangeStart.setBounds(133, 36, 132, 23);
		m_cmdDateRangeStart.setFont(new Font("Arial", Font.PLAIN, 11));
		m_cmdDateRangeStart.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) { OnClick_cmdDateRangeStart(); } 
		});
		add(m_cmdDateRangeStart);
				
		JLabel lblTo = new JLabel("to");
		lblTo.setFont(new Font("Arial", Font.PLAIN, 11));
		lblTo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTo.setBounds(275, 40, 16, 14);
		add(lblTo);
		
		m_cmdDateRangeFinish = new JButton(Constants.NO_DATE_SELECTED);
		m_cmdDateRangeFinish.setBounds(301, 36, 131, 23);
		m_cmdDateRangeFinish.setFont(new Font("Arial", Font.PLAIN, 11));
		m_cmdDateRangeFinish.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) { OnClick_cmdDateRangeFinish(); } 
		});
		add(m_cmdDateRangeFinish);
		
		
		m_tmTimeEntryGridModel = new DefaultTableModel(getColumnNamesForTimeEntryGrid(), 0);
			
		// Time entry grid showing the time entries for the selected employee
		m_tblTimeEntries = new JTable(m_tmTimeEntryGridModel);
		m_tblTimeEntries.setFont(new Font("Arial", Font.PLAIN, 11));
		m_tblTimeEntries.setShowGrid(false);		
				
		JScrollPane spTimeEntries = new JScrollPane(m_tblTimeEntries);
		m_tblTimeEntries.setFillsViewportHeight(true);
		spTimeEntries.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		spTimeEntries.setBounds(10, 70, 696, 304);
		add(spTimeEntries);
		
		
		// Button to trigger the submit of time for the selected employee's date range
		m_cmdSubmitTime = new JButton("Submit");
		m_cmdSubmitTime.setFont(new Font("Arial", Font.PLAIN, 11));
		m_cmdSubmitTime.setBounds(617, 385, 89, 23);
		m_cmdSubmitTime.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) { OnClick_cmdSubmitTime(); } 
		});
		add(m_cmdSubmitTime);
		
		
		// Loading indicator
		m_pLoading = new JPanel();
		m_pLoading.setBackground(Color.BLUE);
		m_pLoading.setBounds(503, 11, 204, 23);
		add(m_pLoading);
		
		JLabel lblProcessingoneMomentPlease = new JLabel("Processing...One Moment Please");
		lblProcessingoneMomentPlease.setFont(new Font("Arial", Font.PLAIN, 11));
		lblProcessingoneMomentPlease.setForeground(Color.WHITE);
		m_pLoading.add(lblProcessingoneMomentPlease);
		
		JLabel lblStatusValuesAre = new JLabel("Status values are: A (approved), N (not submitted), U (under review), or R (rejected)");
		lblStatusValuesAre.setFont(new Font("Arial", Font.PLAIN, 11));
		lblStatusValuesAre.setBounds(10, 385, 427, 14);
		add(lblStatusValuesAre);
	}
	
		
	// Called when the user changes the selection in the Employee drop-down
	private void OnSelChanged_ddlEmployees() { 
		// Show the loading/processing indicator and load in the time for the selected employee that is within the selected date range
		showProcessing(true);					
		reloadTimeEntryGrid(); 
	}
	
		
	// Called when the user clicks on the Start Date Range button 
	private void OnClick_cmdDateRangeStart() {
		// Create a Calendar object so that we can grab the Year and Month index from the start date object
		Calendar calDateRangeStart = Calendar.getInstance();
		calDateRangeStart.setTime(m_dtDateRangeStart);
		
		// Create our Date Picker object (the dialog automatically displays modal)
		CDatePicker dlgDate = new CDatePicker(this.getParent(), "Date Range - Start", calDateRangeStart.get(Calendar.MONTH), calDateRangeStart.get(Calendar.YEAR));
		
		// Grab the selected date. If a date was selected then...
		Date dtSelection = dlgDate.getSelectedDate();
		if(dtSelection != null) {
			// Remember the selection and then cause the button's caption to indicate the selected date
			m_dtDateRangeStart = dtSelection;
			m_cmdDateRangeStart.setText(getCaptionFromDate(dtSelection));
			
			// Show the loading/processing indicator and load in the time for the selected employee that is within the selected date range
			showProcessing(true);
			reloadTimeEntryGrid();
		} // End if(dtSelection != null)
	}
	
	
	// Called when the user clicks on the Finish Date Range button
	private void OnClick_cmdDateRangeFinish() { 
		// Create a Calendar object so that we can grab the Year and Month index from the start date object
		Calendar calDateRangeStart = Calendar.getInstance();
		calDateRangeStart.setTime(m_dtDateRangeEnd);
		
		// Create our Date Picker object (the dialog automatically displays modal)
		CDatePicker dlgDate = new CDatePicker(this.getParent(), "Date Range - Finish", calDateRangeStart.get(Calendar.MONTH), calDateRangeStart.get(Calendar.YEAR));
		
		// Grab the selected date. If a date was selected then...
		Date dtSelection = dlgDate.getSelectedDate();
		if(dtSelection != null) {
			// Remember the selection and then cause the button's caption to indicate the selected date
			m_dtDateRangeEnd = dtSelection;
			m_cmdDateRangeFinish.setText(getCaptionFromDate(dtSelection));
			
			// Show the loading/processing indicator and load in the time for the selected employee that is within the selected date range
			showProcessing(true);
			reloadTimeEntryGrid();
		} // End if(dtSelection != null)
	}
	
	
	// Called when the user clicks on the Submit Time button
	private void OnClick_cmdSubmitTime() {
		// If no rows are in the grid then...
		if(m_tmTimeEntryGridModel.getRowCount() == 0) { JOptionPane.showMessageDialog(null, "No time to submit", "Error", JOptionPane.ERROR_MESSAGE); } 
		// There is data in the grid but none of it has a status that can be submitted (it's all Approved or Under Review time)
		else if(!m_bHasTimeThatCanBeSubmitted) { JOptionPane.showMessageDialog(null, "No un-submitted or rejected time available to submit", "Error", JOptionPane.ERROR_MESSAGE); } 
		else { // We have time and it can be submitted...			
			// Show the loading/processing indicator
			showProcessing(true);
			
			
			// Submit the selected employee's time. If successful, reload the grid to reflect the new time entry statuses...
			CEmployee eEmployee = (CEmployee)m_ddlEmployees.getSelectedItem();
			APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, true);
			if(CTimeEntry.submitTime(eEmployee.getID(), m_dtDateRangeStart, m_dtDateRangeEnd, aRequestResult)) {
				// Reload the grid so that the new statuses are reflected and then tell the user we're done
				reloadTimeEntryGrid();
				JOptionPane.showMessageDialog(null, "Done", "Done", JOptionPane.INFORMATION_MESSAGE); 
			} // End if(CTimeEntry.submitTime(eEmployee.getID(), m_dtDateRangeStart, m_dtDateRangeEnd, aRequestResult))
						
			
			// Hide the loading/processing indicator (just in case the submitTime call fails)
			showProcessing(false);
		} // End if(m_tmTimeEntryGridModel.getRowCount() == 0)
	}
	

	public void initializeControls()
	{
		// Show the loading/processing indicator
		showProcessing(true);
		
		// Make sure our list of employees is empty before we fill it with more employees
		m_EmployeeDataModel.removeAllElements();

		// Load in the employee data
		APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, true);
		m_EmployeeDataModel.loadEmployeeData(aRequestResult);

		// If there were employees loaded, cause the first item in the list to be selected by default
		if(m_EmployeeDataModel.getSize() > 0) { m_EmployeeDataModel.setSelectedItem(m_EmployeeDataModel.getElementAt(0)); }
		
		
		// Hide the loading/processing indicator
		showProcessing(false);
	}
	
	
	private void reloadTimeEntryGrid(){	
		// Make sure the grid of time entries is cleared
		while(m_tmTimeEntryGridModel.getRowCount() > 0) { m_tmTimeEntryGridModel.removeRow(0); }
		
		// Set the flag to false to indicate that there is no time available to be submitted (if we load some that is Rejected or not yet submitted, we will flip this
		// flag - used to inform the user when they click the Submit button - saves processing too since we don't have to post a submit request and reload the time
		// entries if there is nothing to submit)
		m_bHasTimeThatCanBeSubmitted = false;
		
		// If we have a valid date range selected then...
		if(ValidateDateRange(true)) {
			// Load in the selected employee's time entries
			CEmployee eEmployee = (CEmployee)m_ddlEmployees.getSelectedItem();
			APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, true);
			ArrayList<CTimeEntry> lstTimeEntries = new ArrayList<CTimeEntry>();
			loadTimeEntryData(eEmployee.getID(), m_dtDateRangeStart, m_dtDateRangeEnd, aRequestResult, lstTimeEntries);
			
			CTimeEntry aTimeEntry = null;
			String sStatus = "", sProjectTaskName = "", sStartStopTotal = "";
						
			// Loop through the time entries adding them to the grid...			
			int iCount = lstTimeEntries.size();
			for(int iIndex = 0; iIndex < iCount; iIndex++) {
				// Grab the current time entry item and the required values from the item
				aTimeEntry = lstTimeEntries.get(iIndex);
				sStatus = aTimeEntry.getSheetStatus();
				sProjectTaskName = getProjectTaskName(aTimeEntry.getClientID(), aTimeEntry.getClientName(), aTimeEntry.getProjectName(), aTimeEntry.getTaskName());
				sStartStopTotal = getStartStopTotal(aTimeEntry.getStartTime(), aTimeEntry.getStopTime(), aTimeEntry.getTotalHours());
				
				// Add the time entry data to the grid
				m_tmTimeEntryGridModel.addRow(new Object[] { sStatus, sProjectTaskName, getCaptionFromDate(aTimeEntry.getDate()), sStartStopTotal, aTimeEntry.getDescription() });
				
				// If the current entry is Not Submitted or is Rejected then it can be submitted so flip the flag 
				if(sStatus.equals(Constants.STATUS_NOT_SUBMITTED) || sStatus.equals(Constants.STATUS_REJECTED)) { m_bHasTimeThatCanBeSubmitted = true; }				
			} // End of the for(int iIndex = 0; iIndex < iCount; iIndex++) loop.
		} // End if(ValidateDateRange())
		
		
		// Hide the loading/processing indicator
		showProcessing(false);
	}
	
	
	// Helper to load in the employee's time entries within the date range specified
	private void loadTimeEntryData(Long lEmployeeID, Date dtDateRangeStart, Date dtDateRangeEnd, APIRequestResult aRequestResult, 
			ArrayList<CTimeEntry> lstReturnTimeEntries) {	
		// Get the current page of data (if the URI was not specified -first page requested- CTimeEntryUtil will set the URI for the first page of data)		
		lstReturnTimeEntries.addAll(CTimeEntry.getListForEmployee(lEmployeeID, dtDateRangeStart, dtDateRangeEnd, aRequestResult));
		
		
		// If there is a next page of data to load in then...(load it in)
		String sNextPageURI = aRequestResult.getResultNextPageURI();
		if(!sNextPageURI.equals(Constants.URI_NOT_AVAILABLE)) { 
			// Call this function again with the URI for the next page of items
			aRequestResult.setRequestURI(sNextPageURI);
			loadTimeEntryData(lEmployeeID, dtDateRangeStart, dtDateRangeEnd, aRequestResult, lstReturnTimeEntries);
		} // End if(!sNextPageURI.equals(Constants.URI_NOT_AVAILABLE))
	}
	

	
	
	
	// Returns the names for the columns in the Time Entry table/grid
	private String[] getColumnNamesForTimeEntryGrid() {
		String[] arrColNames = { "Status", "Project - Task", "Date", "Start - Stop (Total Hours)", "Description" };	
		return arrColNames;
	}
	
	
	// Helper to return a string showing the "[Client - ] Project - Task" names for display purposes 
	private String getProjectTaskName(Long lClientID, String sClientName, String sProjectName, String sTaskName){
		// Concatenate the Project and Task names together
		String sReturnVal = (sProjectName + " - " + sTaskName);
		
		// If we have a Client then add the Client name to the beginning of the string
		if(!lClientID.equals(Constants.NONE_ITEM_ID)) { sReturnVal = (sClientName + " - " + sReturnVal); }
		
		// Return the string to the caller
		return sReturnVal;
	}
	
	
	// Helper to return a string showing the "Start - Stop (Total)" values for display purposes
	private String getStartStopTotal(String sStartTime, String sStopTime, double dTotalHours){
		return (sStartTime + " - " + sStopTime + " (" + Double.toString(dTotalHours) + (dTotalHours == 1.0 ? " hr)" : " hrs)"));
	}
	
	
	// Helper function to return the text needed for a button's caption based on a date's value
	private String getCaptionFromDate(Date dtValue) {
		// Create a date formatter object and display the date as the caption on the button
		SimpleDateFormat fFormatter = new SimpleDateFormat("MMMM d, yyyy");
		return fFormatter.format(dtValue);
	}
	
	
	// Helper to make sure that a date range is selected and that the Start date is before the End date
	private boolean ValidateDateRange(boolean bDisplayErrorIfIssueExists)
	{
		boolean bValidatedOK = false;
		
		// If we have a date range selected then...
		if(!m_cmdDateRangeStart.getText().equals(Constants.NO_DATE_SELECTED) && !m_cmdDateRangeFinish.getText().equals(Constants.NO_DATE_SELECTED))
		{
			// If the dates equal each other OR the start date is before the end date then we're good...
			if(m_dtDateRangeStart.equals(m_dtDateRangeEnd) || m_dtDateRangeStart.before(m_dtDateRangeEnd)) { bValidatedOK = true; }
			else { // Start date is after the end date...
				// Only display the error if we've been asked to (not every spot that calls this wants a UI response for the user)
				if(bDisplayErrorIfIssueExists) { JOptionPane.showMessageDialog(null, "Please choose a date range where the Start date is before the End date", "Error", JOptionPane.ERROR_MESSAGE); }			
			} // End if
		} // End if(!m_cmdDateRangeStart.getText().equals(Constants.NO_DATE_SELECTED) && !m_cmdDateRangeFinish.getText().equals(Constants.NO_DATE_SELECTED))
		
		// Tell the calling function if the date range is valid or not
		return bValidatedOK;
	}
	
	
	// Helper to show/hide the processing indicator
	private void showProcessing(boolean bShow) {
		m_pLoading.setVisible(bShow);		
		m_pLoading.paintImmediately(m_pLoading.getVisibleRect()); // Otherwise, it may or may not even show up!
	}
}
