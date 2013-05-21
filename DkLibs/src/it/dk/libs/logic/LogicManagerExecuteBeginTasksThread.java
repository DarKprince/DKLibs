package it.dk.libs.logic;

import android.content.Context;
import android.os.Handler;
import it.dk.libs.common.ResultOperation;

import static it.dk.libs.common.ContractHelper.checkNotNull;
/**
 * Executes begin thread
 *
 */
public class LogicManagerExecuteBeginTasksThread
	extends BaseBackgroundThread<ResultOperation<Void>, Void>
{

    //---------- Private fields
    private final LogicManager mBaseLogicManager;

    
    
    
    //---------- Constructor
    /**
     * @param context
     * @param handler
     * @param logicManager
     */
    public LogicManagerExecuteBeginTasksThread(
            Context context,
            Handler handler,
            int handlerMessageWhat,
            LogicManager logicManager) {
        super(context, handler, handlerMessageWhat);
        mBaseLogicManager = checkNotNull(logicManager, "LogicManager");
    }


    //---------- Public properties


    
    
    //---------- Public methods
    /* (non-Javadoc)
     * @see it.rainbowbreeze.libs.logic.BaseBackgroundThread#executeTask()
     */
    @Override
    public ResultOperation<Void> executeTask() {
    	ResultOperation<Void> result = mBaseLogicManager.executeBeginTasks(getContext());
        
        //and call the caller activity handler when the execution is terminated
        return result;       
    }
   
    

    
    //---------- Private methods
}
