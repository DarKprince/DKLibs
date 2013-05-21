package it.dk.libs.logic;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import it.dk.libs.common.ResultOperation;

import java.lang.ref.WeakReference;

/**
 * Execute a generic background task, with the possibility of
 * upgrade UI
 * 
 * Generics <ResultValueType> is for type of result to return
 *
 */
public abstract class BaseBackgroundThread<ResultOperationComplex extends ResultOperation<ResultValueType>, ResultValueType>
	extends Thread
{
	//---------- Private fields
	/** How many times should try to call caller activity handler */
	protected static final int TOTAL_RETRIES = 3;
	/** Delay between one call to caller activity handler and the other */
	protected static final int INTERVAL_BETWEEN_RETRIES = 2000;
	
	protected WeakReference<Context> mWeakContext; 
	protected ResultOperationComplex mResultOperation;
	protected WeakReference<Handler> mCallerHandler;
	protected final int mMessageWhat;

	
	

    //---------- Constructors
	
	/**
	 * @param context application/activity context
	 * @param handler reference to the handler in the calling activity
	 * @param handlerMessageWhat id of the message generated for the handler
	 */
    public BaseBackgroundThread(Context context, Handler handler, int handlerMessageWhat) {
        registrerNewContext(context);
        registerCallerHandler(handler);
        mMessageWhat = handlerMessageWhat; 
    }




	//---------- Public fields
	


	//---------- Public methods
	/**
	 * Register new context
	 */
	public void registrerNewContext(Context context) {
		mWeakContext = new WeakReference<Context>(context);
	}
	
	public void registerCallerHandler(Handler newHandler)
	{ mCallerHandler = new WeakReference<Handler>(newHandler); }

	public void unregisterCallerHandler()
	{ mCallerHandler = null; }

	public ResultOperationComplex getResult()
	{ return mResultOperation; }


	public void run() {
	    mResultOperation = executeTask();
	    callHandlerAndRetry(mMessageWhat);
	}
	
	public abstract ResultOperationComplex executeTask();



	//---------- Private methods

    /**
	 * Get the caller context from the WeakReference object
	 */
	protected Context getContext()
	{ return mWeakContext.get(); }
	
	
	/**
	 * Call the caller activity handler retrying some times if the handler is
	 * still null
	 * @param messageCode
	 * @param arg1
	 * @param arg2
	 * 
	 */
	protected void callHandlerAndRetry(int messageCode, int arg1, int arg2)
	{
		for (int retryCounter = 0; retryCounter < TOTAL_RETRIES; retryCounter++) {
			if (null != mCallerHandler && null != mCallerHandler.get()) {
				Message message = mCallerHandler.get().obtainMessage(messageCode);
				message.arg1 = arg1;
				message.arg2 = arg2;
				mCallerHandler.get().sendMessage(message);
				break;
			}
			//what some times, maybe next time activity is ready
			try {
				Thread.sleep(INTERVAL_BETWEEN_RETRIES);
			} catch (InterruptedException ignoreExcepition) {
			}
		}
	}

	/**
	 * Call the caller activity handler retrying some times if the handler is
	 * still null
	 * @param messageCode
	 */
	protected void callHandlerAndRetry(int messageCode) {
		callHandlerAndRetry(messageCode, 0, 0);
	}
}
