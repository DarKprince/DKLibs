package it.dk.libs.logic;

import android.content.Context;
import android.os.Environment;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.ResultOperation;
import it.dk.libs.data.AppPreferencesDao;

import java.io.File;
import java.util.Calendar;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

public abstract class LogicManager {
    //---------- Private fields
    private static final String LOG_HASH = "RainbowLogicManager";
    
    protected final AppPreferencesDao mBaseAppPreferencesDao;
    protected final ILogger mBaseLogFacility;
    protected final String mCurrentAppVersion;
    
    
    
    //---------- Constructor
    public LogicManager(
    		ILogger logFacility,
    		AppPreferencesDao appPreferencesDao,
            String currentAppVersion) {
        mBaseLogFacility = checkNotNull(logFacility, "Log Facility");
        mBaseAppPreferencesDao = checkNotNull(appPreferencesDao, "Application Preferences");
        mCurrentAppVersion = checkNotNullOrEmpty(currentAppVersion, "Application version");
    }
    
    


    //---------- Public properties
    /**
     * First start of a new version of the application
     */
    public boolean isFirstStartOfAppNewVersion()
    { return mFirstStartOfAppNewVersion; }
    protected boolean mFirstStartOfAppNewVersion;
    


    
    
    //---------- Public methods
    /**
     * Initializes data, execute begin operation
     */
    public ResultOperation<Void> executeBeginTasks(Context context)
    {
        mBaseLogFacility.v(LOG_HASH, "ExecuteBeginTask");
        
        mFirstStartOfAppNewVersion = isNewAppVersion();
        
        //checks for application upgrade
        ResultOperation<Void> res = performAppVersionUpgrade(context);
            
        return res;
    }

    /**
     * Executes final operation, just before the app close
     * @param context
     * @return
     */
    public ResultOperation<Void> executeEndTasks(Context context)
    {
    	ResultOperation<Void> res = new ResultOperation<Void>();
        return res;
    }


    
    
    
    //---------- Private methods
    /**
     * Checks if some upgrade is needed between current version of the
     * application and the previous one
     * 
     *  @return true if all ok, otherwise false
     */
    protected ResultOperation<Void> performAppVersionUpgrade(Context context)
    {
        if (isNewAppVersion()) {
            mBaseLogFacility.i(LOG_HASH, "Upgrading from " + mBaseAppPreferencesDao.getAppVersion() + " to " + mCurrentAppVersion);

            //execute upgrade tasks
            ResultOperation<Void> res = executeUpgradeTasks(context, mBaseAppPreferencesDao.getAppVersion());
            if (null != res && res.hasErrors()) return res;
            
            //update expiration date
            final Calendar c = Calendar.getInstance();
            mBaseAppPreferencesDao.setInstallationTime(c.getTimeInMillis());
            //update application version in the configuration
            mBaseAppPreferencesDao.setAppVersion(mCurrentAppVersion);
            //and save updates
            mBaseAppPreferencesDao.save();
            mBaseLogFacility.i(LOG_HASH, "Upgrading complete");
        }
        
        return new ResultOperation<Void>();
    }
        
    /**
     * Perform application upgrade from startingAppVersion to current version
     * 
     * @param context
     * @param startingAppVersion
     * @return
     */
    protected abstract ResultOperation<Void> executeUpgradeTasks(Context context, String startingAppVersion);
    
    /**
     * Check if the current application is new compared to the last time
     * the application run
     * 
     * @return
     */
    protected boolean isNewAppVersion() {
        String currentAppVersion = mBaseAppPreferencesDao.getAppVersion();
        return mCurrentAppVersion.compareToIgnoreCase(currentAppVersion) > 0;
    }
    
    
    /**
     * Checks the expiration date of the application
     * 
     * @return true if the application is valid, false if
     * application is invalid
     */
    protected boolean checkIfAppExpired()
    {
        //get installation time
        long installTime = mBaseAppPreferencesDao.getInstallationTime();
        
        // get the current time
        final Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();

        //find the maximum valid time interval
        //60 days
        long maxGap = getMaxExpirationTimeInDays() * 86400000;

        //if the difference between the two dates is greater than the max allowed gap
        return (currentTime - installTime > maxGap);
    }

    /**
     * Return number of seconds before the application expires
     * @return
     */
    protected long getMaxExpirationTimeInDays()
    { return 60; }
    
    
    /**
     * Gets data directory where saving application public information.
     * This directory is public and accessible to other application, so pay
     * attention to privacy issues.
     * General format is /mnt/sdcard/Android/data/your.package.name
     * 
     * @param appContext
     * @return
     */
    protected String getDataDirectory(Context appContext) {
        // The Galaxy S has two SDCards. One is internal and the other one is removable. The Environment class
        // refers to the first one. Unfortunately such an SDCard is very slow (maybe because formatted with a poor
        // performance file system type) and storing DBs and files on it makes the application unusable. Because
        // of this we try to use the external SD which has much better performance on writing.
        File sdCardDirectory = null;
        /*
        DeviceInfo devInf = new DeviceInfo(appContext);
        if ("GT-I9000".equalsIgnoreCase(devInf.getDeviceModel())) {
            File extDir = new File("/mnt/sdcard/external_sd");
            if (extDir.exists()) {
                if (Log.isLoggable(Log.INFO)) {
                    Log.info(TAG_LOG, "Using external SDCard for private data");
                }
                sdCardDirectory = "/mnt/sdcard/external_sd";
            }
        }
        */
        
        if (sdCardDirectory == null) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                sdCardDirectory = Environment.getExternalStorageDirectory();
                sdCardDirectory = new File(sdCardDirectory, "Android/data");
            } else {
                //no sd or read only, so switch to download cache directory
                sdCardDirectory = Environment.getDownloadCacheDirectory();
            }
        }
        File appData = new File(sdCardDirectory, appContext.getPackageName());
        return appData.getAbsolutePath();
    }
    
    /**
     * Creates application data directory in sd card, using {@link #getDataDirectory(Context)}
     * as base path
     * 
     * @param appContext
     * @return True if directory already exists or if it was created, otherwise
     * false 
     */
    protected boolean createDataDirectory(Context appContext) {
        //creates all the application folder
        String currentDataDir = getDataDirectory(appContext);
        File appDir = new File(currentDataDir);

        if (appDir.isDirectory() && appDir.exists()) {
            mBaseLogFacility.v(LOG_HASH, "Using " + currentDataDir + " as data directory");
            return true;
        }

        boolean baseCreated = appDir.mkdirs();
        if (baseCreated) {
            mBaseAppPreferencesDao.setApplicationDataDirectory(appDir.getAbsolutePath());
            mBaseAppPreferencesDao.save();
            mBaseLogFacility.v(LOG_HASH, "Created folder " + appDir.getAbsolutePath());
            return true;
        } else {
            mBaseLogFacility.e(LOG_HASH, "Cannot create folder " + appDir.getAbsolutePath());
            return false;
        }
    }
}
