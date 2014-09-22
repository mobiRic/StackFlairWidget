package mobiric.demo.flairstackwidget.constant;

import mobiric.demo.flairstackwidget.service.FlairWidgetService;
import mobiric.demo.flairstackwidget.service.WebService;
import mobiric.demo.flairstackwidget.widget.FlairWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.os.Messenger;


/**
 * Contains definitions of broadcast intent extras for this app.
 */
public class IntentExtra
{

	/**
	 * Defines keys that identify extra info in the intent {@link Bundle}.
	 */
	public static class Key
	{
		/**
		 * Do not use this to pass a widget ID. There is a predefined value in the SDK to use.
		 * 
		 * @deprecated use {@link AppWidgetManager#EXTRA_APPWIDGET_ID} instead
		 */
		@SuppressWarnings("unused")
		private static final String APP_WIDGET_ID = "APP_WIDGET_ID";
		/**
		 * Used to send an image to {@link FlairWidgetService}.
		 */
		public static final String FLAIR_IMAGE = "FLAIR_IMAGE";
		/**
		 * Used in {@link WebService} for the {@link Messenger} object that communicates back to the
		 * caller.
		 */
		public static final String IPC_MESSENGER = "IPC_MESSENGER";
		/** Passes the Image Download URL to the {@link WebService}. */
		public static final String WS_IMAGE_URL = "WS_IMAGE_URL";
	}

	/**
	 * Defines some values that can be passed to an intent as part of the extras {@link Bundle}.
	 */
	public static class Value
	{
		/**
		 * Do not use this to identify that no widget ID is passed. There is a predefined value in
		 * the SDK to use.
		 * 
		 * @deprecated use {@link AppWidgetManager#INVALID_APPWIDGET_ID} instead
		 */
		@SuppressWarnings("unused")
		private static final int APP_WIDGET_NONE_SELECTED = AppWidgetManager.INVALID_APPWIDGET_ID;
	}

}
