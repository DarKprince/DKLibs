package it.dk.libs.common;

public interface ILogger {

    //---------- Public methods
    public void e(String message);

    public void e(Exception e);

    public void e(String methodName, Exception e);

    public void e(String methodName, String message);

    public void e(String methodName, String message, Exception e);

    public void i(String message);

    public void i(String methodName, String message);

    public void v(String message);

    public void v(String methodName, String message);

    /**
     * Reset the log
     * @return
     */
    public ResultOperation<Void> reset();

    /**
     * Return only logs that belong to the specified tags
     * 
     * @param tagFilters array of string with tags to include. null to include all
     * @return
     */
    public ResultOperation<String> getLogData(String[] tagFilters);

    /**
     * Return all the log content
     * @return
     */
    public ResultOperation<String> getLogData();

    /**
     * Return the log content related to the application
     * @return
     */
    public ResultOperation<String> getApplicationLogData();

    /**
     * Log the start of the activity
     * 
     * @param methodName
     * @param activityClass
     * @param bundleData
     */
    public void logStartOfActivity(String methodName,
            Class<? extends Object> activityClass, Object bundleData);

    public void logStartOfActivity(Class<? extends Object> activityClass,
            Object bundleData);

}