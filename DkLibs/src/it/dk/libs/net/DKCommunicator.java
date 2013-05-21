package it.dk.libs.net;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import it.dk.libs.common.Logger;

public class DKCommunicator {
	
	public static String TAG = "Communicator";

	protected RestfulClient mClient;
	protected Logger mLog;
	protected Context mContext;
	protected Handler mHandler;
	protected Boolean mStarted = false;
	

	public DKCommunicator(Context context, Handler ch, Logger log){
		mContext = context;
		mHandler = ch;
		mLog = log;
		mClient = new RestfulClient(mLog);
	}

	
	public void start(){
		//codice necessario per evitare l'errore networkOnMainThread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		mClient.startConversation();
		mStarted = true;
	}

	public void stop(){
		mClient.endConversation();
		mStarted = false;
	}
	
	

}
