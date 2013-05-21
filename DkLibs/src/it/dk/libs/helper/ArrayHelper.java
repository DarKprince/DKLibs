package it.dk.libs.helper;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for array
 *
 */
public class ArrayHelper {
    //---------- Private fields
    private static final String STANDARD_SEPARATOR = ", ";

    //---------- Constructors
    private ArrayHelper() {}
	
	
	//---------- Public properties
	
		
	//---------- Public methods
	/**
	* Reallocates an array with a new size, and copies the contents
	* of the old array to the new array.
	* @param oldArray  the old array, to be reallocated.
	* @param newSize   the new array size.
	* @return          A new array with the same contents.
	*/
    public static Object resizeArray (Object oldArray, int newSize) {
		//TODO review with generics
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(
		elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
		System.arraycopy (oldArray,0,newArray,0,preserveLength);
		return newArray;
	}
	
	/**
	 * Convert into an array of long
	 * 
	 * @param sourceList
	 * @return
	 */
	public static long[] convertToArray(List<Long> sourceList) {
	    if (null == sourceList) return null;
	    long[] result = new long[sourceList.size()];
	    for(int i=0; i<sourceList.size(); i++) {
	        result[i] = sourceList.get(i);
	    }
	    return result;
	}
    
    
    /**
     * Convert into an {@link ArrayList} of {@link Long}
     * 
     * @param sourceArray
     * @return
     */
    public static List<Long> convertToList(long[] sourceArray) {
        if (null == sourceArray) return null;
        List<Long> result = new ArrayList<Long>(sourceArray.length);
        for(int i=0; i<sourceArray.length; i++) {
            result.add(sourceArray[i]);
        }
        return result;
    }
    
    /**
     * Convert into an {@link ArrayList} of {@link Long}
     * 
     * @param sourceArray
     * @return
     */
    public static List<Integer> convertToList(int[] sourceArray) {
        if (null == sourceArray) return null;
        List<Integer> result = new ArrayList<Integer>(sourceArray.length);
        for(int i=0; i<sourceArray.length; i++) {
            result.add(sourceArray[i]);
        }
        return result;
    }
    
	/**
	 * Compares two lists, if they have same items. Items comparison is made
	 * using {@link Object#equals(Object)} method.
	 * 
	 * @param <T>
	 * @param firstList first list to compare
	 * @param secondList second list to compare
	 * @param sameOrder items must have same order. Based on {@link List} 
	 *  characteristics, should be always off. 
	 * @return
	 */
    public static <T extends Object> boolean areEquals(
            List<T> firstList,
            List<T> secondList,
            boolean sameOrder) {
        
        if (null == firstList && null == secondList) return true;
        if (null == firstList && null != secondList) return false;
        if (null != firstList && null == secondList) return false;

        if (firstList.size() != secondList.size()) return false;
        
        if (0 == firstList.size()) return true;
        
        //elements it same order
        if (sameOrder) {
            //compares elements one by one
            for(int index=0; index<firstList.size(); index++) {
                if (!compareItems(firstList.get(index), secondList.get(index))) return false;
            }
        
        //elements in different orders
        } else {
            boolean[] actualItemsExamined = new boolean[secondList.size()];
            for(int i=0; i<actualItemsExamined.length; i++) {
                actualItemsExamined[i] = false;
            }
            
            for(int expectedIndex=0; expectedIndex<firstList.size(); expectedIndex++) {
                T expectedElement = firstList.get(expectedIndex);
                boolean findSameElement = false;
                for(int actualIndex=0; actualIndex<secondList.size(); actualIndex++) {
                    //next element if current was already analyzed
                    if (actualItemsExamined[actualIndex]) continue;
                    T actualElement = secondList.get(actualIndex);
                    //compare also null case
                    if (compareItems(expectedElement, actualElement)) {
                        actualItemsExamined[actualIndex] = true;
                        findSameElement = true;
                        break;
                    }
                }
                if (!findSameElement) return false;
            }
        }
        
        //at the end, list are equals
        return true;
    }
	
    /**
     * Join an array of generic object into a single string. Each item is converted
     * using its {@code toString()} method and a comma is used as separator.
     * 
     * @param <T>
     * @param arrayToJoin
     * @param separator
     * @return
     */
    public static <T extends Object> String join(T[] arrayToJoin) {
        return join(arrayToJoin, STANDARD_SEPARATOR);
    }
    
    /**
     * Join an array of generic object into a single string. Each item is converted
     * using its {@code toString()} method and a custom separator is used.
     * 
     * @param <T>
     * @param arrayToJoin
     * @param separator
     * @return
     */
    public static <T extends Object> String join(T[] arrayToJoin, CharSequence separator) {
        if (null == arrayToJoin) return null;
        if (0 == arrayToJoin.length) return null;
        if (TextUtils.isEmpty(separator)) return null;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayToJoin.length; i++) {
            sb.append(arrayToJoin[i]);
            if (i != arrayToJoin.length -1) sb.append(separator);
        }
        return sb.toString();
    }
    
