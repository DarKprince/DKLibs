package it.dk.libs.logic;

import android.content.Context;
import android.os.Handler;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.ResultOperation;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

/**
 * Read the log related to the application. Could then used for email, report etc
 */
public class PrepareLogToSendThread
	extends BaseBackgroundThread<ResultOperation<String>, String>
{
    //---------- Private fields
	protected final ILogger mBaseLogFacility;
	protected final String mLogTagToSearch;
    protected final CrashReporter mBaseCrashReporter;
    
    


    //---------- Constructors
    public PrepareLogToSendThread(
            Context context,
            Handler handler,
            int handlerMessageWhat,
            ILogger logFacility,
            CrashReporter crashReporter,
            String logTagToSearch)
    {
        super(context, handler, handlerMessageWhat);
        mBaseLogFacility = checkNotNull(logFacility, "LogFacility");
        mBaseCrashReporter = checkNotNull(crashReporter, "CrashReporter");
        mLogTagToSearch = checkNotNullOrEmpty(logTagToSearch, "LogTag to search");
    }




    //---------- Public fields
    



    //---------- Public methods
    @Override
    public ResultOperation<String> executeTask() {
        //collect all the log (normal log + crash report)
//        mResultOperation = LogFacility.getLogData(new String[]{ GlobalDef.LOG_TAG, "AndroidRuntime"});
    	ResultOperation<String> resLog = mBaseLogFacility.getLogData(new String[]{mLogTagToSearch});
    	ResultOperation<String> resCrash = mBaseCrashReporter.getPreviousCrashReports(getContext());

    	ResultOperation<String> result;
        
        //merge two results
        if (!resLog.hasErrors() && !resCrash.hasErrors()) {
            result = new ResultOperation<String>(resCrash.getResult() + resLog.getResult());
        } else if (!resLog.hasErrors()) {
            result = resLog;
        } else if (!resCrash.hasErrors()) {
            result = resCrash;
        } else {
            result = new ResultOperation<String>();
        }
        
        return result;
    }
    



    //---------- Private methods
}
