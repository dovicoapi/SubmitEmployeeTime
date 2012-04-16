package com.dovico.submitemployeetime;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CDatePicker;
import com.dovico.commonlibrary.data.CAssignment;
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
	private CDatePicker m_dlgDatePicker = null; // The date picker that will be used by the From/To date range buttons (saves processing if you don't have to keep re-creating the thing) 
	private JTable m_tblTimeEntries = null; 
	private JPanel m_pLoading = null; 
	
	// The Start and End dates for the time entries	
	private Date m_dtDateRangeStart = null;
	private Date m_dtDateRangeEnd = null;
	
	private DefaultTableModel m_tmTimeEntryGridModel = null; 
	private CTableCellEditorAssignmentPicker m_ProjectTaskCellEditor = null;
	private CTableCellEditorDatePicker m_DateCellEditor = null;
	
	private boolean m_bGridLoading = false;
	private boolean m_bHasTimeThatCanBeSubmitted = false;
	private JButton m_cmdSubmitTime = null; 

	
	// Default constructor
	public CPanel_TimeEntries(CCommonUILogic UILogic) {
		// Remember the reference to the UI Logic parent class
		m_UILogic = UILogic;
		
		// Default the date range to be today's date
		m_dtDateRangeStart = new Date();
		m_dtDateRangeEnd = new Date();
		
		// Objects that will be used when the user clicks into the Project/Task and Date columns of the grid (allows the user to change the Project/Task and Date
		// selections of a record)
		m_ProjectTaskCellEditor = new CTableCellEditorAssignmentPicker(this.getParent());
		m_DateCellEditor = new CTableCellEditorDatePicker(this.getParent());
	
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
		
		
		// Make sure the time entry grid's table model is constructed with the necessary column information
		buildTableModelAndCreateTable();
		
		// Time entry grid showing the time entries for the selected employee
		m_tblTimeEntries.setFont(new Font("Arial", Font.PLAIN, 11));
		m_tblTimeEntries.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 11));
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
		
		
		JLabel lblCanEdit = new JLabel("* You can edit the cells of the column indicated");
		lblCanEdit.setFont(new Font("Arial", Font.PLAIN, 11));
		lblCanEdit.setBounds(10, 385, 232, 14);
		add(lblCanEdit);
	}
	
		
	// Called when the user changes the selection in the Employee drop-down
	private void OnSelChanged_ddlEmployees() { 
		// Show the loading/processing indicator and load in the time for the selected employee that is within the selected date range
		showProcessing(true);					
		reloadTimeEntryGrid(); 
	}
	
		
	// Called when the user clicks on the Start Date Range button 
	private void OnClick_cmdDateRangeStart() {
		// Show the date picker and get the selected date. If a date was selected then...
		Date dtSelection = showDatePicker("Date Range - Start", m_dtDateRangeStart);
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
		// Show the date picker and get the selected date. If a date was selected then...
		Date dtSelection = showDatePicker("Date Range - Finish", m_dtDateRangeEnd);
		if(dtSelection != null) {
			// Remember the selection and then cause the button's caption to indicate the selected date
			m_dtDateRangeEnd = dtSelection;
			m_cmdDateRangeFinish.setText(getCaptionFromDate(dtSelection));
			
			// Show the loading/processing indicator and load in the time for the selected employee that is within the selected date range
			showProcessing(true);
			reloadTimeEntryGrid();
		} // End if(dtSelection != null)
	}
	
	
	// Helper for creating and displaying the date picker control for the Start/Finish date range buttons
	private Date showDatePicker(String sTitle, Date dtDate){
		// Create our Date Picker dialog if it doesn't exist (don't bother setting the title or date in the constructor since we're just going to set them a couple
		// lines down anyway)
		if(m_dlgDatePicker == null) { m_dlgDatePicker = new CDatePicker(this.getParent(), null, "", null); }
		
		// Adjust the title and the date 
		m_dlgDatePicker.setTitle(sTitle);
		m_dlgDatePicker.setDate(dtDate);
		
		// Display the dialog and when the dialog is closed, return the selected date 
		m_dlgDatePicker.setVisible(true);
		return m_dlgDatePicker.getSelectedDate();
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
	

	// View is loaded initially or when user is coming back from another tab (settings for example)
	public void initializeControls()
	{
		// Show the loading/processing indicator
		showProcessing(true);
		
		// Make sure our list of employees and grid are empty before we fill the employee list with more employees
		m_EmployeeDataModel.removeAllElements();
		clearTimeEntryGrid();

		// Load in the employee data (we pass in the logged in employee's information in the event he/she does not have read access to Employees so that the drop-down
		// defaults to them - We also indicate to aRequestResult that we don't want it to display an error...loadEmployeeData is going to customize it if the request 
		// fails)
		APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, false);
		m_EmployeeDataModel.loadEmployeeData(m_UILogic.getEmployeeID(), m_UILogic.getEmployeeLastName(), m_UILogic.getEmployeeFirstName(), aRequestResult);

		// If there were employees loaded, cause the first item in the list to be selected by default
		if(m_EmployeeDataModel.getSize() > 0) { m_EmployeeDataModel.setSelectedItem(m_EmployeeDataModel.getElementAt(0)); }
		
		
		// Hide the loading/processing indicator
		showProcessing(false);
	}
	
	
	// Empties the grid of its rows
	private void clearTimeEntryGrid() {
		// Flag that the grid is being adjusted programmatically and not by the user directly (don't want things trying to save right now)
		m_bGridLoading = true;
		
		// Clear the grid of its contents
		while(m_tmTimeEntryGridModel.getRowCount() > 0) { m_tmTimeEntryGridModel.removeRow(0); } 
			
		// Flag that we are no longer adjusting the grid programmatically
		m_bGridLoading = false;
	}
	
	
	// Reloads the time entries into the grid (employee selection changed, date range changed, etc)
	private void reloadTimeEntryGrid(){	
		// Make sure the grid of time entries is cleared
		clearTimeEntryGrid();

		// Flag that the grid is being adjusted programmatically and not by the user directly (don't want things trying to save right now). NOTE: Do not set this flag
		// before the clearTimeEntryGrid call because that function will set this flag to false when completed.
		m_bGridLoading = true;
		
		// Set the flag to false to indicate that there is no time available to be submitted (if we load some that is Rejected or not yet submitted, we will flip this
		// flag - used to inform the user when they click the Submit button - saves processing too since we don't have to post a submit request and reload the time
		// entries if there is nothing to submit)
		m_bHasTimeThatCanBeSubmitted = false;
		
		// If we have a valid date range selected then...
		if(ValidateDateRange(true)) {
			// Grab the selected employee and the ID
			CEmployee eEmployee = (CEmployee)m_ddlEmployees.getSelectedItem();
			Long lEmployeeID = eEmployee.getID();
			String sConsumerSecret = m_UILogic.getConsumerSecret();
			String sDataAccessToken = m_UILogic.getDataAccessToken();
			String sApiVersionTargeted = Constants.API_VERSION_TARGETED;
			
			// Load in the selected employee's time entries			
			APIRequestResult aRequestResult = new APIRequestResult(sConsumerSecret, sDataAccessToken, sApiVersionTargeted, true);
			ArrayList<CTimeEntry> lstTimeEntries = new ArrayList<CTimeEntry>();
			loadTimeEntryData(lEmployeeID, m_dtDateRangeStart, m_dtDateRangeEnd, aRequestResult, lstTimeEntries);
			
			CTimeEntry aTimeEntry = null;
			String sStatus = "", sStartStopTotal = "";
						
			// Loop through the time entries adding them to the grid...			
			int iCount = lstTimeEntries.size();
			for(int iIndex = 0; iIndex < iCount; iIndex++) {
				// Grab the current time entry item and the required values from the item
				aTimeEntry = lstTimeEntries.get(iIndex);
				sStatus = aTimeEntry.getSheetStatus();
				sStartStopTotal = getStartStopTotal(aTimeEntry.getStartTime(), aTimeEntry.getStopTime(), aTimeEntry.getTotalHours());
				
				// Add the time entry data to the grid
				m_tmTimeEntryGridModel.addRow(new Object[] { 
						aTimeEntry, // This will be a hidden column
						getStatusNameFromStatus(sStatus), 
						new CAssignment(aTimeEntry.getClientID(), aTimeEntry.getClientName(), aTimeEntry.getProjectID(), aTimeEntry.getProjectName(), 0L, "", aTimeEntry.getTaskID(), aTimeEntry.getTaskName(), 0L, ""), 
						new CDate(aTimeEntry.getDate()), 
						sStartStopTotal, 
						aTimeEntry.getDescription() 
						});
								
				// If the current entry is Not Submitted or is Rejected then it can be submitted so flip the flag 
				if(sStatus.equals(Constants.STATUS_NOT_SUBMITTED) || sStatus.equals(Constants.STATUS_REJECTED)) { m_bHasTimeThatCanBeSubmitted = true; }				
			} // End of the for(int iIndex = 0; iIndex < iCount; iIndex++) loop.
			
			
			// Tell the Project/Task picker of the new employee
			m_ProjectTaskCellEditor.loadAssignmentsForEmployee(lEmployeeID, sConsumerSecret, sDataAccessToken, sApiVersionTargeted);
		} // End if(ValidateDateRange())
		
		
		// Flag that we are no longer adjusting the grid programmatically
		m_bGridLoading = false;
		
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
	
	
	// Helper to set up the JTable for editing
	private void buildTableModelAndCreateTable() {
		// Make sure the time entry grid's table model is constructed with the necessary column information and set up a listener for when the table's content is
		// updated
		m_tmTimeEntryGridModel = new DefaultTableModel(getColumnNamesForTimeEntryGrid(), 0);
		m_tmTimeEntryGridModel.addTableModelListener(new TableModelListener() { 
			// Fired when content in the grid changes (rows are added based on the employee/date range selected, content is changed due to the use of the project/task
			// picker, etc)
			public void tableChanged(TableModelEvent e) {
				// We only want to run our code as a result of the user modifying the grid. If the grid is not being loaded/cleared then...
				if(!m_bGridLoading){
					// Grab the Row and Column that are selected as well as the table model (in the event there are multiple tables?)
					int iSelRow = e.getFirstRow();
			        int iSelCol = e.getColumn(); // Index based on the Model
			        TableModel tmModel = (TableModel)e.getSource();

			        // Grab the TimeEntry object from the 1st column in the grid (it's hidden)
			        CTimeEntry aTimeEntry = (CTimeEntry)tmModel.getValueAt(iSelRow, 0);
			        
			        // If the user modified the Project/Task column then...
			        if(iSelCol == 2) {
			        	// Grab the assignment object from the cell
			        	CAssignment aAssignment = (CAssignment)tmModel.getValueAt(iSelRow, iSelCol);
			        	Long lNewProjectID = aAssignment.getProjectID();
			        	Long lNewTaskID = aAssignment.getTaskID();
			        	
			        	// We don't need to request a save if the Project/Task has not actually been changed. If either IDs differ then...
			        	if(!aTimeEntry.getProjectID().equals(lNewProjectID) || !aTimeEntry.getTaskID().equals(lNewTaskID)) {
			        		// Show the loading/processing indicator
			        		showProcessing(true);
			        		
							// Update the time entry with the new Project/Task IDs. The total hours are passed too because it's a required field when doing an
			        		// update/PUT (we pass in the Start/Stop values -HHMM format- because we want to maintain the Start/Stop times. When only passing in a
			        		// total/duration, the time entry will be fit into the first available slot which is not something I want happening.)
				        	APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, true);
				        	CTimeEntry aSavedTimeEntry = CTimeEntry.doUpdate(aTimeEntry.getTimeEntryID(), lNewProjectID, lNewTaskID, null, null, aTimeEntry.getStartTime(), aTimeEntry.getStopTime(), aTimeEntry.getTotalHours(), null, aRequestResult);
				        	
				        	// If the save was successful then put the returned time entry object back into the grid so that the grid has the most up-to-date version
				        	// of the object
				        	if(aSavedTimeEntry != null) { tmModel.setValueAt(aSavedTimeEntry, iSelRow, 0); } 
				        	else { // There was a problem with the save... 
				        		// Revert the Assignment (Client, Project, Task Group, and Task) values
				        		aAssignment = new CAssignment(aTimeEntry.getClientID(), aTimeEntry.getClientName(), aTimeEntry.getProjectID(), aTimeEntry.getProjectName(), 0L, "", aTimeEntry.getTaskID(), aTimeEntry.getTaskName(), 0L, "");
				        		tmModel.setValueAt(aAssignment, iSelRow, iSelCol);
				        	} // End if(aSavedTimeEntry != null)
				        	
				        	// Hide the loading/processing indicator
				    		showProcessing(false);			        		
			        	} // End if(!aTimeEntry.getProjectID().equals(lNewProjectID) || !aTimeEntry.getTaskID().equals(lNewTaskID))
			        }
			        // If the user modified the Date column then...
			        else if(iSelCol == 3) {
			        	// Grab the CDate object from the cell and from that grab the Date object itself
			        	CDate aDate = (CDate)tmModel.getValueAt(iSelRow, iSelCol);
			        	Date dtDate = aDate.getDate();
			        	
			        	// We don't need to request a save if the Date has not actually changed. If the dates differ then...
			        	if(!aTimeEntry.getDate().equals(dtDate)) {
			        		// Show the loading/processing indicator
			        		showProcessing(true);
			        		
							// Update the time entry with the new Date. The total hours are passed too because it's a required field when doing an update/PUT (we pass 
			        		// in the Start/Stop values -HHMM format- because we want to maintain the Start/Stop times. When only passing in a total/duration, the 
			        		// time entry will be fit into the first available slot which is not something I want happening)
				        	APIRequestResult aRequestResult = new APIRequestResult(m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken(), Constants.API_VERSION_TARGETED, true);
				        	CTimeEntry aSavedTimeEntry = CTimeEntry.doUpdate(aTimeEntry.getTimeEntryID(), null, null, null, dtDate, aTimeEntry.getStartTime(), aTimeEntry.getStopTime(), aTimeEntry.getTotalHours(), null, aRequestResult);
				        	
				        	// If the save was successful then put the returned time entry object back into the grid so that the grid has the most up-to-date version
				        	// of the object (the user will be shown the error and null is returned if there was an error returned from the API)
				        	if(aSavedTimeEntry != null) { tmModel.setValueAt(aSavedTimeEntry, iSelRow, 0); } 
				        	// There was a problem with the save so revert the Date value
				        	else { tmModel.setValueAt(new CDate(aTimeEntry.getDate()), iSelRow, iSelCol); }
				        	
				        	// Hide the loading/processing indicator
				    		showProcessing(false);
			        	} // End if(!aTimeEntry.getDate().equals(dtDate))
			        } // End if
				} // End if(!m_bGridLoading)
			}
		});
		
		
		// Create the JTable object
		m_tblTimeEntries = new JTable(m_tmTimeEntryGridModel){
			private static final long serialVersionUID = 1L;

			// Function that tells the grid if the cell can be edited or not
			public boolean isCellEditable(int iRowIndex, int iColIndex) {
				// NOTE:	The column index received is based on the displayed columns in the table which differs from the columns in the model - in the model the ID 
				// 			column is still present which results in two different sets of indexes. 1 here refers to the table's Project/Task column, 2 is the Date
				// 			column.
				//
				// If this is not the Project/Task or Date column then we are not going to allow the edit
				if((iColIndex != 1) && (iColIndex != 2)) { return false; }
				
				// If the row is not Under Review then we are allowed to edit (we don't allow an edit of an under review item simply to reduce processing/network 
				// requests since it will be blocked by the API anyway)
				String sStatus = (String)m_tmTimeEntryGridModel.getValueAt(iRowIndex, 1);
				if(!sStatus.equals(Constants.STATUS_FULLNAME_UNDER_REVIEW)) { return true; }

				// We're not allowing the edit of the selected cell				
				return false;
			}
		};
		
		
		// Grab the column model and then set up the special project/task editor on the 3rd column (Project - Task) and 4th column (Date) 
		TableColumnModel tmColumModel = m_tblTimeEntries.getColumnModel(); 
		tmColumModel.getColumn(2).setCellEditor(m_ProjectTaskCellEditor);
		tmColumModel.getColumn(3).setCellEditor(m_DateCellEditor);
		
		// Hide the 1st column (ID). NOTE: The column is only removed from the grid, not from the model (Be Careful! The table itself will see a different column 
		// index value than the model since the two are not in sync anymore!)
		m_tblTimeEntries.removeColumn(tmColumModel.getColumn(0));
	}


	
	// Returns the names for the columns in the Time Entry table/grid
	private String[] getColumnNamesForTimeEntryGrid() {
		String[] arrColNames = { "TimeEntryObjectData", "Status", "* Project - Task", "* Date", "Start - Stop (Total Hours)", "Description" };	
		return arrColNames;
	}
	
	
	// Helper to return a string showing the "Start - Stop (Total)" values for display purposes
	private String getStartStopTotal(String sStartTime, String sStopTime, double dTotalHours){
		return (sStartTime + " - " + sStopTime + " (" + Double.toString(dTotalHours) + (dTotalHours == 1.0 ? " hr)" : " hrs)"));
	}
	
	
	// Helper to return the proper text to display for the status returned by the API
	private String getStatusNameFromStatus(String sStatus) {
		if(sStatus.equals(Constants.STATUS_APPROVED)) { return Constants.STATUS_FULLNAME_APPROVED; }
		else if(sStatus.equals(Constants.STATUS_NOT_SUBMITTED)) { return Constants.STATUS_FULLNAME_NOT_SUBMITTED; }
		else if(sStatus.equals(Constants.STATUS_UNDER_REVIEW)) { return Constants.STATUS_FULLNAME_UNDER_REVIEW; }
		else { return Constants.STATUS_FULLNAME_REJECTED; } // Only one status left (STATUS_REJECTED)
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