    /**
     * Joins a list of object into a single string. Each item is converted
     * using its {@code toString()} method and a comma separator is used.
     * 
     * @param <T>
     * @param listToJoin
     * @return A string with all the values
     */
    public static <T extends Object> String join(List<T> listToJoin) {
        return join(listToJoin, STANDARD_SEPARATOR);
    }
    
    /**
     * Joins a list of object into a single string. Each item is converted
     * using its {@code toString()} method and a custom separator is used.
     * 
     * @param <T>
     * @param listToJoin
     * @param separator the separator to use
     * @return A string with all the values
     */
    public static <T extends Object> String join(List<T> listToJoin, CharSequence separator) {
        if (TextUtils.isEmpty(separator) || null == listToJoin || 0 == listToJoin.size()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listToJoin.size(); i++) {
            sb.append(listToJoin.get(i));
            if (i != listToJoin.size() -1) sb.append(separator);
        }
        return sb.toString();
    }
    
    /**
     * Transform array of int in a string, separated by commas
     *
     * @param arrayToTransform
     */
    public static String join(int[] arrayToTransform) {
        return join(arrayToTransform, STANDARD_SEPARATOR);
    }
    
    /**
     * Transform array of int in a string, separated by a custom string.
     *
     * @param arrayToTransform
     * @param separator
     */
    public static String join(int[] arrayToTransform, String separator) {
        if (null == arrayToTransform) return null;
        if (0 == arrayToTransform.length) return null;
        if (TextUtils.isEmpty(separator)) return null;
        
        StringBuilder sb = new StringBuilder();
        for(int item: arrayToTransform) {
            sb.append(item);
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        
        return sb.toString();
    }
    
    /**
     * Transform array of long in a string, separated by commas
     *
     * @param arrayToTransform
     */
    public static String join(long[] arrayToTransform) {
        return join(arrayToTransform, STANDARD_SEPARATOR);
    }
    
    /**
     * Transform array of long in a string, separated by a custom string.
     *
     * @param arrayToTransform
     * @param separator
     */
    public static String join(long[] arrayToTransform, String separator) {
        if (null == arrayToTransform) return null;
        if (0 == arrayToTransform.length) return null;
        if (TextUtils.isEmpty(separator)) return null;
        
        StringBuilder sb = new StringBuilder();
        for(long item: arrayToTransform) {
            sb.append(item);
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        
        return sb.toString();
    }
    
    /**
     * Transform array of float in a string, separated by commas
     *
     * @param arrayToTransform
     */
    public static String join(float[] arrayToTransform) {
        return join(arrayToTransform, STANDARD_SEPARATOR);
    }
    
    /**
     * Transform array of float in a string, separated by a custom string.
     *
     * @param arrayToTransform
     * @param separator
     */
    public static String join(float[] arrayToTransform, String separator) {
        if (null == arrayToTransform) return null;
        if (0 == arrayToTransform.length) return null;
        if (TextUtils.isEmpty(separator)) return null;
        
        StringBuilder sb = new StringBuilder();
        for(float item: arrayToTransform) {
            sb.append(item);
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        
        return sb.toString();
    }
    
    /**
     * Splits a string to a {@link List} separated by a comma.
     * 
     * @param contatenatedValue
     * @param listItemClass
     * @return
     */
    public static <ItemType extends Object> List<ItemType> splitToList(
            String contatenatedValue,
            Class<ItemType> itemType) {
        return splitToList(contatenatedValue, STANDARD_SEPARATOR, null, itemType);
    }
    
    /**
     * Splits a string to a {@link List} separated by a comma.
     * 
     * @param contatenatedValue
     * @param listItemClass
     * @return
     */
    public static <ItemType extends Object> List<ItemType> splitToList(
            String contatenatedValue,
            List<ItemType> listWhereAdd,
            Class<ItemType> itemType) {
        return splitToList(contatenatedValue, STANDARD_SEPARATOR, listWhereAdd, itemType);
    }
    
    /**
     * Splits a string to a {@link List} separated by a custom string.
     * 
     * @param contatenatedValue
     * @param separator
     * @param listItemClass
     * @return
     */
    public static <ItemType extends Object> List<ItemType> splitToList(
            String contatenatedValue,
            String separator,
            Class<ItemType> itemType) {
        return splitToList(contatenatedValue, separator, null, itemType);
    }
    
    /**
     * Splits a string to a {@link List} separated by a custom string.
     * 
     * @param contatenatedValue
     * @param separator
     * @param listWhereAdd in input list to use. If null, a new {@link ArrayList}
     *  list will be created.
     * @return the input list with added items
     */
    public static <ItemType extends Object> List<ItemType> splitToList(
            String contatenatedValue,
            String separator,
            List<ItemType> listWhereAdd,
            Class<ItemType> itemType) {
        if (null == listWhereAdd) {
            listWhereAdd = new ArrayList<ItemType>();
        }
        if (TextUtils.isEmpty(contatenatedValue) || TextUtils.isEmpty(separator)) {
            listWhereAdd.clear();
            return listWhereAdd;
        }
        
        String[] values = contatenatedValue.split(separator);
        for (String value : values) {
            ItemType item = convertToGenericType(value, itemType);
            listWhereAdd.add(item);
        }
        return listWhereAdd;
    }
    
    /**
     * Splits a string to an array of integer separated by a custom string.
     * 
     * @param contatenatedValue
     * @param separator separator, regular expression
     */
    public static int[] splitToIntArray(
            String contatenatedValue,
            String separator) throws NumberFormatException {
        
        if (TextUtils.isEmpty(contatenatedValue)) return new int[0];
        String[] values = contatenatedValue.split(separator);
        int[] converted = new int[values.length];
        for (int i=0; i<values.length; i++) {
            int value = Integer.parseInt(values[i]);
            converted[i] = value;
        }
        return converted;
    }
    
    /**
     * Splits a string to an array of float separated by a custom string.
     * 
     * @param contatenatedValue
     * @param separator separator, regular expression
     */
    public static float[] splitToFloatArray(
            String contatenatedValue,
            String separator) throws NumberFormatException {
        
        if (TextUtils.isEmpty(contatenatedValue)) return new float[0];
        String[] values = contatenatedValue.split(separator);
        float[] converted = new float[values.length];
        for (int i=0; i<values.length; i++) {
            float value = Float.parseFloat(values[i]);
            converted[i] = value;
        }
        return converted;
    }
    
    /**
     * Finds if a specific value is contained inside an array
     * @param valueToSearch
     * @param arrayWhereSearch
     * @return
     */
    public static boolean contains(int valueToSearch, int[] arrayWhereSearch) {
        if (null == arrayWhereSearch) return false;
        for (int currentValue : arrayWhereSearch) {
            if (currentValue == valueToSearch) return true;
        }
        return false;
    }
    
    
	//---------- Private methods

    private static boolean compareItems(Object expected, Object actual) {
        if (null == expected && null == actual) return true;
        return expected.equals(actual);
    }
    
    /**
     * Converts a String object to a particular object type.
     * 
     * @param <ItemType>
     * @param stringReprestation
     * @param objectClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <ItemType extends Object> ItemType convertToGenericType(String stringReprestation, Class<ItemType> objectClass) {
        
        try {
            if (objectClass == String.class) {
                return (ItemType) stringReprestation;
            } else if (objectClass == Long.class) {
                return (ItemType) Long.valueOf(stringReprestation);
            } else if (objectClass == Integer.class) {
                return (ItemType) Integer.valueOf(stringReprestation);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

//    private static <T extends Object> T convertBack(String stringRaprestation, T objectType) {
//        return objectType;
//    }
}
