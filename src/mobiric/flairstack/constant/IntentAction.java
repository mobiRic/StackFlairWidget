package mobiric.flairstack.constant;

import android.content.SharedPreferences;

/**
 * Contains definitions of broadcast intent actions for this app.
 */
public class IntentAction
{

	/**
	 * Defines commands that are sent to the {@link TrackerService}.
	 */
	public static class ServiceCommand
	{
		/** Commands {@link TrackerService} to start Active Tracking. */
		public static final String START = "com.mobiric.stackflairwidget.COMMAND_SERVICE_START";
		/** Commands {@link TrackerService} to stop Active Tracking. */
		public static final String STOP = "com.mobiric.stackflairwidget.COMMAND_SERVICE_STOP";

		/**
		 * Commands {@link TrackerService} to start Panic Mode Tracking. This
		 * will also result in Active Tracking being turned on.
		 */
		public static final String PANIC_MODE_START = "com.mobiric.stackflairwidget.COMMAND_PANIC_MODE_START";
		/**
		 * Commands {@link TrackerService} to stop Panic Mode Tracking. Active
		 * Tracking will remain on after this command completes.
		 */
		public static final String PANIC_MODE_STOP = "com.mobiric.stackflairwidget.COMMAND_PANIC_MODE_STOP";

		/**
		 * Commands {@link TrackerService} to check if it should be running,
		 * based on the {@link SharedPreferences}. This command is sent when the
		 * device reboots, and at regular intervals to make sure the service is
		 * not killed.
		 */
		public static final String CHECK_RESTART = "com.mobiric.stackflairwidget.COMMAND_SERVICE_CHECK_RESTART";

		/**
		 * Commands {@link TrackerService} to try and login to the server. This
		 * can be used by a periodic alarm if initial login attempts fail.
		 */
		public static final String REAUTHENTICATE = "com.mobiric.stackflairwidget.COMMAND_SERVICE_LOGIN";

		/**
		 * Commands {@link TrackerService} to sends the last known location to
		 * the server; and to respond by sending out a
		 * {@link ServiceResponse#RESPONSE_LAST_LOCATION} intent
		 * with that location.
		 * 
		 * @see #QUERY_LAST_LOCATION
		 */
		public static final String SEND_LAST_LOCATION = "com.mobiric.stackflairwidget.COMMAND_LAST_LOCATION";

		public static final String QUERY_TRACKING_STATUS = "com.mobiric.stackflairwidget.COMMAND_QUERY_TRACKING_STATUS";
		public static final String QUERY_PANIC_MODE = "com.mobiric.stackflairwidget.COMMAND_QUERY_PANIC_MODE";
		public static final String QUERY_BATTERY_MODE = "com.mobiric.stackflairwidget.COMMAND_QUERY_BATTERY_MODE";
		public static final String QUERY_SERVER_AVAILABILITY = "com.mobiric.stackflairwidget.COMMAND_QUERY_SERVER_AVAILABILITY";
		public static final String QUERY_GPS_AVAILABILITY = "com.mobiric.stackflairwidget.COMMAND_QUERY_GPS_AVAILABILITY";
		/**
		 * Queries {@link TrackerService} to send a
		 * {@link ServiceResponse#RESPONSE_LAST_LOCATION} intent
		 * with the last known location.
		 * 
		 * @see #SEND_LAST_LOCATION
		 */
		public static final String QUERY_LAST_LOCATION = "com.mobiric.stackflairwidget.COMMAND_QUERY_LAST_LOCATION";
	}

	/**
	 * Defines responses that the {@link TrackerService} can send back to the
	 * {@link HomeScreenActivity} after receiving a {@link ServiceCommand}.
	 */
	public static class ServiceResponse
	{
		/** @deprecated not used in current UI. */
		public static final String SERVICE_INITIALISING = "com.mobiric.stackflairwidget.RESPONSE_SERVICE_INITIALISING";
		public static final String SERVICE_TRACKING_ACTIVE = "com.mobiric.stackflairwidget.RESPONSE_SERVICE_TRACKING_ACTIVE";
		public static final String SERVICE_TRACKING_INACTIVE = "com.mobiric.stackflairwidget.RESPONSE_SERVICE_TRACKING_INACTIVE";

		/**
		 * Response sent by {@link TrackerService} in response to
		 * {@link ServiceCommand#SEND_LAST_LOCATION} or
		 * {@link ServiceCommand#QUERY_LAST_LOCATION}.
		 */
		public static final String RESPONSE_LAST_LOCATION = "com.mobiric.stackflairwidget.RESPONSE_LAST_LOCATION";

		public static final String RESPONSE_SEND_POSITION_MANUAL_SUCCESS = "com.mobiric.stackflairwidget.RESPONSE_SEND_POSITION_MANUAL_SUCCESS";
		public static final String RESPONSE_SEND_POSITION_MANUAL_ERROR = "com.mobiric.stackflairwidget.RESPONSE_SEND_POSITION_MANUAL_ERROR";
	}

	/**
	 * Defines IDs for web service commands & responses that are handled by the
	 * {@link WebService}.
	 */
	public static class WebService
	{
		/** Download an image. */
		public static final String IMAGE_DOWNLOAD = "com.mobiric.WS_IMAGE_DOWLOAD";
	}

	/**
	 * Defines information messages that the services can broadcast to any
	 * observers.
	 */
	public static class Info
	{
//		public static final String ERROR_LOGIN = "com.mobiric.ERROR_LOGIN";
	}

}
