package it.dk.libs.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import it.dk.libs.R;
import it.dk.libs.common.Logger;
import it.dk.libs.ui.ActivityHelper;

public abstract class BaseActivity extends Activity{

	public static final int WHAT_LOGTOSEND = 9000, WHAT_CHECK_FOR_CRASH = 9001, WHAT_PREPARELOGTOSEND = 9002;

	protected Context mContext;
	protected Handler mHandler;

	protected AppEnvironment mAppEnv;
	protected ActivityHelper mActH;
	protected Logger mLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext  = getApplicationContext();
		mHandler = createHandler();
		mAppEnv = AppEnvironment.getInstance(mContext);
		mActH = mAppEnv.getActivityHelper();
		mLog = mAppEnv.getLogFacility();
	}

	@Override
	protected void onStart() {
		super.onStart();
		defineGUI();
	}
	
	public void showCrashReportDialog(Activity act){
		AlertDialog.Builder builder = new AlertDialog.Builder(act);  
		builder.setTitle("Crash Reporter");
		builder.setMessage(getString(R.string.crash_msg))
		.setCancelable(false)  
		.setPositiveButton("Si",  
				new DialogInterface.OnClickListener(){  
			public void onClick(DialogInterface dialog, int id){  
				String result = BaseApplication.mLogQueue.firstElement();
				mActH.sendEmail(mContext, AppEnvironment.DEVELOPER_EMAIL,
						AppEnvironment.APP_INTERNAL_NAME+" Crash report, version:"+
								AppEnvironment.APP_DISPLAY_VERSION +" - " + AppEnvironment.APP_INTERNAL_VERSION,
								result);
				BaseApplication.mLogQueue.remove(result);
			}  
		});  
		builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		try {
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void defineGUI();

	public abstract Handler createHandler();

}
