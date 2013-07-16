package com.mobiric.stackflairwidget.service;

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
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.mobiric.debug.Dbug;
import com.mobiric.lib.ipc.StaticSafeHandler;
import com.mobiric.stackflairwidget.R;
import com.mobiric.stackflairwidget.activity.SettingsActivity;
import com.mobiric.stackflairwidget.constant.FlairSettings;
import com.mobiric.stackflairwidget.constant.IntentAction;
import com.mobiric.stackflairwidget.constant.IntentExtra;
import com.mobiric.stackflairwidget.constant.WSConstants;
import com.mobiric.stackflairwidget.utils.FlairUtils;

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
		webserviceHandler = new StaticSafeHandler(this);

		widgetUpdateThread = new WidgetUpdateThread(getApplicationContext(), intent);
		widgetUpdateThread.start();

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
				if (message.arg2 != IntentExtra.Value.APP_WIDGET_NONE_SELECTED)
				{
					updateWidget(message.arg2, (Bitmap) message.obj);
				}
			}
			else
			{
				// download error
				Dbug.log("WebService IMAGE_DOWLOAD -> ERROR");
			}
		}

		return true;
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class WidgetUpdateThread extends Thread
	{
		Context context;
		/** Contains info about the widget to be updated. */
		Intent intent;

		WidgetUpdateThread(Context context, Intent intent)
		{
			this.context = context;
			this.intent = intent;
		}

		@Override
		public void run()
		{
			// which widget
			int appWidgetId =
					intent.getIntExtra(IntentExtra.Key.APP_WIDGET_ID,
							IntentExtra.Value.APP_WIDGET_NONE_SELECTED);
			if (appWidgetId == IntentExtra.Value.APP_WIDGET_NONE_SELECTED)
			{
				return;
			}

			// retrieve preferences
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

			// TODO get saved image & set it before starting download

			FlairUtils.startImageDownload(FlairUtils.getFlairDownloadUrl(account, user, theme),
					appWidgetId, webserviceHandler, context);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void updateWidget(int appWidgetId, Bitmap flair)
	{
		try
		{
			// set remote views
			RemoteViews widgetUi = new RemoteViews(getPackageName(), R.layout.widget);
			widgetUi.setImageViewBitmap(R.id.ivFlair, flair);

			// open Settings on click
			Intent flairSettings = new Intent(this, SettingsActivity.class);
			flairSettings.putExtra(IntentExtra.Key.APP_WIDGET_ID, appWidgetId);
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
