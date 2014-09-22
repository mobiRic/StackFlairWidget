package mobiric.demo.flairstackwidget.widget;

import java.io.File;

import mobiric.demo.flairstackwidget.service.FlairWidgetService;
import mobiric.demo.flairstackwidget.utils.FlairUtils;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


/**
 * A very basic {@link AppWidgetProvider} implementation that delegates the actual processing to the
 * {@link FlairWidgetService}.
 */
public class FlairWidgetProvider extends AppWidgetProvider
{

	/**
	 * Calls the {@link FlairWidgetService} to update the given widget.
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		for (int i : appWidgetIds)
		{
			/*
			 * AppWidgetProvider extends BroadcastReceiver, so we must not spend lots of processing
			 * time in this class. Actual processing is done in a Service so that this method can
			 * return as quickly as possible.
			 */
			context.startService(getIntentForService(context, i));
		}
	}

	/**
	 * Deletes the {@link SharedPreferences} file & cached image for the deleted widget.
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		for (int i : appWidgetIds)
		{
			// delete the SharedPreferences for this deleted widget
			SharedPreferences prefs =
					context.getSharedPreferences(String.valueOf(i), Context.MODE_PRIVATE);
			prefs.edit().clear().commit();
			File prefsFile =
					new File(context.getApplicationContext().getFilesDir().getParent()
							+ "/shared_prefs/" + i + ".xml");
			prefsFile.delete();

			// delete cached flair image
			File flairFile = FlairUtils.getCacheFile(context, i);
			flairFile.delete();
		}

		super.onDeleted(context, appWidgetIds);
	}

	/**
	 * Stops the background service when the last widget is removed.
	 */
	@Override
	public void onDisabled(Context context)
	{
		context.stopService(getIntentForService(context));

		super.onDisabled(context);
	}

	/**
	 * Helper method to create the correct {@link Intent} to use when working with the
	 * {@link FlairWidgetService}.
	 * 
	 * @param context
	 *            Context to use for the Intent
	 * @return Intent that can be used to interact with the {@link FlairWidgetService}
	 */
	private static Intent getIntentForService(Context context)
	{
		Intent widgetService =
				new Intent(context.getApplicationContext(), FlairWidgetService.class);
		return widgetService;
	}

	/**
	 * Helper method to create the correct {@link Intent} to use when working with the
	 * {@link FlairWidgetService}.
	 * 
	 * @param context
	 *            Context to use for the Intent
	 * @param appWidgetId
	 *            ID to send to the {@link FlairWidgetService}
	 * @return Intent that can be used to interact with the {@link FlairWidgetService}
	 */
	private static Intent getIntentForService(Context context, int appWidgetId)
	{
		Intent widgetService =
				new Intent(context.getApplicationContext(), FlairWidgetService.class);
		widgetService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		return widgetService;
	}

}