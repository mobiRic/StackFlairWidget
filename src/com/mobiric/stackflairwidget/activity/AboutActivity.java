package com.mobiric.stackflairwidget.activity;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.mobiric.debug.Dbug;
import com.mobiric.lib.ipc.StaticSafeHandler;
import com.mobiric.stackflairwidget.R;
import com.mobiric.stackflairwidget.constant.IntentAction;
import com.mobiric.stackflairwidget.constant.WSConstants;
import com.mobiric.stackflairwidget.service.WebService;
import com.mobiric.stackflairwidget.utils.FlairUtils;

public class AboutActivity extends Activity implements Handler.Callback
{
	private static final long A_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final long CACHE_REFRESH_TIMEOUT = 14 * A_DAY_IN_MILLIS;

	/**
	 * ID used for the flair image on this {@link Activity}.
	 */
	private static final int ABOUT_FLAIR_ID = 0;

	/**
	 * Handler for the result to come back to this service. The {@link WebService} and this
	 * {@link StaticSafeHandler} both need to know the exact parameters passed back in the
	 * {@link Message} .
	 */
	private static Handler webserviceHandler;
	ImageView ivDevProfile;
	int ivDevHeight;
	int ivDevWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setTitle(R.string.title_about);

		webserviceHandler = new StaticSafeHandler(this);

		// set cached image before downloading a new one
		final Bitmap cachedFlair = FlairUtils.loadCachedImage(this, ABOUT_FLAIR_ID);
		ivDevProfile = (ImageView) findViewById(R.id.imageView2);
		ivDevProfile.post(new Runnable()
		{
			public void run()
			{
				ivDevHeight = ivDevProfile.getHeight();
				ivDevWidth = ivDevProfile.getWidth();

				// display cached image
				if ((cachedFlair != null) && (ivDevProfile != null))
				{
					ivDevProfile.setImageBitmap(cachedFlair);
				}

				// refresh image
				long now = new Date().getTime();
				long lastUpdated =
						FlairUtils.getLastCachedTime(getApplicationContext(), ABOUT_FLAIR_ID);
				if (CACHE_REFRESH_TIMEOUT < (now - lastUpdated))
				{
					FlairUtils.startImageDownload(FlairUtils.getFlairDownloadUrl(
							getString(R.string.defaultAccount), getString(R.string.defaultUser),
							getString(R.string.defaultTheme)), 0, webserviceHandler,
							AboutActivity.this);
				}
			}
		});
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
				Bitmap bmpFlair =
						Bitmap.createScaledBitmap((Bitmap) message.obj, ivDevWidth, ivDevHeight,
								true);
				FlairUtils.saveCachedImage(this, 0, bmpFlair);
				ivDevProfile.setImageBitmap(bmpFlair);
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

	/**
	 * Launch the browser to my Stack Overflow profile.
	 */
	public void onClickDevProfile(View v)
	{
		Uri uri = Uri.parse("http://stackoverflow.com/users/383414/richard-le-mesurier");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}


}
