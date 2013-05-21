package it.dk.libs.logic;

import android.app.Activity;
import it.dk.libs.common.ILogger;
import it.dk.libs.net.IRestfulClient;
import it.dk.libs.net.RestfulClient;
import it.dk.libs.ui.ActivityHelper;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

/**
 * Send info about the app version and OS where installed and screen resolution
 * 
 */
public class SendStatisticsTask
	implements Runnable
{
	//---------- Private fields
    private static final String LOG_HASH = "RainbowSendStatisticsTask";
	protected final ILogger mBaseLogFacility;
	protected final String mUpdateWebserviceUrl;
	protected final String mAppName;
	protected final String mUniqueId;
	protected final String mAppVersion;
	protected String mScreenRes;




	//---------- Constructors
	public SendStatisticsTask(
			ILogger logFacility,
			ActivityHelper activityHelper,
			Activity activity,
			String updateWebserviceUrl,
			String appName,
			String appVersion,
			String uniqueId)
	{
		this(logFacility, updateWebserviceUrl, appName, appVersion, uniqueId);
		checkNotNull(activity, "Activity");
		mScreenRes = activityHelper.getScreenWidth(activity) + "x" + activityHelper.getScreenHeight(activity);
	}

	public SendStatisticsTask(
			ILogger logFacility,
			String updateWebserviceUrl,
			String screenRes,
			String appName,
			String appVersion,
			String uniqueId)
	{
		this(logFacility, updateWebserviceUrl, appName, appVersion, uniqueId);
		mScreenRes = checkNotNullOrEmpty(screenRes, "Screen Resolution");
	}

	private SendStatisticsTask(
			ILogger logFacility,
			String updateWebserviceUrl,
			String appName,
			String appVersion,
			String uniqueId)
	{
		mBaseLogFacility = checkNotNull(logFacility, "Log Facility");
		mUpdateWebserviceUrl = checkNotNullOrEmpty(updateWebserviceUrl, "Update Webservice Url");
		mAppName = checkNotNullOrEmpty(appName, "App Name");
		mAppVersion = checkNotNullOrEmpty(appVersion, "App Version");
		mUniqueId = checkNotNullOrEmpty(uniqueId, "Unique ID");
	}




	//---------- Public properties
	public void run()
	{
		mBaseLogFacility.i(LOG_HASH, "Collecting statistic information about the application");

		//prepare data to send
		StringBuilder url = new StringBuilder();
		url.append(mUpdateWebserviceUrl)
			.append("?")
			.append("unique=")
			.append(mUniqueId)
			.append("&")
			.append("swname=")
			.append(mAppName)
			.append("&")
			.append("ver=")
			.append(mAppVersion)
			.append("&")
			.append("sover=")
			.append("android-"+ android.os.Build.VERSION.SDK)
			.append("&")
			.append("sores=")
			.append(mScreenRes);
		
		IRestfulClient client = new RestfulClient(mBaseLogFacility);
		try {
			client.requestGet(url.toString());
		} catch (ClientProtocolException e) {
			mBaseLogFacility.e(e);
		} catch (IOException e) {
			//connection error
			mBaseLogFacility.e(LOG_HASH, "No connection for sending statistics :(");
		}
	}



	//---------- Public methods




	//---------- Private methods

}
