package com.dovico.submitemployeetime;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CRESTAPIHelper;
import com.dovico.commonlibrary.CXMLHelper;


public class CEmployee {
	private Long m_lID = null;
	private String m_sLastName = ""; // for display purposes, etc
	private String m_sFirstName = ""; // for display purposes, etc
	

	// Overloaded constructor
	public CEmployee(Long lID, String sLastName, String sFirstName) {
		m_lID = lID;
		m_sLastName = sLastName;
		m_sFirstName = sFirstName;
	}
		
		
	// Function that loads in the time entries of the current employee for a given date range and for specific statuses
	// Returns the list of time entries to the caller
	public ArrayList<CTimeEntry> getTimeEntries(String sConsumerSecret, String sDataAccessToken, Date dtStart, Date dtEnd) {
		// We want the list of time entries for a specific employee so we will use the Employee filter
		String sURIPart = ("TimeEntries/Employee/" + m_lID.toString() + "/");
		
		// Create our Date Range query string to restrict the time entries returned to just those within the date range (I manually set the
		// URI encoding for a space below - probably not the best idea)
		Format fFormatter = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
		String sDateRangeQueryString = ("daterange=" + fFormatter.format(dtStart) + "%20" + fFormatter.format(dtEnd));
		
		// Build up the full URI for the request and then ask the REST API for the data (the function will call itself again if necessary based on if there is a next
		// page of data or not)
		String sURI = CRESTAPIHelper.buildURI(sURIPart, sDateRangeQueryString, Constants.API_VERSION_TARGETED);
		ArrayList<CTimeEntry> lstTimeEntries = loadTimeEntryData(sURI, sConsumerSecret, sDataAccessToken);
		
		// Sort the list of time entries by date (time entries are returned with TempTrans first and then Trans. now that we have all of the time entries in one
		// list, we want to make future code easier for ourselves by having everything sorted by date)
		Collections.sort(lstTimeEntries, new Comparator<CTimeEntry>() { 
			public int compare(CTimeEntry t1, CTimeEntry t2) { return t1.getDate().compareTo(t2.getDate()); } 
		});
		
		// Return the time entries to the caller
		return lstTimeEntries;
	}
	
	
	// Causes the Time Entry data, for the URI specified, to be loaded in
	protected ArrayList<CTimeEntry> loadTimeEntryData(String sURI, String sConsumerSecret, String sDataAccessToken) {
		// Will hold the time entries loaded in
		ArrayList<CTimeEntry> lstTimeEntries = new ArrayList<CTimeEntry>();
		
		
		// Load in the current page of data. If there is data returned then...
		APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "GET", null, sConsumerSecret, sDataAccessToken);
		Document xdDoc = arResult.getResultDocument();
		if(xdDoc != null) {
			// Grab the root element and grab the Next Page URI (in case we need to call this function again to load the next page of data)
			Element xeDocElement = xdDoc.getDocumentElement();
			String sNextPageURI = CXMLHelper.getChildNodeValue(xeDocElement, Constants.NEXT_PAGE_URI);

			Element xeTimeEntry = null, xeNode = null;			
			String sID = "", sDate = "", sStartStopTotal = "", sDescription = "", sProjectTaskName = "", sSheetStatus = "";
			
			// Grab the list of TimeEntry nodes
			NodeList xnlTimeEntries = xeDocElement.getElementsByTagName("TimeEntry");
			int iTimeEntryCount = xnlTimeEntries.getLength();
			for(int iIndex = 0; iIndex < iTimeEntryCount; iIndex++) {
				// Clear the string from the previous loop (otherwise, might not get set if the current item does not have a client name)
				sProjectTaskName = "";				
				
				
				// Grab the current TimeEntry element and desired values that are directly children of the TimeEntry element (NOTE: The ID will have a prefix 'T' or
				// 'M'. Items with the 'T' prefix will be Guids. Items with the 'M' prefix will be 'long')
				xeTimeEntry = (Element)xnlTimeEntries.item(iIndex);
				sID = CXMLHelper.getChildNodeValue(xeTimeEntry, "ID");
				sDate = CXMLHelper.getChildNodeValue(xeTimeEntry, "Date");
				sStartStopTotal = (CXMLHelper.getChildNodeValue(xeTimeEntry, "StartTime") + " - " + CXMLHelper.getChildNodeValue(xeTimeEntry, "StopTime") + " ("+ CXMLHelper.getChildNodeValue(xeTimeEntry, "TotalHours") + " hrs)");
				sDescription = CXMLHelper.getChildNodeValue(xeTimeEntry, "Description");

				
				// Grab the Client node. If a non-[None] item is selected then grab that Client name and add it to the Project/Task string variable
				xeNode = (Element)xeTimeEntry.getElementsByTagName("Client").item(0);
				if(!CXMLHelper.getChildNodeValue(xeNode, "ID").equals(Constants.NONE_ITEM_ID)){ sProjectTaskName = (CXMLHelper.getChildNodeValue(xeNode, "Name") + " - "); }
				
				// Grab the Project node adding its name to the Project/Task string variable 
				xeNode = (Element)xeTimeEntry.getElementsByTagName("Project").item(0);
				sProjectTaskName += (CXMLHelper.getChildNodeValue(xeNode, "Name") + " - ");
				
				// Finally, grab the Task node adding its name to the Project/Task string variable 
				xeNode = (Element)xeTimeEntry.getElementsByTagName("Task").item(0);
				sProjectTaskName += (CXMLHelper.getChildNodeValue(xeNode, "Name"));
				
				
				// Grab the Sheet status so that we can let the user know if the sheet has been submitted or not and if so if it is approved or still under review
				xeNode = (Element)xeTimeEntry.getElementsByTagName("Sheet").item(0);
				sSheetStatus =  CXMLHelper.getChildNodeValue(xeNode, "Status");
								
				
				// Add the current Time Entry item to our list
				lstTimeEntries.add(new CTimeEntry(sID, sSheetStatus, sProjectTaskName, sDate, sStartStopTotal, sDescription));
			} // End of the for(int iIndex = 0; iIndex < iTimeEntryCount; iIndex++) loop.
			
			
			// If there is yet another page of time entry data to load then load it in too.
			if(!sNextPageURI.equals(Constants.URI_NOT_AVAILABLE)) { loadTimeEntryData(sNextPageURI, sConsumerSecret, sDataAccessToken); }		
		} // End if(xeDocElement != null)
		
		
		// Return the time entries to the caller
		return lstTimeEntries;
	}
	
	
	// Helper to submit the employee's time for the specified date range
	public boolean submitTime(String sConsumerSecret, String sDataAccessToken, Date dtStart, Date dtEnd) {
		// Build up the URI for the Employee ID we want to submit time for
		String sURIPart = ("TimeEntries/Employee/" + m_lID.toString() + "/Submit/");
				
		// Create our Date Range query string to restrict the time entries submitted to just those within the date range (I manually set the
		// URI encoding for a space below - probably not the best idea)
		Format fFormatter = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
		String sDateRangeQueryString = ("daterange=" + fFormatter.format(dtStart) + "%20" + fFormatter.format(dtEnd));
				
		// Build up the full URI for the request and then ask the REST API for the data (the function will call itself again if necessary based on if there is a next
		// page of data or not)
		String sURI = CRESTAPIHelper.buildURI(sURIPart, sDateRangeQueryString, Constants.API_VERSION_TARGETED);
		
		
		// Fire off the request. Tell the calling function that the request failed if an error was displayed. Otherwise, the request was successful
		APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "POST", "<SubmitTime></SubmitTime>", sConsumerSecret, sDataAccessToken);
		return (arResult.getDisplayedError() ? false : true);		
	}
	
	
	@Override
	public String toString() { return (m_sLastName + ", " + m_sFirstName); }
}
