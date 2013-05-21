package it.dk.libs.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
	private static final String LOGTAG = "NetworkReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOGTAG, "Action: " + intent.getAction());
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager cm =
					(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		}
	}
}