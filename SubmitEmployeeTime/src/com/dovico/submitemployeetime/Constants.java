package com.dovico.submitemployeetime;

public class Constants {
	// Keys for storing/caching data
	public static String PREFS_KEY_CONSUMER_SECRET = "ConsumerSecret";
	public static String PREFS_KEY_USER_TOKEN = "UserToken";
	public static String PREFS_KEY_EMPLOYEE_ID = "EmployeeID";
	public static String PREFS_KEY_EMPLOYEE_FIRST = "EmployeeFirstName";
	public static String PREFS_KEY_EMPLOYEE_LAST = "EmployeeLastName";
	
	public static String NEXT_PAGE_URI = "NextPageURI";
	public static String URI_NOT_AVAILABLE = "N/A"; 

	// Employee ID if the user provides the Data Access Token from the DB Options view of DOVICO
	public static Long ADMIN_TOKEN_EMPLOYEE_ID = 99L;
	
	public static Long NONE_ITEM_ID = 0L; 
	
	// The REST API returns and expects dates in this format
	public static String XML_DATE_FORMAT = "yyyy-MM-dd";
	
	// The REST API version that we are targeting
	public static String API_VERSION_TARGETED = "2";
	
	public static String NO_DATE_SELECTED = "[Not Selected]";
	
	
	// The possible statuses from the API 
	public static String STATUS_APPROVED = "A";
	public static String STATUS_NOT_SUBMITTED = "N";
	public static String STATUS_UNDER_REVIEW = "U";	
	public static String STATUS_REJECTED = "R";	
	
	// The easier to read versions of the statuses for display purposes
	public static String STATUS_FULLNAME_APPROVED = "Approved";
	public static String STATUS_FULLNAME_NOT_SUBMITTED = "Not Submitted";
	public static String STATUS_FULLNAME_UNDER_REVIEW = "Under Review";
	public static String STATUS_FULLNAME_REJECTED = "Rejected";	
}
