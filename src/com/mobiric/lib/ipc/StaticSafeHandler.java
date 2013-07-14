package com.mobiric.lib.ipc;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * {@link Handler} subclass designed to avoid memory leaks. Instances of this
 * {@link StaticSafeHandler} must be declared static to prevent memory leaks of the containing
 * {@link Context}</p>
 * 
 * The {@link Callback} instance used by this class needs to know the exact parameters passed back
 * in the {@link Message}.
 * 
 * @see "HandlerLeak" section of <a href="http://tools.android.com/tips/lint-checks">Lint Checks</a>
 */
public class StaticSafeHandler extends Handler
{
	private final WeakReference<Handler.Callback> weakRefCallback;


	/**
	 * Creates a {@link Handler} that does not leak a {@link Context}. Receives a
	 * {@link Handler.Callback} instance, which is held in a {@link WeakReference} in order to
	 * prevent the memory leak.
	 * 
	 * @param callback
	 *            {@link Callback} instance that is held in a {@link WeakReference}
	 */
	public StaticSafeHandler(Handler.Callback callback)
	{
		super();
		this.weakRefCallback = new WeakReference<Handler.Callback>(callback);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Passes control to the {@link Handler.Callback} instance, if the weakRefCallback field
	 * contains a valid object.
	 */
	public void handleMessage(Message message)
	{
		// null check
		if (message == null)
		{
			return;
		}

		// pass control to callback
		Handler.Callback callback = weakRefCallback.get();
		if (callback != null)
		{
			callback.handleMessage(message);
		}
	}

}