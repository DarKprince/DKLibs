package it.dk.libs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import it.dk.libs.R;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.ResultOperation;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

/**
 * Generic helper for activities task
 * 
 */
public class ActivityHelper {
	//---------- Private fields
	private static final String LOG_HASH = "ActivityHelper";

	protected final Map<Integer, String> mMessages;
	protected final ILogger mBaseLogFacility;

	protected static final int MSG_INDEX_SEND_EMAIL = 0;
	protected static final int MSG_INDEX_ERROR_ARCHITECTURAL = 1;
	protected static final int MSG_INDEX_ERROR_COMMUNICATION = 2;
	protected static final int MSG_INDEX_ERROR_APP_EXPIRED = 3;
	protected static final int MSG_INDEX_ERROR_GENERIC = 4;
	protected static final int MSG_INDEX_YES = 5;
	protected static final int MSG_INDEX_NO = 6;
	protected static final int MSG_INDEX_NO_ERROR_MESSAGE = 7;

	/** use this value as first value available for customized error messages */
	protected static final int MSG_INDEX_FIRST_USER = 10;

	private Context mContext;


	//---------- Constructors
	public ActivityHelper(ILogger logFacility, Context context) {
		mBaseLogFacility = checkNotNull(logFacility, "Log Facility");
		checkNotNull(context, "Context");
		mMessages = loadCommonMessageStrings(context);
		mContext = context;
	}

	//---------- Public properties
	public final static String LINE_SEPARATOR = System.getProperty("line.separator");

	public final static int REQUESTCODE_NONE = 0;
	/** Used in Splashscreen activity for calling the main activity of the app */
	public final static int REQUESTCODE_MAINACTIVITY = 1;
	/** Used for calling main settings activity */
	public final static int REQUESTCODE_MAINSETTINGS = 2;
	/** Used when the camera activity returns the photo taken */
	public final static int REQUESTCODE_CAMERA = 3;
	/** Used when the gallery returns a photo o video selected by the use */
	public final static int REQUESTCODE_GALLERY = 4;

	/** Fist free value for the user */
	public final static int REQUESTCODE_FIRST_USER = 10;



	/** About Activity - Application full name */
	public static final String INTENTKEY_APPLICATION_NAME = "applicationName";
	/** About Activity - Application version (extend text) */
	public static final String INTENTKEY_APPLICATION_VERSION = "applicationVersion";
	/** About Activity - how to contact email address */
	public static final String INTENTKEY_EMAIL_TO_CONTACT = "contactEmail";
	/** Settings activity - must send log report as soon as it's opened */
	public static final String INTENTKEY_MUST_SEND_LOG_REPORT = "sendLogReport";
	/** Settings activity - log report recipient email address */
	public static final String INTENTKEY_EMAIL_TO_SEND_LOG = "sendLogEmail";
	/** Settings activity - log tag to search in the log */
	public static final String INTENTKEY_LOG_TAG_FOR_REPORT = "logTagForReport";




