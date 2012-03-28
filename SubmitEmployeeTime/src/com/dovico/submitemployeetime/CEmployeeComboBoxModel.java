package com.dovico.submitemployeetime;

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CRESTAPIHelper;
import com.dovico.commonlibrary.CXMLHelper;



// We create a subclass of ListModel so that this object can be used directly in the JList object
public class CEmployeeComboBoxModel implements ComboBoxModel {
	// Holds the list of listeners (the JList control only redraws if something changes. this listener is how we tell the list that something changed)
	protected ArrayList<ListDataListener> m_lstListeners = null;
		
	// Holds the list of Employees that are currently loaded
	protected ArrayList<CEmployee> m_lstEmployees = null;
	protected CEmployee m_eSelectedEmployee = null;
	

	// Constructor
	public CEmployeeComboBoxModel() {
		m_lstListeners = new ArrayList<ListDataListener>();
		m_lstEmployees = new ArrayList<CEmployee>(); 
	}
	
	
	// Returns the URI needed for the first page of data. After this, the Previous/Next Page URIs will be used.
	public String getURIForFirstPage() { return CRESTAPIHelper.buildURI("Employees/", "", Constants.API_VERSION_TARGETED); }
	
	
	// Main method to load in the requested page of Employee data from the REST API	
	public void loadEmployeeData(String sURI, String sConsumerSecret, String sDataAccessToken) {	
		// Ask the REST API for the page of data and grab the Next Page URI from the root node. If no data was returned then exit now.
		APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "GET", null, sConsumerSecret, sDataAccessToken);
		Document xdDoc = arResult.getResultDocument();
		if(xdDoc == null) { return; }
		
		
		Element xeEmployee = null;
		String sLastName = "", sFirstName = "";
		long lID = 0;
		
		// Grab the root element and get the NextPageURI from it
		Element xeDocElement = xdDoc.getDocumentElement();
		String sNextPageURI = CXMLHelper.getChildNodeValue(xeDocElement, Constants.NEXT_PAGE_URI, Constants.URI_NOT_AVAILABLE);
				
		// Grab the list of Employee nodes and loop through the employees...
		NodeList xnlEmployees = xeDocElement.getElementsByTagName("Employee");
		int iEmployeeCount = xnlEmployees.getLength();
		for(int iIndex = 0; iIndex < iEmployeeCount; iIndex++) {
			// Grab the current Employee element
			xeEmployee = (Element)xnlEmployees.item(iIndex);
			
			// Grab the values that we're interested in (NOTE: The resource representation for GET 'Employee/Me/' was modified to only return the ID, LastName, 
			// FirstName, and GetItemURI. We could grab the GetItemURI and try to get the additional information - WorkDays and Hours - which might not work if the 
			// user token doesn't have Employee access. If you choose to try and load additional data, specify defaults in case the values you want are not present.)
			lID = Long.valueOf(CXMLHelper.getChildNodeValue(xeEmployee, "ID"));
			sLastName = CXMLHelper.getChildNodeValue(xeEmployee, "LastName");
			sFirstName = CXMLHelper.getChildNodeValue(xeEmployee, "FirstName");			
			
			// Add the current employee item to our list
			m_lstEmployees.add(new CEmployee(lID, sLastName, sFirstName));
		} // End of the for(int iIndex = 0; iIndex < iEmployeeCount; iIndex++) loop.
		
		
		// If the next page URI is 'N/A' then we're done loading employees into the list. 
		if(sNextPageURI.equals(Constants.URI_NOT_AVAILABLE)) {
			// Tell the subscribed listeners that the content of the list has changed.
			updateListenersAboutContentChange();
		} else { // There are more employees to load in...
			// Call this function again with the URI for the next page of employee items
			loadEmployeeData(sNextPageURI, sConsumerSecret, sDataAccessToken);
		} // End if(sNextPageURI.equals(Constants.URI_NOT_AVAILABLE))
	}
	
	
	// Tells the subscribed listeners that the content of the list has changed.
	protected void updateListenersAboutContentChange() {
		// Loop through the list of listeners telling them that the contents of the list have changed (the following is the Java version of a foreach loop)
		for(ListDataListener ldlListener : m_lstListeners) { 
			ldlListener.contentsChanged(new ListDataEvent(m_lstEmployees, ListDataEvent.CONTENTS_CHANGED, 0, getSize())); 
		} // End of the for(ListDataListener ldlListener : m_lstListeners) loop.
	}
	
	
	// Clear our employee list
	public void removeAllElements() {
		m_lstEmployees.clear();
		
		// Tell the subscribed listeners that the content of the list has changed.
		updateListenersAboutContentChange();
	}
	
	
	// Necessary if we wish to subclass the ListModel object
	public Object getElementAt(int iIndex) { return m_lstEmployees.get(iIndex); }
	public int getSize() { return m_lstEmployees.size(); }	
	public void addListDataListener(ListDataListener l) { m_lstListeners.add(l); }
	public void removeListDataListener(ListDataListener l) { m_lstListeners.remove(l); }


	// Might be null (need to call setSelectedItem when initializing this object if you want something shown selected by default)
	@Override
	public Object getSelectedItem() { return m_eSelectedEmployee; }

	// Remember the selected employee object passed in
	@Override
	public void setSelectedItem(Object objEmployee) { m_eSelectedEmployee = (CEmployee)objEmployee; }
}
