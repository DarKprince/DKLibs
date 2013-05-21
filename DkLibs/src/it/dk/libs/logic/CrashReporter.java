package it.dk.libs.logic;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import it.dk.libs.common.ResultOperation;

import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * Collect application crash information
 *
 */
public class CrashReporter implements Thread.UncaughtExceptionHandler
{
	//---------- Private fields
	public final static String LINE_SEPARATOR = System.getProperty("line.separator");	

	HashMap<String, String> mCustomParameters = new HashMap< String, String>();
	protected Thread.UncaughtExceptionHandler mPreviousHandler;
	protected Context mContext;

	protected final static int MAX_REPORTS_TO_COLLECT = 5;

	
	
	
	//---------- Constructors
	/**
	 * 
	 */
	public CrashReporter(Context context) {
		mPreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context;
	}
	
	

	//---------- Public properties
	
	
	
	
	//---------- Public methods
	public void addCustomData( String Key, String Value )
	{
		mCustomParameters.put( Key, Value );
	}

	/**
	 * Called when an exception occurs
	 */
	public void uncaughtException(Thread t, Throwable e)
	{
		//get the error stack trace
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();

		//create the report
		StringBuilder report = new StringBuilder();
		report.append("Error Report collected on: ")
			.append(getCurrentDayHash())
			.append(" - ")
			.append(getCurrentTimeHash())
			.append(LINE_SEPARATOR)
			.append(LINE_SEPARATOR)
			.append("Informations :")
			.append(LINE_SEPARATOR)
			.append("==============")
			.append(LINE_SEPARATOR)
			.append(LINE_SEPARATOR)
			.append(createInformationString())
			.append("Custom Informations :")
			.append(LINE_SEPARATOR)
			.append("=====================")
			.append(LINE_SEPARATOR)
			.append(createCustomInfoString())
			.append(LINE_SEPARATOR)
			.append(LINE_SEPARATOR)
			.append("Stack:")
			.append(LINE_SEPARATOR)
			.append("=======")
			.append(LINE_SEPARATOR)
			.append(stacktrace)
			.append(LINE_SEPARATOR)
			.append("Cause:")
			.append(LINE_SEPARATOR)
			.append("=======")
			.append(LINE_SEPARATOR);
		
		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			report.append(result.toString());
			cause = cause.getCause();
		}
		printWriter.close();
		report.append("****  End of current crash report ***")
			.append(LINE_SEPARATOR)
			.append(LINE_SEPARATOR);
		saveAsFile(report.toString());
		
