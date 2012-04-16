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

import com.dovico.commonlibrary.CDatePicker;


//Custom editor that allows us to edit a Date field in a JTable
public class CTableCellEditorDatePicker extends AbstractCellEditor implements TableCellEditor, ActionListener {
	private static final long serialVersionUID = 1L;
		
	private CDate m_dtDateSelected = null;
	private JButton m_cmdButton = null; 
	private CDatePicker m_dlgDialog = null;
	protected static final String EDIT = "edit"; // So we know that it's our edit button calling and not the date picker
	

	// Constructor
	public CTableCellEditorDatePicker(Component cParent){
		m_cmdButton = new JButton();
		m_cmdButton.setActionCommand(EDIT);
		m_cmdButton.addActionListener(this);
		m_cmdButton.setFont(new Font("Arial", Font.PLAIN, 11));
		m_cmdButton.setHorizontalAlignment(SwingConstants.LEFT);// Have the contents left-aligned to match the text alignment of the grid
	
		// Pass in null for the date since we won't know what the value is until getTableCellEditorComponent is called
		m_dlgDialog = new CDatePicker(cParent, this, "Date", null);
	}
	
	public void actionPerformed(ActionEvent e) {
		// If the user clicked on the cell in the grid then....
		if (EDIT.equals(e.getActionCommand())) {
            // Show the Date picker pop-up
        	m_dlgDialog.setVisible(true);
            fireEditingStopped(); //Make the renderer reappear.
        } else { //User selected a task in the Date picker...
        	m_dtDateSelected = new CDate(m_dlgDialog.getSelectedDate());
        	//m_cmdButton.setText(m_dtDateSelected.toString());// Set the button's text so that it is the proper text the next time the user clicks on the cell (if you change the selection and then click and hold on the cell again, the old text would appear until you released the mouse button...suggests the button is shown before this function gets called)
        } // End if
    }
	
	
	// This method is called when a cell value is edited by the user.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
    	// Grab the value passed in, tell the date picker what the Date value is, adjust the button's text to reflect the selected date, and return the grid our button
    	m_dtDateSelected = (CDate)value;
    	m_dlgDialog.setDate(m_dtDateSelected.getDate());
    	m_cmdButton.setText(m_dtDateSelected.toString());
        return m_cmdButton;
    }

    
    // This method is called when editing is completed. It must return the new value to be stored in the cell.
    public Object getCellEditorValue() { return m_dtDateSelected; }
}
