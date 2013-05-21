package it.dk.libs.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import it.dk.libs.R;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.Logger;
import it.dk.libs.common.ServiceLocator;

import static it.dk.libs.common.ContractHelper.checkNotNull;

/**
 * A basic data editing activity, with saving of
 * user data only when the user press the back button or the select
 * save from option menu.
 * 
 * Starting from Android 2.0 (API 5), i can use the onBackPressed
 * method to execute code when the user press the back button.
 * This Activity simulate the behavior also with API level < 5
 * 
 * http://developer.android.com/reference/android/app/Activity.html#ProcessLifecycle
 */
public abstract class BaseDataEntryActivity
	extends Activity
{
	//---------- Private fields
	protected final static int OPTIONMENU_SAVE = 10;
	protected final static int OPTIONMENU_CANCEL = 11;
	
	protected final static String KEY_MUSTRETURNFROMCALLEDACTIVITY = "mustReturnFromCalledActivity";
	
	protected ILogger mBaseLogFacility;
	/** True when the onResume event is called after a child activity return */
	protected boolean mMustReturnedFromStartedActivity;
	
	/** True when the activity is loaded for the first time, false in all the other cases
	 *   (for example, when screen is rotated)
	 */
	protected boolean mFirstStart;

	
	
	
	//---------- Constructors

	
	
	
	//---------- Public properties

	
	
	
	//---------- Events
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mBaseLogFacility = checkNotNull(ServiceLocator.get(Logger.class), "RainbowLogFacility");

        if (null != savedInstanceState){
			mFirstStart = false;
		} else {
			//first start
			mFirstStart = true;
			//no called activity
			mMustReturnedFromStartedActivity = false;
		}
		
		//if a called activity return and the screen was rotate inside the
		//called activity, a call to this method is done, and after a call to
		//onStart will be made, where data are put inside views.
	}
	

	/**
	 * Called when the activity starts.
	 * When a caller activity returns, the onRestart and the onStart methods are called
	 * When a caller activity returns and the screen was rotated, only onStart is called.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		//load data inside controls if the activity is started for the first time
		if (mFirstStart) {
			loadDataIntoViews();
			mFirstStart = false;
		}
		
		if (mMustReturnedFromStartedActivity)
			returnFromStartedActivity();
		mMustReturnedFromStartedActivity = false;
	}
	

	/**
	 * Create option menu with the cancel button
	 * @param menu
	 * @return
	 */
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, OPTIONMENU_SAVE, 10000, R.string.common_mnuSave)
			.setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, OPTIONMENU_CANCEL, 10001, R.string.common_mnuCancel)
			.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
	};

	/**
	 * Manage the selection of cancel button in the option menu
	 * @param item
	 * @return
	 */
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result;
		
		switch (item.getItemId()) {
		case OPTIONMENU_CANCEL:
			cancelEdit();
			result = true;
			break;
			
		case OPTIONMENU_SAVE:
			confirmEdit();
			result = true;
			break;
			
		default:
			result = super.onOptionsItemSelected(item);
		}

		return result; 
	};

	
	/**
	 * Called when an activity must be destroyed (like when the user rotate
	 * the screen
	 * @param outState
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveVolatileData(outState);
	}
	

	/**
	 * Called when the activity is recreated (like when the user rotate the screen)
	 * 
	 * Called after the onCreate method, between onStart() and onPostCreate(Bundle).
	 * 
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//reload volatile data (screen rotated etc)
		loadVolatileData(savedInstanceState);
	}
	
	
	/**
	 * Intercept when the user press the Back button and create an event tracking
	 * of the event
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * Intercept when the user release the Back button, call the method for
	 * saving data and close the activity
	 * @param keyCode
	 * @param event
	 * @return
	 */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            confirmEdit();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }	
	
    /* (non-Javadoc)
     * @see android.app.Activity#startActivity(android.content.Intent)
     */
    @Override
    public void startActivity(Intent intent) {
    	mMustReturnedFromStartedActivity = true;
    	super.startActivity(intent);
    }
    
    
    /* (non-Javadoc)
     * @see android.app.Activity#startActivityForResult(android.content.Intent, int)
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
    	mMustReturnedFromStartedActivity = true;
    	super.startActivityForResult(intent, requestCode);
    }
    


	//---------- Public methods




	//---------- Private methods
    
	/**
	 * Save values modified in views of the activity. Should manage saving errors.
	 * Called when the user press the back button
	 * 
	 * @return true if everything went well, otherwise false
	 */
	protected abstract boolean saveDataFromViews();
	
	/**
	 * Load values into activity's views.
	 * Called when activity first start, and not when return from called activity
	 * or after a screen rotation, because if the views have an id, the system
	 * automatically restores their values
	 */
	protected abstract void loadDataIntoViews();
	
	/**
	 * Save volatile activity data into Bundle object. Called before the rotation of
	 * the screen, for example. Save data that can change during the activity lifecycle
	 * and that is not stored inside views (data inside views with id is automatically restored)
	 */
	protected void saveVolatileData(Bundle outState)
	{
		outState.putBoolean(KEY_MUSTRETURNFROMCALLEDACTIVITY, mMustReturnedFromStartedActivity);
	}
	
	/**
	 * Load volatile activity data from Bundle object. Called when an activity is
	 * restore after a screen rotation, for example.
	 */
	protected void loadVolatileData(Bundle savedInstanceState)
	{
		mMustReturnedFromStartedActivity = savedInstanceState.getBoolean(KEY_MUSTRETURNFROMCALLEDACTIVITY);
	}
	
	/**
	 * Called when the user press the save menu button or press the back.
	 * Confirm the editing of activity data
	 */
	protected void confirmEdit()
	{
		if (saveDataFromViews()){
			setResult(RESULT_OK);
			finish();
		}
	}
	
	
	/**
	 * Called when the user select the "Cancel" button in option menu
	 */
	protected void cancelEdit()
    {
		setResult(RESULT_CANCELED);
    	finish();
    }
	
	/**
	 * Called when this activity calls another activity and the called activity returns
	 */
	protected void returnFromStartedActivity()
	{	}

}
