package it.dk.libs.common;

import java.util.ArrayList;
import java.util.List;

import static it.dk.libs.common.ContractHelper.checkNotNull;

public class ServiceLocator {

	private static List<Object>  mServices;

	static {
		mServices = new ArrayList<Object>();
	}

	/**
	 * Get a service from Locator
	 * 
	 * @param serviceToRetrieve
	 */
	public static <T extends Object> T get(Class<T> serviceToRetrieve) {
		checkNotNull(serviceToRetrieve, "Service to retrieve");
		T retrievedService = null;

		for (Object service:mServices) {
			if (serviceToRetrieve.isInstance(service)) {
				retrievedService = serviceToRetrieve.cast(service);
				break;
			}
		}
		return retrievedService;
	}
	
	/**
	 * Put a service inside the Locator
	 * 
	 * @param serviceToAdd
	 */
	public static void put(Object serviceToAdd) {
		checkNotNull(serviceToAdd, "Service to add");
		
		//search if service is already present
		remove(serviceToAdd.getClass());
		//add the service
		mServices.add(serviceToAdd);
	}
	
	
	/**
	 * Remove a service from Locator
	 * 
	 * @param serviceToRemove
	 */
	public static <T extends Object> void remove(Class<T> serviceToRemove) {
		checkNotNull(serviceToRemove, "Service to remove");
	
		//TODO
		//still not covered:
		//-father object in in the list
		//-children object is added in the list
		//-father must be removed and children is added to the list
		for(int i=0; i<mServices.size(); i++) {
			Object service = mServices.get(i);
            if (serviceToRemove.isInstance(service)) {
				mServices.remove(i);
				break;
			}
		}
	}
	
	
	/**
	 * Clean all services inside Locator
	 */
	public static void clear() {
		mServices.clear();
	}

}