	//---------- Public methods
	/**
	 * Open a browser activity
	 * 
	 * @param context
	 * @param urlToOpen
	 * @param openInNewTask
	 */
	public void openBrowser(Context context, String urlToOpen, boolean openInNewTask)
	{
		mBaseLogFacility.v(LOG_HASH, "Launching intent for opening a browser");
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(urlToOpen));
		if (openInNewTask) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent); 		
	}

	/**
	 * Open a personalized Settings activity, based on {@link SettingsMainActivity}
	 * 
	 * @param settingsActivity
	 * @param callerActivity
	 * @param mustSendLog
	 * @param appName
	 * @param appVersion
	 * @param sendLogEmail
	 * @param logTagForReport
	 */
	public void openSettingsMain(
			Class<? extends SettingsMainActivity> settingsActivity,
			Activity callerActivity,
			boolean mustSendLog,
			String appName,
			String appVersion,
			String sendLogEmail,
			String logTagForReport)
	{
		checkNotNull(settingsActivity, "Derived Settings activity");
		checkNotNull(callerActivity, "CallerActivity");
		checkNotNullOrEmpty(appName, "Application Name");
		checkNotNullOrEmpty(appVersion, "Application Version");
		checkNotNullOrEmpty(sendLogEmail, "Send Log Email");
		checkNotNullOrEmpty(logTagForReport, "Log Tag to Search");

		Bundle extras = new Bundle();
		if (mustSendLog) extras.putString(INTENTKEY_MUST_SEND_LOG_REPORT, "1");
		extras.putString(INTENTKEY_APPLICATION_NAME, appName);
		extras.putString(INTENTKEY_APPLICATION_VERSION, appVersion);
		extras.putString(INTENTKEY_EMAIL_TO_SEND_LOG, sendLogEmail);
		extras.putString(INTENTKEY_LOG_TAG_FOR_REPORT, logTagForReport);

		openActivity(callerActivity, settingsActivity, extras, true, REQUESTCODE_MAINSETTINGS);
	}	

	/**
	 * Open {@link SettingsMainActivity} activity
	 * 
	 * @param callerActivity
	 * @param mustSendLog
	 * @param appName
	 * @param appVersion
	 * @param sendLogEmail
	 * @param logTagForReport
	 */
	public void openSettingsMain(
			Activity callerActivity,
			boolean mustSendLog,
			String appName,
			String appVersion,
			String sendLogEmail,
			String logTagForReport)
	{
		openSettingsMain(SettingsMainActivity.class, callerActivity, mustSendLog, appName, appVersion, sendLogEmail, logTagForReport);
	}	


	/**
	 * Launch the intent for sending an email
	 * 
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 */
	public void sendEmail(Context context, String to, String subject, String body)
	{
		sendEmail(context, to, subject, body, null);
	}

	/**
	 * 
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 * @param fileNameToAttach
	 */
	public void sendEmail(Context context, String to, String subject, String body, String fileNameToAttach) {
		mBaseLogFacility.v(LOG_HASH, "Launching activity for sending email with subject " + subject);
		Intent intent = new Intent(Intent.ACTION_SEND);
		//intent.setType("text/plain"); //use this line for testing in the emulator
		intent.setType("message/rfc822") ; // use from live device
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});        
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, body);

		//check if an attach is required
		if (!TextUtils.isEmpty(fileNameToAttach)) {
			File file = new File(fileNameToAttach);
			if (!file.exists())
				throw new IllegalArgumentException("File doesn't exists: " + file.getAbsolutePath());
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); 
		}
		Intent c_int = Intent.createChooser(intent, getMessage(MSG_INDEX_SEND_EMAIL));
		c_int.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(c_int);
	}


	/**
	 * Open the Google Map activity with the latitude and longitude coordinates
	 * 
	 * @param context
	 * @param latitude
	 * @param longitude
	 */
	public void openMapActivity(Context context, String latitude, String longitude) {
		openMapActivity(context, latitude, longitude, 0);
	}
	/**
	 * Open the Google Map activity with the latitude and longitude coordinates
	 * 
	 * @param context
	 * @param latitude
	 * @param longitude
	 * @param zoomLevel zoom level between 1 and 23. pass 0 for unset the zoom
	 */
	public void openMapActivity(Context context, String latitude, String longitude, int zoomLevel) {
		if (TextUtils.isEmpty(latitude))
			throw new IllegalArgumentException("Latitude cannot be null or empty");
		if (TextUtils.isEmpty(longitude))
			throw new IllegalArgumentException("Longitude cannot be null or empty");

		//how to build geo intent
		//http://developer.android.com/guide/appendix/g-app-intents.html
		StringBuilder sb = new StringBuilder();
		sb.append("geo:")
		.append(latitude)
		.append(",")
		.append(longitude);
		if (zoomLevel > 0)
			sb.append("?z=").append(zoomLevel);

		openMapActivityCore(context, sb.toString());
	}
	/**
	 * Open the Google Map activity pointing to the given address
	 * 
	 * @param context
	 * @param address
	 */
	public void openMapActivity(Context context, String address) {
		if (TextUtils.isEmpty(address))
			throw new IllegalArgumentException("Address cannot be null or empty");

		//how to build geo intent
		//http://developer.android.com/guide/appendix/g-app-intents.html
		StringBuilder sb = new StringBuilder();
		sb.append("geo:0,0?q=")
		.append(address);

		openMapActivityCore(context, sb.toString());
	}

	/**
	 * Open the camera and take a photo. In the onActivityResult method of calling activity
	 * the image should be saved, if only a thumbnail is required.
	 * 
	 * @param callerActivity
	 * @param destImageFile file where save the image, null to obtain a
	 *        thumbnail of shoot photo
	 */
	public void openNewPhotoFromCamera(Activity callerActivity, File destImageFile) {
		Intent intent = createShootNewPhotoIntent(destImageFile);
		openActivity(intent, callerActivity, true, REQUESTCODE_CAMERA);
	}

	/**
	 * Select a photo from the gallery. 
	 * @param callerActivity
	 */
	public void openPickImageFromGallery(Activity callerActivity) {
		Intent intent = createPickPhotoFromGalleryIntent();
		openActivity(intent, callerActivity, true, REQUESTCODE_GALLERY);         
	}

	/**
	 * Create the dialog that inform the user about application
	 * main capabilities and changeslog.
	 * <p/>
	 * Change resources for text personalization
	 * 
	 * @param context
	 * @return
	 */
	public Dialog createStartupInformativeDialog(Context context) {
		checkNotNull(context, "CallerActivity");

		return createInformativeDialog(context,
				context.getString(R.string.common_msgInfoboxTitle),
				context.getString(R.string.about_lblDescription) + LINE_SEPARATOR + LINE_SEPARATOR + context.getString(R.string.about_msgChangeslog),
				context.getString(R.string.common_btnOk));		
	}

	/**
	 * Launch the Android Market with the activity
	 * @param callerActivity
	 * @param query
	 */
	public void launchAndroidMarket(Activity callerActivity, String query) {
		//value to pass to the intent example
		//market://search?q=pname:de.ub0r.android.websms
		//market://search?q=websms+connector

		String completeQuery = "market://search?q=" + query;
		Intent i = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse(completeQuery));

		try {
			openActivity(i, callerActivity, false, REQUESTCODE_NONE);
		} catch (Exception e) {
			//maybe the Android Market is not installed
			mBaseLogFacility.e(LOG_HASH, "Cannot start Amdroid Market with this query:\n" + completeQuery);
			showInfo(R.string.common_msgCannotStartMarketIntent, Toast.LENGTH_LONG);
		}
	}


	/**
	 * Notify an error to the user 
	 * 
	 * @param context
	 * @param errorMessage
	 */
	public void reportError(Context context, String errorMessage)
	{
		mBaseLogFacility.e(LOG_HASH, "Error: " + errorMessage);
		Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
	}

	/**
	 * Notify an error to the user 
	 * @param context
	 * @param errorMessageId
	 */
	public void reportError(Context context, int errorMessageId)
	{ reportError(context, context.getString(errorMessageId)); }

	/**
	 * Notify an error to the user 
	 * 
	 * @param context
	 * @param res
	 */
	public <T extends Object> void reportError(Context context, ResultOperation<T> res)
	{ reportError(context, res.getException(), res.getReturnCode()); }

	/**
	 * Notify an error to the user 
	 * 
	 * @param context
	 * @param exception
	 * @param returnCode
	 */
	public void reportError(Context context, Exception exception, int returnCode)
	{
		String userMessage = getErrorMessage(returnCode, exception);

		//display the error to the user
		reportError(context, userMessage);
		//and log the error
		mBaseLogFacility.e(LOG_HASH, exception);
	}

	/**
	 * Get the error message to show, based on the error code and the exception
	 * @param returnCode
	 * @param exception
	 * @return
	 */
	public String getErrorMessage(int returnCode, Exception exception) {
		//First of all, examines return code for standard errors
		String userMessage = null;
		String exceptionMessage = null != exception ?  exception.getMessage() : getMessage(MSG_INDEX_NO_ERROR_MESSAGE);

		switch (returnCode) {
		case ResultOperation.RETURNCODE_ERROR_APPLICATION_ARCHITECTURE:
			userMessage = String.format(
					getMessage(MSG_INDEX_ERROR_ARCHITECTURAL), exceptionMessage);
			break;
		case ResultOperation.RETURNCODE_ERROR_COMMUNICATION:
			userMessage = String.format(
					getMessage(MSG_INDEX_ERROR_COMMUNICATION), exceptionMessage);
			break;
		case ResultOperation.RETURNCODE_APP_EXPIRED:
			userMessage = String.format(
					getMessage(MSG_INDEX_ERROR_APP_EXPIRED), exceptionMessage);
			break;
		case ResultOperation.RETURNCODE_ERROR_GENERIC:
			userMessage = String.format(
					getMessage(MSG_INDEX_ERROR_GENERIC), exceptionMessage);
			break;
		default:
			userMessage = String.format(
					getMessage(MSG_INDEX_ERROR_GENERIC), exceptionMessage);
			break;
		}
		return userMessage;
	}


	/**
	 * Notify an info message to the user
	 * @param context
	 * @param infoMessage
	 */
	public void showInfo(int infoMessageId)
	{
		showInfo(mContext.getString(infoMessageId), Toast.LENGTH_SHORT);
	} 
	public void showInfo(String infoMessage)
	{
		showInfo(infoMessage, Toast.LENGTH_SHORT);
	}
	public void showInfo(String infoMessage, int messageLenght)
	{
		Toast.makeText(mContext, infoMessage, messageLenght).show();
	}
	public void showInfo(int infoMessageId, int messageLenght)
	{
		Toast.makeText(mContext, infoMessageId, messageLenght).show();
	} 
	public void showInfo(int infoMessageId, int gravity, int xOffset, int yOffset)
	{
		Toast toast = Toast.makeText(mContext, infoMessageId, Toast.LENGTH_LONG);
		toast.setGravity(gravity, xOffset, yOffset);
		toast.show();
	} 
	public void showInfo(String infoMessage, int gravity, int xOffset, int yOffset)
	{
		Toast toast = Toast.makeText(mContext, infoMessage, Toast.LENGTH_LONG);
		toast.setGravity(gravity, xOffset, yOffset);
		toast.show();
	}

	/**
	 * Create a dialog with yes and no button
	 * 
	 * @param context
	 * @param titleId
	 * @param messageId
	 * @param yesListner
	 * @param noListener
	 * @return
	 */
	public Dialog createYesNoDialog(
			Context context,
			int titleId,
			int messageId,
			DialogInterface.OnClickListener yesListner,
			DialogInterface.OnClickListener noListener
			)
	{
		return createYesNoDialog(
				context,
				0 != titleId ? context.getString(titleId) : null,
						0 != messageId ? context.getString(messageId) : null,
								yesListner,
								noListener);
	}


	/**
	 * Create a dialog with yes and no button
	 * 
	 * @param context
	 * @param title
	 * @param message
	 * @param yesListner
	 * @param noListener
	 * @return
	 */
	public Dialog createYesNoDialog(
			Context context,
			String title,
			String message,
			DialogInterface.OnClickListener yesListner,
			DialogInterface.OnClickListener noListener
			)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		String yesMessage = getMessage(MSG_INDEX_YES);
		String noMessage = getMessage(MSG_INDEX_NO);

		if (!TextUtils.isEmpty(title)) builder.setTitle(title);
		if (!TextUtils.isEmpty(message)) builder.setMessage(message);
		builder.setCancelable(true);
		builder.setPositiveButton(yesMessage, yesListner);
		//		       new DialogInterface.OnClickListener() {
		//		           public void onClick(DialogInterface dialog, int id) {
		//		                //MyActivity.this.finish();
		//		           }
		//		       })
		builder.setNegativeButton(noMessage, noListener);
		//		    		   new DialogInterface.OnClickListener() {
		//		           public void onClick(DialogInterface dialog, int id) {
		//		                dialog.cancel();
		//		           }
		//		       });
		AlertDialog alert = builder.create();
		return alert;
	}


	/**
	 * Create and show progress dialog.
	 * 
	 * @param context
	 * @param title title of dialog, null or empty for no title
	 * @param message message of dialog, null or empty for no message
	 * @return
	 */
	public ProgressDialog createAndShowProgressDialog(
			Context context,
			String title,
			String message)
	{
		ProgressDialog progressDialog = createProgressDialog(context, title, message);
		progressDialog.show();
		return progressDialog;
	}

	/**
	 * Create and show a progress dialog.
	 * 
	 * @param context
	 * @param titleId 
	 * @param messageId
	 * @return
	 */
	public ProgressDialog createAndShowProgressDialog(
			Context context,
			int titleId,
			int messageId)
	{
		ProgressDialog progressDialog = createProgressDialog(context, titleId, messageId);
		progressDialog.show();
		return progressDialog;
	}

	/**
	 * Create a progress dialog.
	 * @param context
	 * @param title title of dialog, null or empty for no title
	 * @param message message of dialog, null or empty for no message
	 * @return
	 */
	public ProgressDialog createProgressDialog(
			Context context,
			String title,
			String message)
	{
		ProgressDialog progressDialog = new ProgressDialog(context);
		if (!TextUtils.isEmpty(title)) progressDialog.setTitle(title);
		if (!TextUtils.isEmpty(message)) progressDialog.setMessage(message);
		return progressDialog;
	}

	/**
	 * Create a progress dialog.
	 * 
	 * @param context
	 * @param titleId 
	 * @param messageId
	 * @return
	 */
	public ProgressDialog createProgressDialog(
			Context context,
			int titleId,
			int messageId)
	{
		return createProgressDialog(
				context,
				0 != titleId ? context.getString(titleId) : null,
						0 != messageId ? context.getString(messageId) : null);
	}


	/**
	 * Create a simple dialog with text and ok button
	 * 
	 * @param context
	 * @param title
	 * @param message
	 * @param okButtonLabel
	 * @return
	 */
	public Dialog createInformativeDialog(
			Context context,
			String title,
			String message,
			String okButtonLabel)
	{
		Dialog returnDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (!TextUtils.isEmpty(title)) builder.setTitle(title);
		if (!TextUtils.isEmpty(message)) builder.setMessage(message);
		builder.setNeutralButton(okButtonLabel, null);
		builder.setCancelable(true);
		returnDialog = builder.create();

		return returnDialog;
	}

	/**
	 * Create a simple dialog with text and ok button
	 * 
	 * @param context
	 * @param titleId
	 * @param messageId
	 * @param okButtonLabelId
	 * @return
	 */
	public Dialog createInformativeDialog(
			Context context,
			int titleId,
			int messageId,
			int okButtonLabelId)
	{
		return createInformativeDialog(context,
				0 != titleId ? context.getString(titleId) : null,
						0 != messageId ? context.getString(messageId) : null,
								context.getString(okButtonLabelId));
	}



	/**
	 * 
	 * @param callerActivity
	 * @return
	 */
	public int getScreenWidth(Activity callerActivity)
	{
		WindowManager w = callerActivity.getWindowManager();
		Display d = w.getDefaultDisplay();
		int width = d.getWidth();
		//int height = d.getHeight();
		return width;
	}


	/**
	 * 
	 * @param callerActivity
	 * @return
	 */
	public int getScreenHeight(Activity callerActivity)
	{
		WindowManager w = callerActivity.getWindowManager();
		Display d = w.getDefaultDisplay();
		int height = d.getHeight();
		return height;
	}

	/**
	 * 
	 * @param callerActivity
	 * @return
	 */
	public Point getScreenSize(Activity callerActivity)
	{
		WindowManager w = callerActivity.getWindowManager();
		Display d = w.getDefaultDisplay();
		Point p = new Point(d.getWidth(), d.getHeight());
		return p;
	}


	/**
	 * Return activity's current orientation
	 * 
	 * @param callerActivity
	 * @return Configuration.ORIENTATION_PORTRAIT, Configuration.ORIENTATION_LANDSCAPE or
	 *         Configuration.ORIENTATION_SQUARE
	 */
	public int getCurrentOrientation(Context callerContext)
	{
		return callerContext.getResources().getConfiguration().orientation;
	}

	/**
	 * Return activity orientation set in the Manifest.xml or by the last call
	 * to activity.setRequestedOrientation

	 * @param activity
	 * @return an orientation constant as used in ActivityInfo.screenOrientation.
	 */
	public int getOrientationSetOnActivity(Activity activity) {
		return activity.getRequestedOrientation();
	}

	/**
	 * Assign the desired orientation to the activity
	 * 
	 * @param callerActivity
	 * @param newOrientation: valuers from ActivityInfo.screenOrientation
	 */
	public void setCurrentOrientation(Activity activity, int newOrientation) {
		//set the orientation only if current orientation is different
		if (activity.getRequestedOrientation() != newOrientation) 
			activity.setRequestedOrientation(newOrientation);
	}


	/**
	 * Is the current activity orientation portrait? 
	 * 
	 * @param callerActivity
	 * @return
	 */
	public boolean isPortrait(Activity callerActivity)
	{
		//        WindowManager w = callerActivity.getWindowManager();
		//        Display d = w.getDefaultDisplay();
		//        return (d.getWidth() > d.getHeight());
		return
				callerActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				|| callerActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	}


	/**
	 * Is the current activity orientation landscape? 
	 * 
	 * @param callerActivity
	 * @return
	 */
	public boolean isLandscape(Activity callerActivity)
	{
		return
				callerActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				|| callerActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	}




	//---------- Protected methods
	// (override them with personalized code of the using app )

	/**
	 * Load standard messages. In general, this method shouldn't be overridden
	 * 
	 */
	protected Map<Integer, String> loadCommonMessageStrings(Context context) {
		Map<Integer, String> messages = new HashMap<Integer, String>();

		messages.put(MSG_INDEX_SEND_EMAIL, context.getString(R.string.common_sendEmailChooser));
		messages.put(MSG_INDEX_ERROR_ARCHITECTURAL, context.getString(R.string.common_msgArchitecturalError));
		messages.put(MSG_INDEX_ERROR_COMMUNICATION, context.getString(R.string.common_msgCommunicationError));
		messages.put(MSG_INDEX_ERROR_APP_EXPIRED, context.getString(R.string.common_msgAppExpiredError));
		messages.put(MSG_INDEX_ERROR_GENERIC, context.getString(R.string.common_msgGenericError));
		messages.put(MSG_INDEX_YES, context.getString(R.string.common_btnYes));
		messages.put(MSG_INDEX_NO, context.getString(R.string.common_btnNo));
		messages.put(MSG_INDEX_NO_ERROR_MESSAGE, context.getString(R.string.common_msgNoErrorMessage));

		loadCustomMessageStrings(context, messages);

		return messages;
	}


	/**
	 * Load customized message strings. Override this method for loading
	 * additional messages
	 * 
	 * @param context
	 */
	protected void loadCustomMessageStrings(Context context, Map<Integer, String> messages) {
	}

	/**
	 * Return the requested message, based on index
	 * @param msg_index
	 * @return
	 */
	protected String getMessage(int msg_index) {
		try {
			return mMessages.get(msg_index);
		} catch (Exception e) {
			return null;
		}
	}	

	/**
	 * Generic method for activity opening
	 * 
	 * @param callerActivity
	 * @param cls
	 * @param extraData
	 * @param mustReturn
	 * @param requestCode
	 */
	protected void openActivity(
			Activity callerActivity,
			Class<?> cls,
			Bundle extras,
			boolean mustReturn,
			int requestCode
			)
	{
		mBaseLogFacility.v(LOG_HASH, "Opening activity " + cls.toString());
		Intent i = new Intent(callerActivity.getBaseContext(), cls);

		//put the data in the intent
		if (null != extras) {
			i.putExtras(extras);
		}

		openActivity(i, callerActivity, mustReturn, requestCode);
	}

	protected void openActivity(
			Intent intent,
			Activity callerActivity,
			boolean mustReturn,
			int requestCode
			)
	{
		if (mustReturn)
			callerActivity.startActivityForResult(intent, requestCode);
		else
			callerActivity.startActivity(intent);
	}

	/**
	 * Launch the map application with the given string as intent URI
	 * 
	 * See http://developer.android.com/guide/appendix/g-app-intents.html
	 *  for a list of possible intent uris
	 *  
	 * @param context
	 * @param intentUri
	 */
	protected void openMapActivityCore(Context context, String intentUri) {
		mBaseLogFacility.v(LOG_HASH, "Opening map activity with string " + intentUri);
		final Intent myIntent = new Intent(
				android.content.Intent.ACTION_VIEW,
				Uri.parse(intentUri));
		context.startActivity(myIntent);

	}

	/**
	 * Creates the intent used to require an image from camera
	 * @param savedImageFile file where save the image, null to obtain a
	 *        thumbnail of shoot photo
	 * @return
	 */
	protected Intent createShootNewPhotoIntent(File savedImageFile) {
		//http://2009.hfoss.org/Tutorial:Camera_and_Gallery_Demo
		//Standard Intent action that can be sent to have the camera application capture an image and return it.
		//The caller may pass an extra EXTRA_OUTPUT to control where this image will be written. If the
		//EXTRA_OUTPUT is not present, then a small sized image is returned as a Bitmap object in the 
		//extra field. This is useful for applications that only need a small image. If the EXTRA_OUTPUT 
		//is present, then the full-sized image will be written to the Uri value of EXTRA_OUTPUT.

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (null != savedImageFile) {
			Uri extraFile = Uri.fromFile(savedImageFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, extraFile);
		}
		return intent;
	}

	protected Intent createPickPhotoFromGalleryIntent() {
		// To open up a gallery browser - mode 1
		//      Intent intent = new Intent();  
		//      intent.setType("image/*");  
		//      intent.setAction(Intent.ACTION_GET_CONTENT);
		//      String text = "Text to add to chooser";
		//      openActivity(Intent.createChooser(intent, text), callerActivity, true, REQUESTCODE_GALLERY);
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		return intent;
	}

	/**
	 * Get IMEI code which could be a good uuid
	 * @return
	 */
	public String getIMEI(Context context){
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return md5(tManager.getDeviceId());
	}

	/**
	 * MD5 Encrypt
	 * @param in
	 * @return
	 */
	public String md5(String in) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(in.getBytes());
			byte[] a = digest.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		return null;
	}

	public static boolean isEmulator() {
		return Build.MANUFACTURER.equals("unknown");
	}

}
