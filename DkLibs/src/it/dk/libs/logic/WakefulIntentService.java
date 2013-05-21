package it.dk.libs.logic;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

abstract public class WakefulIntentService extends IntentService {
    public WakefulIntentService(String name) {
        super(name);
    }

    abstract protected void doWakefulWork(Intent intent);
    
    private static final String LOCK_NAME_STATIC="com.commonsware.cwac.wakeful.WakefulIntentService";
    private static PowerManager.WakeLock lockStatic=null;
    
    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic==null) {
            PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            
            lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                        LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }
        
        return(lockStatic);
    }
    
    public static void sendWakefulWork(Context ctxt, Intent i) {
        getLock(ctxt).acquire();
        ctxt.startService(i);
    }
    
    public static void sendWakefulWork(Context ctxt, Class clsService) {
        sendWakefulWork(ctxt, new Intent(ctxt, clsService));
    }
    
    @Override
    final protected void onHandleIntent(Intent intent) {
        try {
            doWakefulWork(intent);
        }
        finally {
            getLock(this).release();
        }
    }
}
