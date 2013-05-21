package it.dk.libs.data;

import android.content.Context;

import java.util.Calendar;

public abstract class AppPreferencesDao
	extends PreferencesDao
{
	//---------- Private fields
	
    protected static final String PROP_APPVERSION = "appVersion";
    protected static final String PROP_INSTALLATION_TIME = "installationTime";
    protected static final String PROP_UNIQUEID = "uniqueId";
    protected static final String PROP_APPDATADIRECTORY = "appDataDirectory";

	

	
	//---------- Constructors
	public AppPreferencesDao(Context context, String preferenceKey) {
		super(context, preferenceKey);
	}





	//---------- Public Properties


    
    
    
	//---------- Public Methods

    public String getAppVersion()
    { return mSettings.getString(PROP_APPVERSION, "00.00.00"); }
    public void setAppVersion(String newValue)
    { mEditor.putString(PROP_APPVERSION, newValue); }
    
    
    /**
     * Get the installation time of the application. Useful for
     * application that expires
     * @return
     */
    public long getInstallationTime()
    {
    	long installationTime = mSettings.getLong(PROP_INSTALLATION_TIME, 0);
    	if (0 == installationTime) {
    		//first run, set as installation time current milliseconds
    	    final Calendar c = Calendar.getInstance();
    	    installationTime = c.getTimeInMillis();
    	    //store it
    	    setInstallationTime(installationTime);
    	    save();
    	}
    	return installationTime;
    }
    /**
     * Set the installation time of the application
     * @param newValue
     */
    public void setInstallationTime(long newValue)
    { mEditor.putLong(PROP_INSTALLATION_TIME, newValue); }

    
    /**
     * Return the unique id of the application
     * @return
     */
    public long getUniqueId()
    {
    	long installationTime = mSettings.getLong(PROP_UNIQUEID, 0);
    	if (0 == installationTime) {
    		//first run, set as installation time current milliseconds
    	    final Calendar c = Calendar.getInstance();
    	    installationTime = c.getTimeInMillis();
    	    //store it
    	    mEditor.putLong(PROP_UNIQUEID, installationTime);
    	    save();
    	}
    	return installationTime;
    }
    
    public String getApplicationDataDirectory()
    { return mSettings.getString(PROP_APPDATADIRECTORY, ""); }
    public void setApplicationDataDirectory(String newValue)
    { mEditor.putString(PROP_APPDATADIRECTORY, newValue); }



    //---------- Protected Methods

}