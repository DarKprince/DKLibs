package it.dk.libs.logic;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import it.dk.libs.R;
import it.dk.libs.common.Logger;
import it.dk.libs.common.ServiceLocator;
import it.dk.libs.ui.ActivityHelper;

import java.io.File;

import static it.dk.libs.common.ContractHelper.checkNotNull;

/**
 * 
 * AppEnvironment activity
 * 
 * @author saverio guardato
 *
 */

public class AppEnvironment
{
	//---------- Private fields
	protected static final String TAG = "DkLibs";
	protected static String TAG2 = "["+AppEnvironment.class.getSimpleName()+"]";
	private static final Object mSyncObject = new Object();
	private Context mContext;

	private static AppEnvironment mInstance;
	protected static String preferences_name = "preferences";
	
	/** Application name */
	private static String mAppDisplayName;
	protected String getAppDisplayName()
	{ return mAppDisplayName; }
	protected void setAppDisplayName(String value) 
	{ mAppDisplayName = value; }

	/** Application version displayed to the user (about activity etc) */
	public final static String APP_DISPLAY_VERSION = "1.0";

	/** Application name used during the ping of update site */
	public final static String APP_INTERNAL_NAME = mAppDisplayName;

	/** Application version for internal use (update, crash report etc) */
	public final static String APP_INTERNAL_VERSION = "1";

	/** address where send log */
	public final static String DEVELOPER_EMAIL = "sa.guarda@gmail.com";

	/** platform - dependent newline char */
	protected final static String LINE_SEPARATOR = System.getProperty("line.separator");

	protected static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/."+APP_INTERNAL_NAME+"/";

	protected boolean mForceSubserviceRefresh;
	protected boolean getForceSubserviceRefresh()
	{ return mForceSubserviceRefresh; }
	public void setForceSubserviceRefresh(boolean newValue)
	{ mForceSubserviceRefresh = newValue; }

	private AppEnvironment(Context context) {
		//use default objects factory
		this(context, getDefaultObjectsFactory());
	}

	private AppEnvironment(Context context, ObjectsFactory objectsFactory) {
		checkNotNull(objectsFactory, "ObjectsFactory");
		checkMainDirectory();
		setupVolatileData(context, objectsFactory);
	}

	/** Default objects factory, testing purposes */
	protected static final ObjectsFactory defaultObjectsFactory = new ObjectsFactory();
	protected static final ObjectsFactory getDefaultObjectsFactory()
	{ return defaultObjectsFactory; }

	/** lazy loading singleton */
	public static AppEnvironment getInstance(Context context) {
		synchronized (mSyncObject) {
			if (null == mInstance)
				mInstance = new AppEnvironment(context.getApplicationContext());
		}
		return mInstance;
	}

	public CrashReporter getCrashReporter()
	{return ServiceLocator.get(CrashReporter.class);}

	public Logger getLogFacility()
	{ return checkNotNull(ServiceLocator.get(Logger.class), "LogFacility"); }
	
	public ActivityHelper getActivityHelper()
	{ return checkNotNull(ServiceLocator.get(ActivityHelper.class), "ActivityHelper"); }

	/**
	 * Setup the volatile data of application.
	 * This is needed because sometime the system release memory
	 * without completely close the application, so all static fields
	 * will become null
	 * 
	 * Public only for test purpose
	 */
	private void setupVolatileData(Context context, ObjectsFactory mObjectsFactory) {
		//empty service locator
		ServiceLocator.clear();
		
		//set the log tag
		Logger logFacility = mObjectsFactory.createLogFacility(TAG);
		logFacility.i("AppEnvironment", "Initializing environment");
		//put log facility
		ServiceLocator.put(logFacility);

		//initialize and automatically register crash reporter
		CrashReporter crashReport = mObjectsFactory.createCrashReporter(context);
		ServiceLocator.put(crashReport);

		//ActivityHelper
		ActivityHelper actHelper = mObjectsFactory.createActivityHelper(logFacility, context);
		ServiceLocator.put(actHelper);
		
		//set application name
	    setAppDisplayName(context.getString(R.string.app_name));
		setForceSubserviceRefresh(false);
	}

	private void checkMainDirectory() {
		File dir = new File(DATA_PATH);
		if (!dir.exists()) {
			if (!dir.mkdirs())
				Log.e(TAG ,"ERROR: Creation of directory " + DATA_PATH + " on sdcard failed");
			else
				Log.v(TAG, "Created directory " + DATA_PATH + " on sdcard");
		}
	}

	////---------- Private classes
	private static class ObjectsFactory {
		public Logger createLogFacility(String logTag)
		{ return new Logger(logTag); }

		public ActivityHelper createActivityHelper(Logger logf, Context ctx) {
			return new ActivityHelper(logf, ctx);
		}

		public CrashReporter createCrashReporter(Context context)
		{ return new CrashReporter(context); }

	}

}
