package it.dk.libs.logic;

import java.util.HashMap;

public abstract class BaseAppState {

	//dictionary activity ---> data
	protected HashMap<String, HashMap<Object, Object>> mStateMap;

	public BaseAppState() {
		//mData = new HashMap<Object, Object>();
		mStateMap = new HashMap<String, HashMap<Object,Object>>();
	}

	/**
	 * Store dictionary data in general Map 
	 * @param owner: the activity who is storing data
	 * @param data: the effective dictionary key-value of data
	 */
	public void storeData(Class<?> owner, HashMap<Object, Object> data){
		mStateMap.put(owner.getSimpleName(), data);
	}

	/**
	 * Get the HashMap<Object, Object> stored by owner 
	 * @param owner
	 * @return
	 */
	public HashMap<Object, Object> getData(Class<?> owner){
		return mStateMap.get(owner.getSimpleName());
	}

	/**
	 * Get a specific value stored in a dictionary by an owner
	 * @param owner
	 * @param key
	 * @return
	 */
	public Object getValue(Class<?> owner, Object key){
		Object value = null;
		HashMap<Object, Object> mData = getData(owner);
		value = mData.get(key);
		return value;
	}

	/**
	 * Create a HashMap<Object, Object> from arrays of key-values
	 * @param keys
	 * @param values
	 */
	public HashMap<Object, Object> createDataDictionary(Object [] keys, Object [] values){
		if(keys.length != values.length)
			throw new IllegalArgumentException("Array must have the same size");
		//delete any previous value
		HashMap<Object, Object> mData = new HashMap<Object, Object>();
		for (int i = 0; i < keys.length; i++) {
			mData.put(keys[i], values[i]);
		}
		return mData;
	}

	public abstract boolean loadStateFromFile(Class<?> owner, String fileName);

	public abstract boolean saveStateToFile(Class<?> owner, String fileName);

}
