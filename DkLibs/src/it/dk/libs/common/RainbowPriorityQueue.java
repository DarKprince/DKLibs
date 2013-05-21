/**
 * Copyright 2010-2011, Thotpot Inc.
 */
package it.dk.libs.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A queue where objects can be submitted, but following
 * also a priority ordering criteria, in addition to FIFO order.
 * More priority item are extracted from the queue before the lower
 * priority queue, also if they are put in the queue after the lower
 * priority item.
 * For items with same priority, normal FIFO order is applied
 * 
 * @author Alfredo "Rainbowbreeze" Morresi
 * 
 * @param <E> the type of elements held in this collection
 */
public class RainbowPriorityQueue<E> {
    // ------------------------------------------ Private Fields
    protected final List<PriorityItem> mInternalQueue;

    
    // -------------------------------------------- Constructors
    public RainbowPriorityQueue() {
        mInternalQueue = new ArrayList<PriorityItem>();
    }


    // --------------------------------------- Public Properties
    /** Lowest priority that can be applied to an item */
    public static final int PRIORITY_LOW = 0;

    
    // ------------------------------------------ Public Methods
    /**
     * Retrieves first available item from the queue
     */
    public E get() {
        if(mInternalQueue.size() > 0) {
            PriorityItem pt = mInternalQueue.get(0);
            mInternalQueue.remove(0);
            return pt.getItem();
        } else {
            return null;
        }
    }

    /**
     * Put a new task in the queue, ordering it based on its priority
     * @param task
     */
    public void put(E item, int priority) {
        put(new PriorityItem(item, priority));
    }

    /**
     * Finds if an item is in the queue
     * @param item
     * 
     * @return the item, otherwise null
     */
    public E find(E item) {
        for(PriorityItem pt : mInternalQueue) {
            if (null != pt.getItem()) {
                if (pt.getItem().equals(item)) {
                    return pt.getItem();
                }
            }
        }
        return null;
    }

    /**
     * Change priority of a given item
     * 
     * @param item
     * @param newPriority
     */
    public void changePriority(E item, int newPriority) {
        for(int i=0; i<mInternalQueue.size(); ++i) {
            PriorityItem pt = mInternalQueue.get(i);
            if (null != pt.getItem()) {
                if (pt.getItem().equals(item)) {
                    // We need to change its position
                    mInternalQueue.remove(i);
                    pt.setPriority(newPriority);
                    put(pt);
                    return;
                }
            }
        }
    }

    /**
     * Clear the queue
     * @return
     */
    public void clear() {
        mInternalQueue.clear();
    }

    /**
     * Returns the size of the queue
     * @return
     */
    public int size() {
        return mInternalQueue.size();
    }

    
    // ----------------------------------------- Private Methods
    /**
     * Put a new task in the queue, ordering it based on its priority
     * @param priorityItem
     */
    private void put(PriorityItem priorityItem) {
        int index = 0;
        for(; index<mInternalQueue.size(); index++) {
            PriorityItem pt = mInternalQueue.get(index);
            if(pt.getPriority() < priorityItem.getPriority()) {
                break;
            }
        }
        mInternalQueue.add(index, priorityItem);
    }

    
    // ----------------------------------------- Private Classes
    /**
     * Wrapper around an object, adding priority
     */
    protected class PriorityItem {
        private E mItem;
        private int mPriority;

        public PriorityItem(E job, int priority) {
            this.mItem = job;
            this.mPriority = priority;
        }

        public E getItem() {
            return mItem;
        }

        public int getPriority() {
            return mPriority;
        }

        public void setPriority(int priority) {
            this.mPriority = priority;
        }
    }
}
