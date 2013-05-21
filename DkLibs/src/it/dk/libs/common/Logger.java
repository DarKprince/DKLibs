package it.dk.libs.common;

import android.text.TextUtils;
import android.util.Log;
import it.dk.libs.BuildConfig;

import java.io.*;
import java.util.ArrayList;

import static it.dk.libs.common.ContractHelper.checkNotNull;
/**
 *
 * REMEMBER to set
 *   <uses-permission android:name="android.permission.READ_LOGS" />
 * in the application manifest
 *
 */
public class Logger implements ILogger {

	protected final static String LINE_SEPARATOR =
		System.getProperty("line.separator");		

	/** Default log tag */
	protected final String mTag;


	public Logger(String tag) {
		mTag = checkNotNull(tag, "Log Tag");
	}	
	
	public void e(String message)
	{ log(Log.ERROR, message); }

    public void e(Exception e)
    { log(Log.ERROR, getStackTrace(null, e)); }

    public void e(String methodName, Exception e)
    { log(Log.ERROR, getStackTrace(methodName, e)); }

    public void e(String methodName, String message)
    { log(Log.ERROR, formatSectionName(methodName, message)); }

    public void e(String methodName, String message, Exception e) {
        e(methodName, message);
        e(methodName, e);
    }

    public void i(String message)
	{ log(Log.INFO, message); }

    public void i(String methodName, String message)
    { log(Log.INFO, formatSectionName(methodName, message)); }

	public void v(String message)
	{ log(Log.VERBOSE, message); }
	
	public void v(String methodName, String message)
	{ log(Log.VERBOSE, formatSectionName(methodName, message)); }
	
	public ResultOperation<Void> reset() {
        try{
        	//dump the log and order by tag and by time
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-c");
            
            //execute the command
            Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
        } 
        catch (IOException e){
        	return new ResultOperation<Void>(e, ResultOperation.RETURNCODE_ERROR_GENERIC);
        } 

        return new ResultOperation<Void>();
	}

	public ResultOperation<String> getLogData(String[] tagFilters) {
        final StringBuilder log = new StringBuilder();
        try{
        	//dump the log and order by tag and by time
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-d");
            commandLine.add("-v");
            commandLine.add("tag");
            commandLine.add("-v");
            commandLine.add("time");
            
            //execute the command
            String[] progArray = commandLine.toArray(new String[0]);
            Process process = Runtime.getRuntime().exec(progArray);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            boolean includeLine;
            while ((line = bufferedReader.readLine()) != null){
            	//apply filters
                if (null == tagFilters) {
                	includeLine = true;
                } else {
                	includeLine = false;
                	//for each line
                	for(String tag : tagFilters) {
                		//refine tag string
	                    String tagToFind = "/" + tag;
	                	if (line.contains(tagToFind)) {
	                		includeLine = true;
	                		break;
	                	}
                	}
                }
            	//get logs lines
            	if (includeLine) {
	                log.append(line);
	                log.append(LINE_SEPARATOR);
            	}
            }
        } 
        catch (IOException e){
        	return new ResultOperation<String>(e, ResultOperation.RETURNCODE_ERROR_GENERIC);
        } 

        return new ResultOperation<String>(log.toString());
	}

	public ResultOperation<String> getLogData() {
		return getLogData(null);
	}

	public ResultOperation<String> getApplicationLogData() {
		return getLogData(new String[]{ mTag });
	}

	public void logStartOfActivity(String methodName, Class<? extends Object> activityClass, Object bundleData) {
		String logString = "Activity " + activityClass.getSimpleName() + " is starting ";
		logString = logString.concat(null == bundleData ? "for first time" : "after a restart");
		if (TextUtils.isEmpty(methodName))
            v(logString);
		else
		    v(methodName, logString);
	}

    public void logStartOfActivity(Class<? extends Object> activityClass, Object bundleData) {
        logStartOfActivity(null, activityClass, bundleData);
    }

	protected void log(int level, String msg) {
		String msgToLog = TextUtils.isEmpty(msg) ? "Empty message to log" : msg;
		
		switch (level){
		case Log.ERROR:
			Log.e(mTag, msgToLog);
			break;
		case Log.INFO:
		    if (BuildConfig.DEBUG) {
		        Log.i(mTag, msgToLog);
		    }
			break;
		case Log.VERBOSE:
            if (BuildConfig.DEBUG) {
                Log.v(mTag, msgToLog);
            }
			break;
		}
	}
	
	protected String formatSectionName(String methodName, String message) {
	    if (TextUtils.isEmpty(methodName)) {
	        return message;
	    } else {
	        return "[" + methodName + "] " + message;
	    }
	}
	
	protected String getStackTrace(String methodName, Exception e) {
	    StringBuilder sb = new StringBuilder();
        //log the message
        sb.append(formatSectionName(methodName, "--- Errortrace ---\n"));
        if (e != null) {
            sb.append(formatSectionName(methodName, e.getMessage())).append("\n");
            //and the stack trace
            final Writer result = new StringWriter();
            e.printStackTrace(new PrintWriter(result));
            sb.append(formatSectionName(methodName, result.toString()));
//            StackTraceElement[] items = e.getStackTrace();
//            for (StackTraceElement item : items) {
//                sb.append(formatSectionName(methodName, " at "));
//                sb.append(item.toString()).append("\n");
//            }
        }
        sb.append(formatSectionName(methodName, "------------------"));
        return sb.toString();
	}

}
