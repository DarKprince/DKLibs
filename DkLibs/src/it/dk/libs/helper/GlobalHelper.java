package it.dk.libs.helper;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class GlobalHelper {


	public static boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	/**
	 * Get available memory from activity manager 
	 * @return
	 */
	public static long readFreeMem(Context context){
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		return (mi.availMem / 1048576L);
	}

	/**
	 * Calculate the math average from a vector containing integers 
	 * @param sys_cpu_samples
	 * @return
	 */
	public static double getAverage(Vector<Integer> samples) {
		double sum = 0;
		for(int el : samples)
			sum += el;

		return arrotonda(sum / samples.size(), 2);
	}

	/**
	 * Calculate the Math standard deviation from a vector containing integers
	 * @param samples
	 * @param average
	 * @return
	 */
	public static double getStdDeviation(Vector<Integer> samples, double average){
		double sum = 0, temp =0;
		for(int el : samples){
			temp = (el-average);
			sum += (temp * temp);
		}

		return arrotonda(Math.sqrt(sum / (samples.size()-1)), 2);
	}

	/**
	 * Get string of Base64 from bitmap, needed to POST request
	 * @param b
	 * @return
	 */
	public static String getBase64Bitmap(Bitmap b){
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 90, bao);
		byte [] ba = bao.toByteArray();
		return Base64.encodeToString(ba, 0);
	}

	/**
	 * Arrotonda un double d a p cifre decimali
	 * @param d: input
	 * @param p: numero di cifre da visualizzare dopo la virgola
	 * @return
	 */
	public static double arrotonda(double d, int p) 
	{
		return Math.rint(d*Math.pow(10,p))/Math.pow(10,p);
	}

	/**
	 * Takes the result of command TOP and print the CPU statistics for system and
	 * user usage
	 * @return
	 */
	public static String[] readCPU(){
		String read="";
		String [] elems = null ;
		try{
			Process process = Runtime.getRuntime().exec("top -m 5 -d 5 -n 1");
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while(in.readLine()!= null){
				read += in.readLine()+"\n";
			}
			in.close();
			elems = read.split("\n")[1].split(",");
		} catch(Exception ex){
			Log.e("GlobalHelper", "Error while checking CPU", ex);
		}
		return elems;
	}

	/**
	 * Get device's IP address
	 * FIXME: formatter may not work with IP6
	 * @return
	 */
	public static String getIpAddress(Context context){
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return Formatter.formatIpAddress(ipAddress);
	}

	/**
	 * Verify is the device has generic connection
	 * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
	 * 
	 * Need this AndroidManifest permissions
	 *  uses-permission android:name="android.permission.INTERNET"
	 *  uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"
	 *  
	 * @return true if connection is present or in case of problems in connection detection
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != cm && null != cm.getActiveNetworkInfo()) {
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} else {
			//default in case of problems
			return true;
		}
	}

	/**
	 * Verify is the device has specific connection to an host
	 * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
	 * 
	 * @param context
	 * @param hostToCheck: dns name of the host to check
	 * 
	 * @return true if connection is present or in case of problems in connection detection
	 */
	public static boolean isHostAvailable(Context context, String hostToCheck) {
		String completeHostName = 
				hostToCheck.toLowerCase().startsWith("http")
				? hostToCheck
						: "http://" + hostToCheck;

		try {
			URL url = new URL(completeHostName);

			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			//HTC G1 user agent
			urlc.setRequestProperty("User-Agent", "tico e teco");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(10000);
			urlc.setReadTimeout(10000);
			urlc.connect();
			return true;
			//MalformedURLException, IOException and other possible exception
		} catch (Exception e) {
		}

		return false;
	}

	public static String fixDate(String date, String OLD_FORMAT, String NEW_FORMAT) {
		SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
		Date d = null;
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sdf.applyPattern(NEW_FORMAT);
		return sdf.format(d);
	}

}
