package it.dk.libs.logic;

import android.app.Application;
import android.content.Context;
import it.dk.libs.common.Logger;
import it.dk.libs.common.ResultOperation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Istance of Application to override definitions and do operations when
 * the app is initialized
 * @author saverio guardato
 */

public class BaseApplication extends Application{

	private static final String TAG = "MyApplication";
	private Context mContext;
	protected CrashReporter mCR;
	protected Logger mLog;
	protected ResultOperation<String> mResultOperation;
	public static Vector <String> mLogQueue;


	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		mLogQueue = new Vector<String>();
		mLog = AppEnvironment.getInstance(mContext).getLogFacility();
		mLog.i(TAG, "start application. check crash precedenti..");
		mCR = AppEnvironment.getInstance(mContext).getCrashReporter();
		if(mCR.isCrashReportPresent(mContext))
			getLog();		
	}

	/**
	 * Method to get log after a crash to report
	 * 
	 */
	private void getLog() {
		Thread t = new Thread(new Runnable(){

			public void run() {
				mLog.i(TAG, "getLog...");
				//questo metodo preleva il contenuto del logcat
				ResultOperation<String> resLog = getLogData(new String[]{TAG});
				//quest'altro metodo preleva lo stacktrace racolto dal crashHandler
				ResultOperation<String> resCrash = mCR.getPreviousCrashReports(mContext);

				//merge two results
				if (!resLog.hasErrors() && !resCrash.hasErrors()) {
					mResultOperation = new ResultOperation<String>(resCrash.getResult() + resLog.getResult());
				} else if (!resLog.hasErrors()) {
					mResultOperation = resLog;
				} else if (!resCrash.hasErrors()) {
					mResultOperation = resCrash;
				} else {
					mResultOperation = new ResultOperation<String>();
				}

				if(!mResultOperation.hasErrors()){
					pushLog(mResultOperation.getResult());
				}
			}
		});
		t.start();
	}

	/**
	 * Push a new log request in vector
	 * @param result
	 */
	protected void pushLog(String result) {
		mLogQueue.add(result);
	}

	/**
	 * Metodo che preleva dal logCat le righe filtrate dal Tag scelto.
	 * Le righe restituite possono essere utilizzate per inviare report
	 * e visualizzarlo senza ADB.
	 * E' necessario il permesso nel manifest READLOG
	 * 
	 * @author Saverio Guardato
	 * @param tag filtro da selezionate. se null, preleva tutto il log
	 * @return
	 */
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
					log.append(CrashReporter.LINE_SEPARATOR);
				}
			}
		} 
		catch (IOException e){
			return new ResultOperation<String>(e, ResultOperation.RETURNCODE_ERROR_GENERIC);
		} 

		return new ResultOperation<String>(log.toString());
	}


}
