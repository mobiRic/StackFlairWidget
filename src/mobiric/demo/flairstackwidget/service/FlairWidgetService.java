package mobiric.demo.flairstackwidget.service;

import lib.debug.Dbug;
import lib.ipc.StaticSafeHandler;
import mobiric.demo.flairstackwidget.R;
import mobiric.demo.flairstackwidget.activity.SettingsActivity;
import mobiric.demo.flairstackwidget.constant.FlairSettings;
import mobiric.demo.flairstackwidget.constant.IntentAction;
import mobiric.demo.flairstackwidget.constant.IntentExtra;
import mobiric.demo.flairstackwidget.constant.WSConstants;
import mobiric.demo.flairstackwidget.utils.FlairUtils;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

/**
 * Service that provides background processing in order to update the info on the widget.
 */
public class FlairWidgetService extends Service implements Handler.Callback
{
	/**
	 * Handler for the result to come back to this service. The {@link WebService} and this
	 * {@link StaticSafeHandler} both need to know the exact parameters passed back in the
	 * {@link Message} .
	 */
	private static Handler webserviceHandler;

	String ipAddress;
	boolean wifiConnected = false;
	Thread widgetUpdateThread;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// get widget ID
		int appWidgetId =
				intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);

		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			// get widget image
			// TODO rather pass image as a file per http://stackoverflow.com/a/4352194/383414
			Bitmap bmpFlair = intent.getParcelableExtra(IntentExtra.Key.FLAIR_IMAGE);

			if (bmpFlair != null)
			{
				// update widget with provided image
				widgetUpdateThread =
						new WidgetUpdateThread(getApplicationContext(), appWidgetId, bmpFlair);
				widgetUpdateThread.start();
			}
			else
			{
				// check for cached image
				Bitmap cachedFlair = FlairUtils.loadCachedImage(this, appWidgetId);

				if (cachedFlair == null)
				{
					Dbug.log("No cached image for widget " + appWidgetId);
				}

				// initialise the widget with a click event and cached image
				updateWidget(appWidgetId, cachedFlair);

				webserviceHandler = new StaticSafeHandler(this);

				widgetUpdateThread = new WidgetUpdateThread(getApplicationContext(), appWidgetId);
				widgetUpdateThread.start();
			}
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	/**
	 * @deprecated Binding not allowed from a widget - use the Command Pattern instead.
	 */
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Callback method from the {@link StaticSafeHandler}, that handles the {@link Message} sent
	 * from the {@link WebService}. </p>
	 * 
	 * Needs to know which parameters are passed back in which predefined fields of the
	 * {@link Message}. </p>
	 * <ul>
	 * <li><code>what</code> - hash of the related {@link IntentAction.WebService} constant</li>
	 * <li><code>arg1</code> - one of the constants declared in {@link WSConstants.Result}</li>
	 * <li><code>arg2</code> - widget ID</li>
	 * <li><code>obj</code> - the returned {@link Bitmap}</li>
	 * </ul>
	 * 
	 * @param msg
	 *            {@link Message} sent from the {@link WebService}
	 * @return <code>true</code> always
	 */
	@Override
	public boolean handleMessage(Message message)
	{
		// get info from message
		int actionHash = message.what;

		/* IMAGE_DOWNLOAD */
		if (IntentAction.WebService.IMAGE_DOWNLOAD.hashCode() == actionHash)
		{
			if (message.arg1 == WSConstants.Result.OK)
			{
				// download success - update image
				if (message.arg2 != AppWidgetManager.INVALID_APPWIDGET_ID)
				{
					Bitmap bmpFlair = (Bitmap) message.obj;
					FlairUtils.saveCachedImage(this, message.arg2, bmpFlair);
					updateWidget(message.arg2, bmpFlair);
				}
			}
			else
			{
				// download error
				Dbug.log("WebService IMAGE_DOWNLOAD -> ERROR");
			}
		}

		return true;
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class WidgetUpdateThread extends Thread
	{
		Context context;
		/** ID of the widget to be updated. */
		int appWidgetId;
		/** Bitmap to update the widget. */
		Bitmap bmpFlair;

		/**
		 * Create a thread to update a given widget ID, based on preferences stored for that widget.
		 * 
		 * @param context
		 *            Required to retrieve {@link SharedPreferences}
		 * @param appWidgetId
		 *            ID of widget to update
		 */
		WidgetUpdateThread(Context context, int appWidgetId)
		{
			this(context, appWidgetId, null);
		}

		/**
		 * Create a thread to update a given widget ID with a specific image.
		 * 
		 * @param context
		 *            Required to retrieve {@link SharedPreferences}
		 * @param appWidgetId
		 *            ID of widget to update
		 * @param bmpFlair
		 *            Bitmap to use to update widget; if <code>null</code> thread will attempt to
		 *            download an image based on widget preferences
		 */
		WidgetUpdateThread(Context context, int appWidgetId, Bitmap bmpFlair)
		{
			this.context = context;
			this.appWidgetId = appWidgetId;
			this.bmpFlair = bmpFlair;
		}

		@Override
		public void run()
		{
			// check for ID
			if (AppWidgetManager.INVALID_APPWIDGET_ID == appWidgetId)
			{
				return;
			}

			// check for bitmap
			if (bmpFlair != null)
			{
				updateWidget(appWidgetId, bmpFlair);
				return;
			}

			// download bitmap based on preferences
			SharedPreferences prefs =
					context.getSharedPreferences(String.valueOf(appWidgetId), Context.MODE_PRIVATE);
			String user =
					prefs.getString(FlairSettings.Key.USER, context.getString(R.string.defaultUser));
			String account =
					prefs.getString(FlairSettings.Key.ACCOUNT,
							context.getString(R.string.defaultAccount));
			String theme =
					prefs.getString(FlairSettings.Key.THEME,
							context.getString(R.string.defaultTheme));

			// update image
			String flairDownloadUrl = FlairUtils.getFlairDownloadUrl(account, user, theme);
			FlairUtils
					.startImageDownload(flairDownloadUrl, appWidgetId, webserviceHandler, context);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Updates a widget with an image.
	 * 
	 * @param appWidgetId
	 *            ID of widget to update
	 * @param flair
	 *            image to update; if <code>null</code> then widget will be updated with a click
	 *            action to open the {@link SettingsActivity}
	 */
	public void updateWidget(int appWidgetId, Bitmap flair)
	{
		try
		{
			// set remote views
			RemoteViews widgetUi = new RemoteViews(getPackageName(), R.layout.widget);
			if (flair != null)
			{
				widgetUi.setImageViewBitmap(R.id.ivFlair, flair);
			}

			// open Settings on click
			Intent flairSettings = new Intent(this, SettingsActivity.class);
			flairSettings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntentIp =
					PendingIntent.getActivity(this, appWidgetId, flairSettings,
							PendingIntent.FLAG_UPDATE_CURRENT);
			widgetUi.setOnClickPendingIntent(R.id.ivFlair, pendingIntentIp);

			/* UPDATE THE WIDGET INSTANCE */
			try
			{
				AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
				widgetManager.updateAppWidget(appWidgetId, widgetUi);
			}
			catch (Exception e)
			{
				Dbug.log("Failed to update widget");
			}
		}
		catch (Exception e)
		{
			Dbug.log("Failed to update widget");
		}
		finally
		{
			// clean up
			FlairWidgetService.this.stopSelf();
		}
	}

}
