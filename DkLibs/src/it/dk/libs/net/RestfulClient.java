package it.dk.libs.net;

import it.dk.libs.common.ILogger;
import it.dk.libs.helper.StreamHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static it.dk.libs.common.ContractHelper.checkNotNull;

/**
 * Implements GET and POST requests using Apache HTTP Client
 * 
 * TODO
 * use HttpURLConnection after Gingerbread and Apache library prior to it
 *
 */
public class RestfulClient implements IRestfulClient
{
	//---------- Private fields
	protected static final String LOG_HASH = RestfulClient.class.getSimpleName();
	//indicate that a conversation is in progress
	protected boolean mIsConversationInProgress = false;
	//client used during a conversation
	protected DefaultHttpClient mHttpClient;

	protected final ILogger mBaseLogFacility;



	//---------- Constructors
	public RestfulClient( ILogger logFacility) {
		mBaseLogFacility = checkNotNull(logFacility, "RainbowLogFacility");
	}

	public ServerResponse requestGet(String url)
			throws ClientProtocolException, IOException
			{
		// reference here:
		// http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/

		HttpGet httpGet;
		ServerResponse result;

		// prepare a request object
		httpGet = new HttpGet(url);

		// Get hold of the response entity
		mBaseLogFacility.v(LOG_HASH, "Executing GET request to url " + url);
		result = executeResponse(httpGet);

		return result;
			}


	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.data.IRainbowHttpClient#requestPost(java.lang.String, java.util.HashMap, java.util.HashMap)
	 */
	public ServerResponse requestPost(
			String url,
			HashMap<String, String> headers,
			HashMap<String, String> parameters
			)
					throws ClientProtocolException, IOException, IllegalArgumentException
					{
		// reference here:
		// http://www.androidsnippets.org/snippets/36/
		// another reference, but with different method, here
		// http://www.anddev.org/doing_http_post_with_android-t492.html

		//prepare the post client
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		//create new list of values to pass as header
		if (headers != null) {
			Iterator<String> it = headers.keySet().iterator();
			String k, v;
			while (it.hasNext()) {
				k = it.next();
				v = headers.get(k);
				httpPost.setHeader(k, v);
			}
		}

		//create new list of values to pass as post data
		if (parameters != null) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			Iterator<String> it = parameters.keySet().iterator();
			String k, v;
			while (it.hasNext()) {
				k = it.next();
				v = parameters.get(k);
				nameValuePairs.add(new BasicNameValuePair(k, v));
			}

			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}

		// Execute HTTP Post Request
		ServerResponse result = executeResponse(httpPost);
		return result;
					}


	public ServerResponse requestPost(
			String url,
			HashMap<String, String> headers,
			String jsonData
			)
					throws ClientProtocolException, IOException, IllegalArgumentException
					{

		//prepare the post client
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		//create new list of values to pass as header
		if (headers != null) {
			Iterator<String> it = headers.keySet().iterator();
			String k, v;
			while (it.hasNext()) {
				k = it.next();
				v = headers.get(k);
				httpPost.setHeader(k, v);
			}
		}

		//create new list of values to pass as post data
		if (jsonData != null) {
			StringEntity se = new StringEntity(jsonData);  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(se);
		}

		// Execute HTTP Post Request
		ServerResponse result = executeResponse(httpPost);
		return result;
					}


	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.data.IRainbowHttpClient#startConversation()
	 */
	public void startConversation() {
		mIsConversationInProgress = true;
		mHttpClient = new DefaultHttpClient();
	}

	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.data.IRainbowHttpClient#endConversation()
	 */
	public void endConversation() {
		mIsConversationInProgress = false;
		mHttpClient = null;
	}


	/**
	 * Execute a post request getting data from an {@link InputStream}.
	 * This method uses another wat to post data to a server, very low
	 * protocol level, so away to use it if you can and use
	 * {@link RestfulClient#requestPost(String, HashMap, HashMap) instead.
	 * 
	 * Code credits:
	 * http://reecon.wordpress.com/2010/04/25/uploading-files-to-http-server-using-post-android-sdk/
	 * 
	 * @param url url where send post request
	 * @param boundary boundary fields delimitator
	 * @param in the inputstream with the data
	 */
	public ServerResponse requestPost(String url, String boundary, InputStream in)
			throws MalformedURLException, ProtocolException, IOException {
		URL serverUrl;
		HttpURLConnection connection = null;
		int serverResponseCode = 0;
		String serverResponseMessage = null;
		String serverMessageBody = null;

		mBaseLogFacility.v(LOG_HASH, "Send POST request to address " + url);
		serverUrl = new URL(url);
		connection = (HttpURLConnection) serverUrl.openConnection();

		//allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		connection.setRequestMethod("POST");

		//set other values
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		OutputStream out = null;
		try {
			out = connection.getOutputStream();

			//send data to server
			StreamHelper.appendToStream(in, out, false);

			//responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();
			//          String type = connection.getContentType();
			int len = connection.getContentLength();
			if (len > 0) {
				try {
					InputStream is = connection.getInputStream();
					serverMessageBody = StreamHelper.convertStreamToString(is);
				} catch (Exception e) {
					try {
						//try another method
						Object is = connection.getContent();
						serverMessageBody = is.toString();
					} catch (Exception e1) {
						//nothing to do
					}
				}
			}

		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				//nothing to do here
			}
		}

		return new ServerResponse(serverResponseCode, serverResponseMessage, serverMessageBody);
	}


	@Override
	public ServerResponse downloadFile(String url, OutputStream outputStream)
			throws MalformedURLException, IOException {
		int serverResponseCode = 0;
		String serverResponseMessage = null;
		mBaseLogFacility.v("downloadFile request to address " + url);
		URL imageUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setInstanceFollowRedirects(true);
		InputStream is=conn.getInputStream();
		serverResponseCode = conn.getResponseCode();
		serverResponseMessage = conn.getResponseMessage();
		CopyStream(is, outputStream);
		outputStream.close();
		return new ServerResponse(serverResponseCode, serverResponseMessage);
	}


	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	//---------- Private methods

	/**
	 * Execute the GET or POST request
	 * 
	 */
	protected ServerResponse executeResponse(HttpUriRequest httpRequest)
			throws ClientProtocolException, IOException
			{
		HttpClient httpClient;
		HttpResponse response;
		String responseBody = null;

		if (mIsConversationInProgress) {
			httpClient = mHttpClient;
		} else {
			// create the client
			httpClient = new DefaultHttpClient();
		}

		//httpRequest.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Ubuntu/10.10 Chromium/18.0.1025.142 Chrome/18.0.1025.142 Safari/535.19");
		response = httpClient.execute(httpRequest);

		// Check if server response is valid
		StatusLine status = response.getStatusLine();
		int responseCode = status.getStatusCode();

		// Get hold of the response entity
		HttpEntity entity = response.getEntity();

		// If the response does not enclose an entity, there is no need
		// to worry about connection release
		if (entity != null) {
			InputStream instream = entity.getContent();
			responseBody = StreamHelper.convertStreamToString(instream);
		}

		return new ServerResponse(responseCode, responseBody);
			}
}
