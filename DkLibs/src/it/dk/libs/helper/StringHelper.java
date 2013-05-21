package it.dk.libs.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for string parsing etc
 * 
 * @param <T>
 *
 */
public class StringHelper {
	//---------- Private fields

    //---------- Constructors
    private StringHelper() {}

	//---------- Public properties

    
	//---------- Public methods
	public static String getStringBetween(String source, String tokenBefore, String tokenAfter)
	{
		String result = "";
		
		if (TextUtils.isEmpty(source) || TextUtils.isEmpty(tokenBefore))
			return result;
		
		int posInit = source.indexOf(tokenBefore);
		if (-1 == posInit) return result;
		posInit = posInit += tokenBefore.length();
		
		int posEnd;
		if (TextUtils.isEmpty(tokenAfter)) {
			posEnd = source.length();
		} else {
			posEnd = source.indexOf(tokenAfter, posInit);
			if (-1 == posEnd) posEnd = source.length(); 
		}
		
		result = source.substring(posInit, posEnd);
		
		return result;
	}

	
	/**
	 * Return the latest char of the string before the variable part
	 * @param stringToCheck
	 * @return
	 */
	
	public static String getInvariableStringFinalBoundary(String stringToCheck)
	{
		if (TextUtils.isEmpty(stringToCheck)) return stringToCheck;
		//ok, i know it isn't the best way, but it works as a workaround
		//for the presence of %s parameter in the source message string ;)
		int pos = stringToCheck.indexOf("%");
		return (pos >= 0) ? stringToCheck.substring(0, pos) : stringToCheck;
	}
	
	
	/**
	 * Hides the last digits of a telephone number
	 * @param numberToScramble
	 * @return
	 */
	public static String scrambleNumber(String numberToScramble)
	{
		if (TextUtils.isEmpty(numberToScramble) || numberToScramble.length() < 3) {
			return numberToScramble;
		} else {
			return numberToScramble.substring(0, numberToScramble.length() - 3) + "***";
		}
	}
	
	
    /**
     * Clean phone number from [ -.()<>].
     * 
     * @param phoneNumber
     *            recipient's mobile number
     * @return clean number
     */
    public static String cleanPhoneNumber(final String phoneNumber) {
        if (phoneNumber == null) return "";

        return phoneNumber.replaceAll("[^+0-9]", "").trim();
    }	


	public static String readContentFromResourceFile(Context context, int resourceId)
	throws IOException {
	    StringBuffer sb = new StringBuffer();
	    final String NEW_LINE = System.getProperty("line.separator");
	    
	    InputStream is = context.getResources().openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String readLine = null;

        try {
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
                sb.append(NEW_LINE);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            br.close();
            is.close();
        }
        
        return sb.toString();
	}
	
	/**
	 * Checks if a string has a valid format for beeing an email address
	 * 
	 * @param emailToCheck
	 * @return true is the email format is valid, otherwise false
	 */
	public static boolean isValidEmail(String emailToCheck) {
	    if (TextUtils.isEmpty(emailToCheck)) return false;
	    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
	    Matcher m = p.matcher(emailToCheck);
	    boolean matchFound = m.matches();

	    return matchFound;
	}
	
	public static String formatPercentage(float percentage, int maxDecimal) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(maxDecimal);
        String result = percentFormat.format(percentage);
        return result;
	}
	
	/**
	 * Use regular expression to extract numbers from string
	 * @param str: string input
	 * @return number from str
	 */
	public int extractInt(String str) {
		Matcher matcher = Pattern.compile("\\d+").matcher(str);
		try{
			if (!matcher.find())
				throw new NumberFormatException("For input string [" + str + "]");
		}
		catch (Exception e){
			Log.e("StringHelper","", e);
		}
		return Integer.parseInt(matcher.group());
	}

	//---------- Private methods
}
