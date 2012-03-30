package com.dovico.submitemployeetime;

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.data.CEmployee;


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
	
	
	
	// Main method to load in the requested page of Employee data from the REST API	
	public void loadEmployeeData(APIRequestResult aRequestResult) {	
		// Get the current page of data (if the URI was not specified -first page requested- CEmployeeUtil will set the URI for the first page of employee data)
		m_lstEmployees.addAll(CEmployee.getList(aRequestResult));		
		
		// If the next page URI is 'N/A' then we're done loading employees into the list.
		String sNextPageURI = aRequestResult.getResultNextPageURI(); 
		if(sNextPageURI.equals(Constants.URI_NOT_AVAILABLE)) {
			// Tell the subscribed listeners that the content of the list has changed.
			updateListenersAboutContentChange();
		} else { // There are more employees to load in...
			// Call this function again with the URI for the next page of items
			aRequestResult.setRequestURI(sNextPageURI);
			loadEmployeeData(aRequestResult);
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
