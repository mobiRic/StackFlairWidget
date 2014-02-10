package mobiric.flairstack.constant;

import android.app.AlarmManager;

public class FlairSettings
{

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class Key
	{
		/** Stores the user ID. */
		public static final String USER = "user";
		/** Stores the website / account for the given user. */
		public static final String ACCOUNT = "account";
		/** Stores the theme for the Flair Widget. */
		public static final String THEME = "theme";
		/**
		 * If the app is unable to login to the server, it will keep trying
		 * periodically. These retries are triggered by the {@link AlarmManager}
		 * , and this key holds the time that the next alarm will fire.
		 */
		public static final String REAUTH_ALARM_TIME = "REAUTH_ALARM_TIME";

		/** Stores which activity the user returns to via the notification. */
		public static final String CURRENT_ACTIVITY = "CURRENT_ACTIVITY";

	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class Value
	{
		//		public static final String GPS_ON = "GPS_ON";
		//		public static final String GPS_OFF = "GPS_OFF";
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

}
