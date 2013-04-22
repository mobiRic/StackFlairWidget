package com.mobiric.stackflairwidget.constant;

import android.os.Bundle;
import android.os.Messenger;

import com.mobiric.stackflairwidget.service.WebService;

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
		 * Used in {@link WebService} for the {@link Messenger} object
		 * that communicates back to the caller.
		 */
		public static final String IPC_MESSENGER = "IPC_MESSENGER";
		/** Passes the Image Download URL to the {@link WebService}. */
		public static final String WS_IMAGE_URL = "WS_IMAGE_URL";
	}

	/**
	 * Defines some values that can be passed to an intent as part of the extras
	 * {@link Bundle}.
	 */
	public static class Value
	{
		//		public static final String EVENT_LOW_BATTERY = "LowInternalBattery";
	}

}
