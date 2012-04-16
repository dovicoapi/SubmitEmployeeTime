package com.dovico.submitemployeetime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CDate {
	private Date m_dtValue = null;
	private SimpleDateFormat m_fDisplayFormatter = null;
	
	public CDate(Date dtValue) {
		// Create a date formatter object
		m_fDisplayFormatter = new SimpleDateFormat("MMMM d, yyyy");

		// Remember the date passed in
		m_dtValue = dtValue;		
	}

	
	// Returns the date value held
	public Date getDate(){ return m_dtValue; }
		
	// Helpful function so that you can use this object in things like a list or button (this will be the text displayed)
	@Override
	public String toString() { return m_fDisplayFormatter.format(m_dtValue); }
}
