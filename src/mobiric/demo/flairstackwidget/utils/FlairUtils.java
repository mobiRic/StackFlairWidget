package mobiric.demo.flairstackwidget.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lib.debug.Dbug;
import mobiric.demo.flairstackwidget.constant.IntentAction;
import mobiric.demo.flairstackwidget.constant.IntentExtra;
import mobiric.demo.flairstackwidget.service.WebService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Messenger;


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
	 * Commands the {@link WebService} to download an image for a given widget.
	 * 
	 * @param url
	 *            URL of the image to download
	 * @param appWidgetId
	 *            ID of the widget that this image applies to. This needs to be passed into the
	 *            {@link WebService} so that it can round-trip back to the update handler method.
	 * @param webserviceHandler
	 *            {@link Handler} to be used by the webservice
	 * @param context
	 *            {@link Context} to call the webservice from
	 */
	public static void startImageDownload(String url, int appWidgetId, Handler webserviceHandler,
			Context context)
	{
		Dbug.log("Widget [" + appWidgetId + "] fetching [" + url + "]");

		// create Intent to download the image
		Intent intent = new Intent(context, WebService.class);
		intent.setAction(IntentAction.WebService.IMAGE_DOWNLOAD);

		// create new Messenger for the communication back
		Messenger messenger = new Messenger(webserviceHandler);
		intent.putExtra(IntentExtra.Key.IPC_MESSENGER, messenger);

		// set url
		intent.putExtra(IntentExtra.Key.WS_IMAGE_URL, url);

		// set widget id
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		// start the service for this request
		context.startService(intent);
	}

	/**
	 * Gives the name of the cache file used for this widget's image.
	 * 
	 * @param appWidgetId
	 *            ID of the widget
	 * @return Full filename including extension
	 */
	private static String getFilename(int appWidgetId)
	{
		return appWidgetId + ".png";
	}

	/**
	 * Creates a {@link File} reference to the widget's image cache.
	 * 
	 * @param context
	 *            Context to access image file with
	 * @param appWidgetId
	 *            ID of the widget
	 * @return {@link File} used for the widget's image cache
	 */
	public static File getCacheFile(Context context, int appWidgetId)
	{
		return new File(context.getApplicationContext().getFilesDir(), getFilename(appWidgetId));
	}

	/**
	 * Saves the image a given widget in the file cache.
	 * 
	 * @param context
	 *            Context to access image file with
	 * @param appWidgetId
	 *            ID of the widget
	 * @param flair
	 *            image to cache
	 * @return <code>true</code> if file is written; <code>false</code> if there was an error
	 */
	public static boolean saveCachedImage(Context context, int appWidgetId, Bitmap flair)
	{
		try
		{
			FileOutputStream fos =
					context.getApplicationContext().openFileOutput(getFilename(appWidgetId),
							Context.MODE_PRIVATE);
			flair.compress(CompressFormat.PNG, 100, fos);
			fos.close();
		}
		catch (IOException e)
		{
			Dbug.log("Error caching bitmap.");
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Loads the image for a given widget from the file cache.
	 * 
	 * @param context
	 *            Context to access image file with
	 * @param appWidgetId
	 *            ID of the widget
	 * @return image from cache, or <code>null</code> image could not be found or read
	 */
	public static Bitmap loadCachedImage(Context context, int appWidgetId)
	{
		Bitmap flair = null;
		try
		{
			FileInputStream fis =
					context.getApplicationContext().openFileInput(getFilename(appWidgetId));
			flair = BitmapFactory.decodeStream(fis);
			fis.close();
		}
		catch (IOException e)
		{
			Dbug.log("ERROR reading cached bitmap for widget " + appWidgetId);
			e.printStackTrace();
		}

		return flair;
	}

	/**
	 * Returns the time when this image cache was last modified.
	 * 
	 * @param context
	 *            Context to access image file with
	 * @param appWidgetId
	 *            ID of the widget
	 * @return time when the cache was last modified, measured in milliseconds since January 1st,
	 *         1970, midnight (0 if the file does not exist)
	 */
	public static long getLastCachedTime(Context context, int appWidgetId)
	{
		return getCacheFile(context, appWidgetId).lastModified();
	}
}
