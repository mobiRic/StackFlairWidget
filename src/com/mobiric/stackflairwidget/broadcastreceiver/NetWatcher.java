package com.mobiric.stackflairwidget.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiric.stackflairwidget.service.FlairWidgetService;

public class NetWatcher extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// send action to the service
		Intent serviceIntent = new Intent(context, FlairWidgetService.class);

		//		String receivedAction = intent.getAction();
		//		if (receivedAction != null)
		//		{
		//			serviceIntent.setAction(receivedAction);
		//		}

		context.startService(serviceIntent);
	}
}