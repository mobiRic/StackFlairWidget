package com.mobiric.stackflairwidget.constant;


/**
 * Defines constants for working with the web services i.e. parameter names, api
 * methods etc.
 */
public class WSConstants
{

	/**
	 * Defines result constants for the web service calls.
	 * 
	 * @see Result#OK
	 * @see Result#FAILURE_GENERAL
	 * @see Result#FAILURE_UNAUTHORIZED
	 */
	public static class Result
	{
		public static final int OK = -1;
		public static final int FAILURE_GENERAL = 1;
		public static final int FAILURE_UNAUTHORIZED = 401;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

}
