package com.mobiric.stackflairwidget.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;

import com.mobiric.debug.Dbug;
import com.mobiric.stackflairwidget.constant.IntentAction;
import com.mobiric.stackflairwidget.constant.IntentExtra;
import com.mobiric.stackflairwidget.constant.WSConstants;

public class WebService extends IntentService
{
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Static {@link PowerManager.WakeLock} instance as per pattern in CommonsWare
	 * WakefulIntentService, recommended by Google.
	 */
	private static volatile PowerManager.WakeLock lockStatic = null;

	synchronized private static PowerManager.WakeLock getLock(Context context)
	{
		if (lockStatic == null)
		{
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

			lockStatic =
					mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebService_WakeLock");
			lockStatic.setReferenceCounted(true);
		}

		return (lockStatic);
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Global flag to say if the server was available after each call.
	 */
	@SuppressWarnings("unused")
	private boolean serverAvailable = true;

	public WebService()
	{
		super(WebService.class.getName());
	}

	/**
	 * Overridden as per pattern in WakefulIntentService to gain the {@link PowerManager.WakeLock}
	 * for this service. </p>
	 * 
	 * Copied from CommonsWare WakefulIntentService.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		PowerManager.WakeLock lock = getLock(this.getApplicationContext());

		if ((!lock.isHeld()) || ((flags & START_FLAG_REDELIVERY) != 0))
		{
			lock.acquire();
		}

		super.onStartCommand(intent, flags, startId);

		return (START_REDELIVER_INTENT);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		try
		{
			// get info from intent
			String action = intent.getAction();

			/* IMAGE_DOWNLOAD */
			if (IntentAction.WebService.IMAGE_DOWNLOAD.equals(action))
			{
				handleImageDownload(intent);
			}
		}
		finally
		{
			// release the lock
			PowerManager.WakeLock lock = getLock(this.getApplicationContext());

			if (lock.isHeld())
			{
				lock.release();
			}
		}

	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the {@link Messenger} object from the {@link Intent}.
	 */
	private Messenger getMessenger(Intent intent)
	{
		Bundle extras = intent.getExtras();
		Messenger messenger = null;
		if (extras != null)
		{
			messenger = (Messenger) extras.get(IntentExtra.Key.IPC_MESSENGER);
		}

		return messenger;
	}

	/**
	 * @return the Image Download URL from the {@link Intent}; or <code>null</code> if not found
	 */
	private String getImageDownloadUrl(Intent intent)
	{
		// null check
		if (intent == null || intent.getExtras() == null)
		{
			return null;
		}

		return intent.getExtras().getString(IntentExtra.Key.WS_IMAGE_URL);
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Logs in to the server with the details in the given {@link Intent}.
	 * 
	 * @param intent
	 *            Intent action is used to identify the response. <br>
	 *            Extras must contain the following key-value pairs:
	 *            <ul>
	 *            <li>
	 *            {@link IntentExtra.Key#IPC_MESSENGER} - {@link Messenger} object to return the
	 *            results.</li>
	 *            <li>
	 *            {@link IntentExtra.Key#WS_IMAGE_URL} - URL of the image.</li>
	 *            </ul>
	 */
	void handleImageDownload(Intent intent)
	{
		/* REQUEST TO SERVER */

		AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		int result = WSConstants.Result.FAILURE_GENERAL;
		Bitmap flair = null;
		try
		{
			String getUrl = getImageDownloadUrl(intent);
			Dbug.log(getUrl);

			HttpGet get = new HttpGet(getUrl);

			Dbug.log(get.getRequestLine().toString());

			// call service
			HttpResponse response = client.execute(get);

			// check response code
			StatusLine statusLine = response.getStatusLine();
			int responseCode = statusLine.getStatusCode();
			if (HttpStatus.SC_OK == responseCode)
			{
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null)
				{
					InputStream inputStream = null;
					try
					{
						inputStream = resEntity.getContent();
						flair = BitmapFactory.decodeStream(inputStream);
					}
					finally
					{
						if (inputStream != null)
						{
							inputStream.close();
						}
						resEntity.consumeContent();
					}
					result = WSConstants.Result.OK;
				}
				else
				{
					Dbug.log("DOWNLOAD RESPONSE EMPTY");
				}


			}
			else
			{
				Dbug.log("DOWNLOAD FAILED - " + responseCode + " " + statusLine.getReasonPhrase());
			}
		}
		catch (IOException e)
		{
			Dbug.log("DOWNLOAD IO ERROR!");
			e.printStackTrace();

			serverAvailable = false;
		}
		catch (Exception e)
		{
			Dbug.log("DOWNLOAD FAILURE!");
			e.printStackTrace();
		}
		finally
		{
			if (client != null)
			{
				client.close();
			}
		}

		/* RESPONSE TO CALLING CONTEXT */

		// send response back to calling context's Handler
		Message msg = Message.obtain();

		// use hash of the action to identify this message
		msg.what = intent.getAction().hashCode();
		msg.arg1 = result;
		msg.obj = flair;

		try
		{
			Messenger messenger = getMessenger(intent);
			if (messenger != null)
			{
				messenger.send(msg);
			}
		}
		catch (android.os.RemoteException e)
		{
			Dbug.log("WEBSERVICE TO RESPOND TO CALLING ACTIVITY - " + e);
			e.printStackTrace();
		}
	}


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////


	// ////////////////////////////////////////////////////////////////////////////////////////////////////////


}
