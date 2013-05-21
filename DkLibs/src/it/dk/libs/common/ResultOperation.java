package it.dk.libs.common;

/**
 * Class for result operation.
 * Similar to http reply, where there are a return code
 * and a reply
 */
public class ResultOperation<ResultType>
{
	public ResultOperation() {
		mReturnCode = RETURNCODE_OK;
	}

	public ResultOperation(int errorReturnCode) {
		mReturnCode = errorReturnCode;
	}

	public ResultOperation(Exception ex, int errorReturnCode) {
		mReturnCode = errorReturnCode;
		mException = ex;
	}

	public ResultOperation(ResultType value) {
		this(RETURNCODE_OK, value);
	}

	public ResultOperation(int returnCode, ResultType value) {
		setReturnCode(returnCode);
		setResult(value);
	}

	/** All done! */
	public final static int RETURNCODE_OK = 200;
    /** First free value for custom operation (no error) code */
    public final static int RETURNCODE_OPERATION_FIRST_USER = 210;

	/** Errors code, all codes are over 400, please insert custum code from 450 to above */
	public final static int RETURNCODE_ERROR_GENERIC = 400;
	public static final int RETURNCODE_ERROR_APPLICATION_ARCHITECTURE = 401;
	public static final int RETURNCODE_ERROR_COMMUNICATION = 402;
	public static final int RETURNCODE_APP_EXPIRED = 403;
	/** First free value for custom error code */
	public static final int RETURNCODE_ERROR_FIRST_USER = 500;
	
	/** A good result, but without additional data to carry on */
	public static ResultOperation OK = new ResultOperation();
	

	ResultType mResult;
	public ResultType getResult()
	{return mResult; }
	public void setResult(ResultType newValue)
	{ mResult = newValue; }
	
	protected int mReturnCode;
	public int getReturnCode()
	{ return mReturnCode; }
	public void setReturnCode(int newValue)
	{ mReturnCode = newValue; }
	
	
	protected Exception mException;
	public Exception getException()
	{ return mException; }
	public void setException(Exception newValue, int returnCode)
	{
		mException = newValue;
		mReturnCode = returnCode;
	}
	
	
	/**
	 * Return if the object contains error
	 */
	public boolean hasErrors()
	{ return null != mException || mReturnCode >= 400; }
	
	/**
	 * Move Exception and return code to another {@link ResultOperation}
	 * but with a different type.
	 * It's important to maintain same firm of this method in order to allow
	 * derived class to declare they own {@link #translateError(ResultOperation)}
	 * method with a different parameters used by overload mechanism.
	 * 
	 * @param <NewResultType>
	 * @param newResultOperation
	 * @return
	 */
	public <NewResultType> ResultOperation<NewResultType> translateError(ResultOperation<NewResultType> newResultOperation) {
		if (null == newResultOperation) newResultOperation = new ResultOperation<NewResultType>();
		newResultOperation.setException(mException, mReturnCode);
		return newResultOperation;
	}

}
