// TODO This whole activity needs to be redone as a nice custom view
package mobiric.demo.flairstackwidget.activity;

import lib.debug.Dbug;
import lib.ipc.StaticSafeHandler;
import mobiric.demo.flairstackwidget.R;
import mobiric.demo.flairstackwidget.constant.IntentAction;
import mobiric.demo.flairstackwidget.constant.IntentExtra;
import mobiric.demo.flairstackwidget.constant.WSConstants;
import mobiric.demo.flairstackwidget.preference.ImageViewPreference;
import mobiric.demo.flairstackwidget.service.FlairWidgetService;
import mobiric.demo.flairstackwidget.service.WebService;
import mobiric.demo.flairstackwidget.utils.FlairUtils;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design:
 * Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more
 * information on developing a Settings UI.
 * 
 * @deprecated Create a much nicer version of this screen!
 */
public class SettingsActivity extends PreferenceActivity implements Handler.Callback
{
	/**
	 * Handler for the result to come back to this service. The {@link WebService} and this
	 * {@link StaticSafeHandler} both need to know the exact parameters passed back in the
	 * {@link Message} .
	 */
	private static Handler webserviceHandler;

	private EditTextPreference prefUser;
	private ListPreference prefAccount;
	private ListPreference prefTheme;
	private ImageViewPreference prefFlairImage;

	Bitmap cachedFlair;

	private int appWidgetId;
	private volatile boolean initialised = false;

	/**
	 * Result of this {@link SettingsActivity}. This is required for this activity to be used in the
	 * configuration of a new widget. This intent is initialised with {@link Activity#RESULT_OK}.
	 */
	private Intent resultValue;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		webserviceHandler = new StaticSafeHandler(this);
		appWidgetId =
				getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);

		// initialise result as ok
		resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, resultValue);

		// fail if no widget id
		if (AppWidgetManager.INVALID_APPWIDGET_ID == appWidgetId)
		{
			setResult(RESULT_CANCELED, resultValue);
			finish();
		}

		// hack the default shared preferences file to use the one for this widget
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(String.valueOf(appWidgetId));
		prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
	}

	/**
	 * Used to hack the built in screen layout with the image.
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();

		// retrieve cached image after activity has started
		if (cachedFlair == null)
		{
			cachedFlair = FlairUtils.loadCachedImage(this, appWidgetId);
		}
		findViewById(android.R.id.content).post(new Runnable()
		{
			public void run()
			{
				// display cached image
				if ((cachedFlair != null) && (prefFlairImage != null))
				{
					prefFlairImage.setBitmap(cachedFlair);
				}

				// bind preferences & update image
				bindPreferenceSummaryToValue(findPreference("user"));
				bindPreferenceSummaryToValue(findPreference("account"));
				bindPreferenceSummaryToValue(findPreference("theme"));

				initialised = true;
			}
		});
	}

	/**
	 * Saves the cached flair bitmap on rotation.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable("NON", cachedFlair);
	}

	/**
	 * Restores the cached flair bitmap on rotation.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state)
	{
		super.onRestoreInstanceState(state);
		cachedFlair = state.getParcelable("NON");
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		updateWidget(appWidgetId, null);
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the device configuration
	 * dictates that a simplified, single-pane UI should be shown.
	 */
	private void setupSimplePreferencesScreen()
	{
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);
		prefUser = (EditTextPreference) findPreference("user");
		prefAccount = (ListPreference) findPreference("account");
		prefTheme = (ListPreference) findPreference("theme");

		// Add example Flair image
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_flair);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_flair_image);
		prefFlairImage = (ImageViewPreference) findPreference("flair");
	}

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new
	 * value.
	 */
	private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
			new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference preference, Object value)
				{
					String stringValue = value.toString();

					// update preference summaries
					if (preference instanceof ListPreference)
					{
						// For list preferences, look up the correct display value in
						// the preference's 'entries' list.
						ListPreference listPreference = (ListPreference) preference;
						int index = listPreference.findIndexOfValue(stringValue);

						// Set the summary to reflect the new value.
						preference.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

					}
					else
					{
						// For all other preferences, set the summary to the value's
						// simple string representation.
						preference.setSummary(stringValue);
					}

					// get new image
					if ((initialised) && (prefUser != null) && (prefAccount != null)
							&& (prefTheme != null) && (prefUser.getSummary() != null)
							&& (prefAccount.getSummary() != null)
							&& (prefTheme.getSummary() != null))
					{
						if ((prefUser == preference) || (prefAccount == preference)
								|| (prefTheme == preference))
						{
							// get old values
							String user = prefUser.getText();
							String website = prefAccount.getValue();
							String theme = prefTheme.getValue();

							// get new value
							if (prefUser == preference)
							{
								user = stringValue;
							}
							if (prefAccount == preference)
							{
								website = stringValue;
							}
							if (prefTheme == preference)
							{
								theme = stringValue;
							}

							// TODO abstract image download to be triggered by an Intent
							FlairUtils.startImageDownload(
									FlairUtils.getFlairDownloadUrl(website, user, theme),
									appWidgetId, webserviceHandler, SettingsActivity.this);
						}
					}

					return true;
				}
			};

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void updateWidget(int appWidgetId, Bitmap bmpFlair)
	{
		Intent serviceIntent = new Intent(this, FlairWidgetService.class);
		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		if (bmpFlair != null)
		{
			serviceIntent.putExtra(IntentExtra.Key.FLAIR_IMAGE, bmpFlair);
		}
		startService(serviceIntent);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is
	 * changed, its summary (line of text below the preference title) is updated to reflect the
	 * value. The summary is also immediately updated upon calling this method. The exact display
	 * format is dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private void bindPreferenceSummaryToValue(Preference preference)
	{
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, getPreferenceManager()
				.getSharedPreferences().getString(preference.getKey(), ""));
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Needs to know which parameters are passed back in which predefined fields of the
	 * {@link Message}. </p>
	 * <ul>
	 * <li><code>what</code> - hash of the related {@link IntentAction.WebService} constant</li>
	 * <li><code>arg1</code> - one of the constants declared in {@link WSConstants.Result}</li>
	 * <li><code>arg2</code> - 1 if Server was available, 0 if not available</li>
	 * </ul>
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
				Bitmap bmpFlair = (Bitmap) message.obj;
				cachedFlair = bmpFlair;
				prefFlairImage.setBitmap(bmpFlair);

				// TODO abstract widget update to be triggered by an Intent
				// update widget
				FlairUtils.saveCachedImage(this, appWidgetId, bmpFlair);
				updateWidget(appWidgetId, bmpFlair);
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


}
