package com.mobiric.stackflairwidget.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;

import com.mobiric.debug.Dbug;
import com.mobiric.stackflairwidget.constant.IntentAction;
import com.mobiric.stackflairwidget.constant.IntentExtra;
import com.mobiric.stackflairwidget.service.WebService;

/**
 * Utility class for StackFlair specific functions.
 */
public class FlairUtils
{

	/**
	 * Builds up the correct download for a given StackExchange site, user and theme.
	 */
	public static String getFlairDownloadUrl(String website, String user, String theme)
	{
		return "http://" + website + "/users/flair/" + user + ".png?theme=" + theme;
	}

	/**
	 * Commands the {@link WebService} to download an image.
	 * 
	 * @param url
	 *            URL of the image to download
	 * @param webserviceHandler
	 *            {@link Handler} to be used by the webservice
	 * @param context
	 *            {@link Context} to call the webservice from
	 */
	public static void startImageDownload(String url, Handler webserviceHandler, Context context)
	{
		Dbug.log("Flair: " + url);


		// create Intent to send the LOGIN command
		Intent intent = new Intent(context, WebService.class);
		intent.setAction(IntentAction.WebService.IMAGE_DOWNLOAD);

		// create new Messenger for the communication back
		Messenger messenger = new Messenger(webserviceHandler);
		intent.putExtra(IntentExtra.Key.IPC_MESSENGER, messenger);

		// set url
		intent.putExtra(IntentExtra.Key.WS_IMAGE_URL, url);

		// start the service for this request
		context.startService(intent);
	}


}
