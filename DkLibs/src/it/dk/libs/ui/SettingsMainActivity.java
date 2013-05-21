package it.dk.libs.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import it.dk.libs.R;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.Logger;
import it.dk.libs.common.ResultOperation;
import it.dk.libs.common.ServiceLocator;
import it.dk.libs.logic.CrashReporter;
import it.dk.libs.logic.PrepareLogToSendThread;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

/**
 * Application main settings, allow to send log related to the
 * application.
 *
 */
public class SettingsMainActivity
	extends PreferenceActivity
{
	//---------- Private fields
    private static final String LOG_HASH = "RainbowSettingsMainActivity";
	protected static final int DIALOG_EXECUTING_SENDING_LOGS = 10;
	protected static final int TASK_PREPARE_LOG_THREAD = 10;

	protected PrepareLogToSendThread mPrepareLogThread;
	
	protected boolean mMustSendLog;
	protected String mSendLogEmail;
	
	protected ILogger mBaseLogFacility;
	protected ActivityHelper mBaseActivityHelper;
	protected CrashReporter mBaseCrashReporter;
	protected String mLogTagToSearch;
	protected String mAppName;
	protected String mAppVersionDescription; 




	//---------- Public properties

	
	
	
	//---------- Events
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBaseLogFacility = checkNotNull(ServiceLocator.get(Logger.class), "BaseLogFacility");
        mBaseLogFacility.logStartOfActivity(LOG_HASH, this.getClass(), savedInstanceState);
		mBaseActivityHelper = checkNotNull(ServiceLocator.get(ActivityHelper.class), "BaseActivityHelper");
		mBaseCrashReporter = checkNotNull(ServiceLocator.get(CrashReporter.class), "CrashReporter");

		getDataFromIntent(getIntent());

		addPreferencesFromResource(R.layout.actsettingsmain);
        setTitle(String.format(
        		getString(R.string.settingsmain_title), mAppName));
        
        //send application log
		Preference sendLog = findPreference("actsettingsmain_sendLog");
		sendLog.setOnPreferenceClickListener(sendLogClickListener);
		
		//can send the log only when the activity is called for the first time
		if(null != savedInstanceState) mMustSendLog = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPrepareLogThread = (PrepareLogToSendThread) getLastNonConfigurationInstance();
		if (null != mPrepareLogThread) {
			//register new handler
			mPrepareLogThread.registerCallerHandler(mActivityHandler);
		}

		if (mMustSendLog) {
			//create the log email
			sendLogClickListener.onPreferenceClick(null);
			mMustSendLog = false;
		}
	}

	@Override
	protected void onPause() {
        super.onPause();
		if (null != mPrepareLogThread) {
			//unregister handler from background thread
			mPrepareLogThread.unregisterCallerHandler();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		//save eventually open background thread
		return mPrepareLogThread;
	}


	/**
	 * Called when send log button is pressed
	 */
	private OnPreferenceClickListener sendLogClickListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			Activity activity = SettingsMainActivity.this;
			//create new progress dialog
			showDialog(DIALOG_EXECUTING_SENDING_LOGS);

			//preparing the background thread for executing service command
			mPrepareLogThread = new PrepareLogToSendThread(
					activity.getApplicationContext(),
					mActivityHandler,
					TASK_PREPARE_LOG_THREAD,
					mBaseLogFacility,
					mBaseCrashReporter,
					mLogTagToSearch);
			mPrepareLogThread.start();
			return true;
		}
	};

	
	protected Dialog onCreateDialog(int id){
		Dialog retDialog;
		
		switch (id){
		case DIALOG_EXECUTING_SENDING_LOGS:
			retDialog = mBaseActivityHelper.createAndShowProgressDialog(this, 0, R.string.common_msgGatheringLogs);
			break;
			
		default:
			return super.onCreateDialog(id);
		}
		
		return retDialog;
	};
	
	
	/**
	 * Hander to call when the execute command menu option ended
	 */
	private Handler mActivityHandler = new Handler() {
		public void handleMessage(Message msg)
		{
			//check if the message is for this handler
			if (msg.what != TASK_PREPARE_LOG_THREAD)
				return;
			
			//dismisses progress dialog
			dismissDialog(DIALOG_EXECUTING_SENDING_LOGS);
			ResultOperation<String> res = mPrepareLogThread.getResult();
			if (res.hasErrors()) {
				//some errors
				mBaseActivityHelper.reportError(SettingsMainActivity.this, res);
			} else {
				//send email with log
				mBaseActivityHelper.sendEmail(
						SettingsMainActivity.this,
						mSendLogEmail,
						String.format(getString(R.string.common_sendlogSubject), mAppName + " " + mAppVersionDescription),
						String.format(getString(R.string.common_sendlogBody), res.getResult()));
			}
			//free the thread
			mPrepareLogThread = null;
		};
	};


	
	
	//---------- Public methods

	
	
	
	//---------- Private methods
	/**
	 * Get data from intent and configured internal fields
	 * @param intent
	 */
	private void getDataFromIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		//check if a direct call to send log preference is needed
		if(extras != null) {
			mMustSendLog = !TextUtils.isEmpty(extras.getString(ActivityHelper.INTENTKEY_MUST_SEND_LOG_REPORT));
			mAppName =  checkNotNullOrEmpty(extras.getString(ActivityHelper.INTENTKEY_APPLICATION_NAME), "Application Name");
			mAppVersionDescription =  checkNotNullOrEmpty(extras.getString(ActivityHelper.INTENTKEY_APPLICATION_VERSION), "Application Version Description");
			mSendLogEmail = checkNotNull(extras.getString(ActivityHelper.INTENTKEY_EMAIL_TO_SEND_LOG), "SendLogEmail Address");
			mLogTagToSearch =  checkNotNullOrEmpty(extras.getString(ActivityHelper.INTENTKEY_LOG_TAG_FOR_REPORT), "LogTag to search in log");
		} else {
			mMustSendLog = false;
			checkNotNullOrEmpty(null, "BaseSettingsActivity required params all missing!");
		}
	}
}
