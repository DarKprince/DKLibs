package it.dk.libs.net;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 *
 */
public interface IRestfulClient {

    /**
     * Send to the webservice a GET request
     * 
     * @param url
     *            the Url of the webservice
     *            
     * @return the string returned from the webservice
     *
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public ServerResponse requestGet(String url)
    throws ClientProtocolException, IOException;

    /**
     * Send to the webservice a POST request
     * 
     * @param url the Url of the webservice
     * @param parameters the data to pass in post via parameters
     * @param headers the data to pass in post via headers
     *
     * @return the string returned from the webservice
     * 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public ServerResponse requestPost(String url, HashMap<String, String> headers,
            HashMap<String, String> parameters)
    throws ClientProtocolException, IOException, IllegalArgumentException;
    
    /**
     * Download a files to the given {@link OutputStream}
     * 
     * @param url
     * @param outputStream
     * 
     * @return null if the download terminated successfully, otherwise
     * webserver response
     */
    public ServerResponse downloadFile(String url, OutputStream outputStream)
    throws MalformedURLException, IOException;

    
    /**
     * Start a conversation, where cookies and other states are
     * preserver between a request (get or post) and the following
     * 
     * To stop a conversation, call stopConversation
     */
    public void startConversation();

    /**
     * End a conversation previously started
     */
    public void endConversation();
    
    
    //---------- Public classes
    /**
     * Represents the response obtained from the server
     */
    public class ServerResponse {
        private final int mResponseCode;
        private final String mResponseMessage;
        private final String mMessageBody;

        /**
         * Server response status code, as defined by the HTTP status codes RFC
         */
        public int getResponseCode() { return mResponseCode; }
        /**
         * Server response message
         */
        public String getResponseMessage() { return mResponseMessage; }
        /**
         * Server message body
         */
        public String getMessageBody() { return mMessageBody; }
        
        public ServerResponse(int responseCode, String responseMessage) {
            mResponseCode = responseCode;
            mResponseMessage = responseMessage;
            mMessageBody = null;
        }

        public ServerResponse(int responseCode, String responseMessage, String messageBody) {
            mResponseCode = responseCode;
            mResponseMessage = responseMessage;
            mMessageBody = messageBody;
        }
    }
    
    /**
     * Used to monitor a download operation
     */
    public interface DownloadListener {
        /**
         * Reports that a new download operation started.
         * @param totalSize the total size of bytes to download
         */
        public void downloadStarted(long totalSize);

        /**
         * Reports the progress of a download operation.
         * @param size the total number of bytes received from the beginning
         */
        public void downloadProgress(long size);

        /**
         * Reports that a download operation ended.
         */
        public void downloadEnded();
    }
}
