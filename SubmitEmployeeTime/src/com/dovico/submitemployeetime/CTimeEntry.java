package com.dovico.submitemployeetime;

import java.text.*;
import java.util.Date;

public class CTimeEntry {
	protected String m_sID = ""; // Could be a Guid or a Long. I don't need the actual value right now so I left it as a string
	protected String m_sStatus = "";
	protected String m_sProjectTaskName = "";
	protected Date m_dtDate = null; // Needed as a Date object rather than a string to make sorting easier 
	protected String m_sStartStopTotal = "";
	protected String m_sDescription = "";

	
	// Overloaded constructor
	public CTimeEntry(String sID, String sSheetStatus, String sProjectTaskName, String sDate, String sStartStopTotal, String sDescription) {
		m_sID = sID;
		m_sStatus = sSheetStatus;
		m_sProjectTaskName = sProjectTaskName;
		m_sStartStopTotal = sStartStopTotal;
		m_sDescription = sDescription;
		
		Format fFormatter = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
		try { m_dtDate = (Date)fFormatter.parseObject(sDate); } 
		catch (ParseException e) { e.printStackTrace(); }
	}
			
	
	public String getStatus() { return m_sStatus; }
	public String getDisplayProjectTaskName() { return m_sProjectTaskName; }
	public Date getDate() { return m_dtDate; }
	public String getDisplayStartStop() { return m_sStartStopTotal; }
	public String getDescription() { return m_sDescription; }
}
