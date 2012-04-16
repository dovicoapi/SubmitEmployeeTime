package com.dovico.submitemployeetime;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import com.dovico.commonlibrary.CAssignmentPicker;
import com.dovico.commonlibrary.data.CAssignment;


// Custom editor that allows us to edit a Project/Task field in a JTable
public class CTableCellEditorAssignmentPicker extends AbstractCellEditor implements TableCellEditor, ActionListener {
	private static final long serialVersionUID = 1L;
	
	private CAssignment m_aAssignmentSelected = null;
	private JButton m_cmdButton = null; 
	private CAssignmentPicker m_dlgDialog = null;
	protected static final String EDIT = "edit"; // So we know that it's our edit button calling and not the assignment picker 
	

	// Constructor
	public CTableCellEditorAssignmentPicker(Component cParent){
		m_cmdButton = new JButton();
		m_cmdButton.setActionCommand(EDIT);
		m_cmdButton.addActionListener(this);
		m_cmdButton.setFont(new Font("Arial", Font.PLAIN, 11));
		m_cmdButton.setHorizontalAlignment(SwingConstants.LEFT);// Have the contents left-aligned to match the text alignment of the grid
				
		m_dlgDialog = new CAssignmentPicker(cParent, this);
	}
	
	
	// Since we're dealing with multiple employees, each time an employee selection is changed, we want to tell the tree picker to reload with the new employee's data 
	public void loadAssignmentsForEmployee(Long lEmployeeID, String sConsumerSecret, String sDataAccessToken, String sApiVersionTargeted){ 
		m_dlgDialog.loadAssignmentsForEmployee(lEmployeeID, sConsumerSecret, sDataAccessToken, sApiVersionTargeted); 
	}
	
	
	public void actionPerformed(ActionEvent e) {
		// If the user clicked on the cell in the grid then....
		if (EDIT.equals(e.getActionCommand())) {
            //Show the Project/Task picker pop-up       	
        	m_dlgDialog.setVisible(true);
            fireEditingStopped(); //Make the renderer reappear.
        } else { //User selected a task in the Project/Task picker...
        	m_aAssignmentSelected = m_dlgDialog.getSelectedItem();
        } // End if
    }
	
	
	// This method is called when a cell value is edited by the user.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
    	// Grab the value passed in, adjust the button's text to reflect the selected assignment, and return the grid our button
    	m_aAssignmentSelected = (CAssignment)value;
    	m_cmdButton.setText(m_aAssignmentSelected.toString());
        return m_cmdButton;
    }
    
    
    // This method is called when editing is completed. It must return the new value to be stored in the cell.
    public Object getCellEditorValue() { return m_aAssignmentSelected; }
}
