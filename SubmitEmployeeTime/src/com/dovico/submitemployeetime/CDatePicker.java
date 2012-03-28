package com.dovico.submitemployeetime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// Not perfect but it works. We now have a date picker control :)
//
// Future item: Have some way to jump to a Month or Year (drop-downs perhaps). For now, we only have the Previous/Next buttons to cycle through the months
public class CDatePicker {
	JDialog m_dlgSelf = null;	
	JLabel m_lblMonthYear = null;
	JButton[] m_arrMonthDayButtons = new JButton[42];
	
	int m_iMonth = 0, m_iYear = 0; // Passed in via the constructor
	String m_sSelectedDay = "";
	
	
	// The constructor simply lays out the controls (NOTE: iMonth needs to be zero-based. e.g. January is 0 rather than 1)
	public CDatePicker(Component parent, String sTitle, int iMonth, int iYear){
		// Remember the Month and Year values passed in
		m_iMonth = iMonth;
		m_iYear = iYear;		
			
		int iWidth= 330;
		
		// Create our date picker window, give it the specified title and indicate that it is to be a modal window
		m_dlgSelf = new JDialog();
		m_dlgSelf.setTitle(sTitle);
		m_dlgSelf.setSize(iWidth, 290);//width, height
		m_dlgSelf.setResizable(false);
		m_dlgSelf.setModal(true);
	
		// Main container within the dialog to allow for us to set up a margin so the controls are not jammed against the edge of the form (setBorder call) 
		JPanel pContent = new JPanel(new BorderLayout(0, 0));
		pContent.setBorder(new EmptyBorder(5, 5, 5, 5));
				
		
		// The Month row (previous month button, label of current month/year, next month button) 
		JPanel pMonthRow = new JPanel(new BorderLayout(0, 0));
		pMonthRow.setBorder(new EmptyBorder(2, 2, 2, 2)); // A bit of a border so the controls on this row have a bit of a margin
		pMonthRow.setBackground(new Color(220, 220, 220));

		JButton cmdPreviousMonth = new JButton("<");
		cmdPreviousMonth.setBackground(new Color(220, 220, 220));
		cmdPreviousMonth.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent ae) { onClick_cmdPreviousMonth(); }
		});
		pMonthRow.add(cmdPreviousMonth, BorderLayout.WEST);
		
		m_lblMonthYear = new JLabel("", JLabel.CENTER);
		m_lblMonthYear.setFont(new Font("Arial", Font.BOLD, 12));// Slightly bigger font than the rest to stand out a bit
		pMonthRow.add(m_lblMonthYear, BorderLayout.CENTER);
		
		JButton cmdNextMonth = new JButton(">");
		cmdNextMonth.setBackground(new Color(220, 220, 220));
		cmdNextMonth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) { onClick_cmdNextMonth(); }
        });
		pMonthRow.add(cmdNextMonth, BorderLayout.EAST);
		
		
		// Day of the Week row (filled with the appropriate labels for each day)		
		JPanel pDayOfWeekRow = new JPanel(new GridLayout(1, 7));
		String[] arrDayOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		int iIndex = 0;
		for(iIndex = 0; iIndex < 7; iIndex++) {
			JLabel lblDay = new JLabel(arrDayOfWeek[iIndex], JLabel.CENTER);
			lblDay.setFont(new Font("Arial", Font.BOLD, 11));
			pDayOfWeekRow.add(lblDay); 
		} // End of the for(iIndex = 0; iIndex < 7; iIndex++) loop.
		
		
		// Month's days
		JPanel pMonthDaysRow = new JPanel(new GridLayout(6, 7));
		pMonthDaysRow.setBorder(new EmptyBorder(2, 2, 2, 2)); // Just so that the controls line up with the Prev/Next buttons above
		pMonthDaysRow.setPreferredSize(new Dimension(iWidth, 200));
		
		// Loop through adding in all of the buttons for the calendar days (no text yet. that gets set in the displayDate function)
		int iButtons = m_arrMonthDayButtons.length;
		for (iIndex = 0; iIndex < iButtons; iIndex++) {
			final int iSelection = iIndex;
			
			// Create the current button and add a click event
	        m_arrMonthDayButtons[iIndex] = new JButton();
	        m_arrMonthDayButtons[iIndex].addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent ae) { onClick_cmdMonth(iSelection); }
	        });
	        
	        // Add the current button to the panel
	        pMonthDaysRow.add(m_arrMonthDayButtons[iIndex]);
		} // End of the for (iIndex = 0; iIndex < iButtons; iIndex++) loop.
		
		
		
		// Add the 3 sections to the content panel and then add the content panel to the dialog
		pContent.add(pMonthRow, BorderLayout.NORTH);
		pContent.add(pDayOfWeekRow, BorderLayout.CENTER);
		pContent.add(pMonthDaysRow, BorderLayout.SOUTH);
		m_dlgSelf.add(pContent, BorderLayout.CENTER);
	
		// Adjust where this dialog is displayed, adjust the date controls according to the specified date, and then show this dialog
        m_dlgSelf.setLocationRelativeTo(parent);
		displayDate();
		m_dlgSelf.setVisible(true);
	}
	
	
    // Adjust the controls to reflect the month's days
    public void displayDate() {
    	// Get a Calendar instance for the first day of the month we're to show   	
        Calendar calFirstDayOfMonth = Calendar.getInstance();
        calFirstDayOfMonth.set(m_iYear, m_iMonth, 1);
        
        // Determine which day of the week the first day of the month lands on. Also get the number of days in the current month 
        int iFirstDayOfWeek = (calFirstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1); // -1 because iIndex is 0-based and this was 1-based
        int iDaysInMonth = calFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        int iIndex = 0, iCurrentDay = 0, iButtons = m_arrMonthDayButtons.length;
      
        // Loop through all of the buttons...        
        for (iIndex = 0; iIndex < iButtons; iIndex++) {
        	String sButtonCaption = "";
        	boolean bShowButton = false;
        	
        	// If the current index is within this month's days and we haven't exceeded the number of days in the month then...
        	if((iIndex >= iFirstDayOfWeek) && (iCurrentDay < iDaysInMonth)) {
        		// This day is to be displayed. Make sure the button is visible in the event the user changed months.
        		iCurrentDay++;
        		sButtonCaption = Integer.toString(iCurrentDay);
        		bShowButton = true;        		
        	} // End if((iIndex >= iFirstDayOfWeek) && (iCurrentDay < iDaysInMonth))
        	        	
        	// Adjust the current button's text and visibility
        	m_arrMonthDayButtons[iIndex].setText(sButtonCaption);
        	m_arrMonthDayButtons[iIndex].setVisible(bShowButton); 
        } // End of the for (iIndex = 0; iCurrentDay < iButtons; iIndex++) loop.
        
        
        // Display the current month and year in the dialog's Month/Year label
        SimpleDateFormat fFormatter = new SimpleDateFormat("MMMM yyyy");
        m_lblMonthYear.setText(fFormatter.format(calFirstDayOfMonth.getTime()));
    }

    
    // User clicked on the Previous month button
 	private void onClick_cmdPreviousMonth(){
 		// Adjust the month index backwards. If we are currently at January, adjust the index to be December and decrement the Year index
 		if(m_iMonth == 0) { m_iMonth = 11; m_iYear--; }
 		else { m_iMonth--; }		
 		
 		// Display the new month's days
         displayDate();	
 	}

 	
 	// User clicked on the Next month button
 	private void onClick_cmdNextMonth() {
 		// Adjust the month index forwards. If we are currently at December, adjust the index to be January and increment the Year index
 		if(m_iMonth == 11) { m_iMonth = 0; m_iYear++; }
 		else { m_iMonth++; }
 		
 		// Display the new month's days
 	    displayDate();
 	}
 	
 	
    // User clicked on one of the month's Day buttons
    private void onClick_cmdMonth(int iSelection) {
    	m_sSelectedDay = m_arrMonthDayButtons[iSelection].getActionCommand();
    	m_dlgSelf.dispose();
    }
    
    
    // NOTE: if no selection was made, returns null. Otherwise, returns the selected date. 
    public Date getSelectedDate() {
    	// If no date was selected, just return an empty string
	    if (m_sSelectedDay.equals("")) { return null; }
	    
	    // Create a Calendar object for the current year, month, and selected day and then return the Date object to the caller (NOTE: If you do NOT specify the
	    // Hour, Minute, Second to 0, it gets the current time which can really throw off date comparisons!)
	    Calendar calSelectedDay = Calendar.getInstance();
	    calSelectedDay.set(m_iYear, m_iMonth, Integer.parseInt(m_sSelectedDay), 0, 0, 0);
	    return calSelectedDay.getTime();
    }
}
