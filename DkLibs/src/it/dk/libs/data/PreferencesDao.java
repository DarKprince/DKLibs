package it.dk.libs.data;

import android.content.Context;
import android.content.SharedPreferences;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

public abstract class PreferencesDao
{
	//---------- Private fields
	protected SharedPreferences mSettings;
	protected SharedPreferences.Editor mEditor;
	protected final String mPreferencesKey;
	
	protected final static String BACKUP_SUFFIX = "_backup";

	

	
	//---------- Constructors
	public PreferencesDao(Context context, String preferenceKey) {
		checkNotNull(context, "Context");
		mPreferencesKey = checkNotNullOrEmpty(preferenceKey, "Preference Key");
	    mSettings = context.getSharedPreferences(mPreferencesKey, 0);
	    mEditor = mSettings.edit();
	}

	/**
	 * Store the preferences
	 */
	public boolean save() {
	    return mEditor.commit();
	}


	/**
	 * Backup preferences
	 */
    public void backup(Context context)
    {
        //backup settings into backup shared preferences
        SharedPreferences settingsBackup = context.getSharedPreferences(mPreferencesKey + BACKUP_SUFFIX, 0);
        SharedPreferences.Editor editorBackup = settingsBackup.edit();
        backupProperties(editorBackup);
    	editorBackup.commit();
    }
    
    
    /**
     * Restore a previously backup data
     * @param context
     */
    public void restore(Context context)
    {
        //if settings is null, no backup was made at the settings
        if (null == mSettings)
        	return;
    	
        //load backup preferences
    	SharedPreferences settingsBackup = context.getSharedPreferences(mPreferencesKey + BACKUP_SUFFIX, 0);
    	restoreProperties(settingsBackup);
        //backup settings into backup shared preferences
		save();
    }



	
	//---------- Private methods
    protected abstract void backupProperties(SharedPreferences.Editor editorBackup);
	
	protected abstract void restoreProperties(SharedPreferences settingsBackup);
}