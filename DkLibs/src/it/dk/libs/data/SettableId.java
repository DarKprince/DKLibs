package it.dk.libs.data;

import android.content.ContentProvider;

/**
 * Base interface for POJO object that can be stored inside a {@link ContentProvider}
 * 
 */
public interface SettableId {

    /**
     * Can set the unique identifier of the entity
     * @param newValue
     */
    public void setId(long newValue);
    
    public long getId();
}