		//call previous handler
		mPreviousHandler.uncaughtException(t, e);
	}

	
	/**
	 * Find if previous crash reports are present
	 * @return
	 */
	public boolean isCrashReportPresent(Context context) {
		try {
			String baseFilePath = getBaseFilePath(context);
			String[] errorFilesList = getCrashFilesList(baseFilePath);
			return errorFilesList.length > 0;
			
		} catch (Exception e){
			return false;
		}
	}
	
	/**
	 * Checks if there are previous stack trace files 
	 * @param context
	 */
	public ResultOperation<String> getPreviousCrashReports(Context context)
	{
		//return an empty string if no crash reports are present
		if (!isCrashReportPresent(context))
			return new ResultOperation<String>("No crash report to submit" + LINE_SEPARATOR + LINE_SEPARATOR);
		
		try {
			String baseFilePath = getBaseFilePath(context);
			String[] crashFilesList = getCrashFilesList(baseFilePath);
			
			int curIndex = 0;
			StringBuffer wholeErrorText = new StringBuffer();

			for (String crashFile : crashFilesList) {
				if (curIndex++ <= MAX_REPORTS_TO_COLLECT) {
					wholeErrorText.append("New Trace collected:")
							.append(LINE_SEPARATOR)
							.append("=====================")
							.append(LINE_SEPARATOR);

					String filePath = baseFilePath + File.separator + crashFile;
					
					//read file content
					BufferedReader input =  new BufferedReader(new FileReader(filePath));
					String line;
					while ((line = input.readLine()) != null) {
						wholeErrorText.append(line)
								.append(LINE_SEPARATOR);
					}
					input.close();
				}
			}
			wholeErrorText.append(LINE_SEPARATOR);
			
			//delete all crash files
			deleteCrashFiles(context);
			
			return new ResultOperation<String>(wholeErrorText.toString());
			
		} catch(Exception e) {
			return new ResultOperation<String>(e, ResultOperation.RETURNCODE_ERROR_GENERIC);
		}
	}
	
	/**
	 * Delete all crash report files
	 * @param context
	 */
	public void deleteCrashFiles(Context context) {
		try {
			String baseFilePath = getBaseFilePath(context);
			String[] crashFilesList = getCrashFilesList(baseFilePath);
	
			for (String crashFile : crashFilesList) {
				//delete all reports files
				File curFile = new File(baseFilePath + File.separator + crashFile);
				curFile.delete();
			}
		} catch (Exception e) {
			//...
		}
	}

	
	
	//---------- Private methods
	
	protected String createCustomInfoString()
	{
		StringBuilder customInfo = new StringBuilder();
		Iterator<String> iterator = mCustomParameters.keySet().iterator();
		while (iterator.hasNext()) {
			String currentKey = iterator.next();
			String currentVal = mCustomParameters.get(currentKey);
			customInfo.append(currentKey)
				.append(" = ")
				.append(currentVal)
				.append(LINE_SEPARATOR);
		}
		return customInfo.toString();
	}

	
	/**
	 * Gathers information from the system and creates a report with them
	 */
	protected String createInformationString()
	{
		String versionName = null;
		String packageName = null;
		String phoneModel = null;
		String androidVersion = null;
		String board = null;
		String brand = null;
		String device = null;
		String display = null;
		String fingerPrint = null;
		String host = null;
		String id = null;
		String model = null;
		String product = null;
		String tags = null;
		long time = 0;
		String type = null;
		String user = null;
		
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi;
			// Version
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			versionName = pi.versionName;
			// Package name
			packageName = pi.packageName;
			// Device model
			phoneModel = android.os.Build.MODEL;
			// Android version
			androidVersion = android.os.Build.VERSION.RELEASE;
			board = android.os.Build.BOARD;
			brand = android.os.Build.BRAND;
			device = android.os.Build.DEVICE;
			display = android.os.Build.DISPLAY;
			fingerPrint = android.os.Build.FINGERPRINT;
			host = android.os.Build.HOST;
			id = android.os.Build.ID;
			model = android.os.Build.MODEL;
			product = android.os.Build.PRODUCT;
			tags = android.os.Build.TAGS;
			time = android.os.Build.TIME;
			type = android.os.Build.TYPE;
			user = android.os.Build.USER;

		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		StringBuffer returnMsg = new StringBuffer();
		returnMsg.append("Version: " + versionName)
			.append(LINE_SEPARATOR)
			.append("Package: " + packageName)
			.append(LINE_SEPARATOR)
			.append("FilePath: " + getBaseFilePath(mContext))
			.append(LINE_SEPARATOR)
			.append("Phone Model: " + phoneModel)
			.append(LINE_SEPARATOR)
			.append("Android Version: " + androidVersion)
			.append(LINE_SEPARATOR)
			.append("Board: " + board)
			.append(LINE_SEPARATOR)
			.append("Brand: " + brand)
			.append(LINE_SEPARATOR)
			.append("Device: " + device)
			.append(LINE_SEPARATOR)
			.append("Display: " + display)
			.append(LINE_SEPARATOR)
			.append("Finger Print: " + fingerPrint)
			.append(LINE_SEPARATOR)
			.append("Host: " + host)
			.append(LINE_SEPARATOR)
			.append("ID: " + id)
			.append(LINE_SEPARATOR)
			.append("Model: " + model)
			.append(LINE_SEPARATOR)
			.append("Product: " + product)
			.append(LINE_SEPARATOR)
			.append("Tags: " + tags)
			.append(LINE_SEPARATOR)
			.append("Time: " + time)
			.append(LINE_SEPARATOR)
			.append("Type: " + type)
			.append(LINE_SEPARATOR)
			.append("User: " + user)
			.append(LINE_SEPARATOR)
			.append("Total Internal memory: " + getTotalInternalMemorySize())
			.append(LINE_SEPARATOR)
			.append("Available Internal memory: " + getAvailableInternalMemorySize())
			.append(LINE_SEPARATOR);
		
		return returnMsg.toString();
	}


	protected long getAvailableInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
     
	protected long getTotalInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * Save the string inside a private file
	 * @param ErrorContent
	 */
	protected void saveAsFile(String ErrorContent) {
		try {
			Random generator = new Random();
			int random = generator.nextInt(99999);
			String fileName = "stack-" + random + ".stacktrace";
			FileOutputStream trace = mContext.openFileOutput( fileName, Context.MODE_PRIVATE);
			trace.write(ErrorContent.getBytes());
			trace.close();
		} catch( Exception e ) {
			// ...
		}
	}

	/**
	 * 
	 * @param baseSearchPath
	 * @return
	 */
	protected String[] getCrashFilesList(String baseSearchPath)
	{
		File dir = new File(baseSearchPath + File.separator);
		// Try to create the files folder if it doesn't exist
		dir.mkdir();
		// Filter for ".stacktrace" files
		FilenameFilter filter = new FilenameFilter() {
		         public boolean accept(File dir, String name) {
		                 return name.endsWith(".stacktrace");
		                 }
		         };
         return dir.list(filter);
	}

	/**
	 * Return base path for private storage
	 * @return
	 */
	protected String getBaseFilePath(Context context) {
		return context.getFilesDir().getAbsolutePath();
	}


	/**
	 * Returns current date in format YYYY-MM-DD
	 * 
	 * @return
	 */
	protected String getCurrentDayHash()
	{
        final Calendar c = Calendar.getInstance();
        StringBuilder dateHash = new StringBuilder();
        dateHash.append(c.get(Calendar.YEAR))
        	.append("-")
        	.append(c.get(Calendar.MONTH))
        	.append("-")
        	.append(c.get(Calendar.DAY_OF_MONTH));
        return dateHash.toString();
	}

	/**
	 * Returns current time in format HH:MM:SS
	 * 
	 * @return
	 */
	protected String getCurrentTimeHash()
	{
        final Calendar c = Calendar.getInstance();
        StringBuilder dateHash = new StringBuilder();
        dateHash.append(c.get(Calendar.HOUR_OF_DAY))
        	.append(":")
        	.append(c.get(Calendar.MINUTE))
        	.append(":")
        	.append(c.get(Calendar.SECOND));
        return dateHash.toString();
	}
}
