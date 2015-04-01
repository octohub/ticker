package com.richdomapps.ticker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by richard on 12/1/14.
 */
public class NTPService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
    private long mOffset = 0;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        NTPService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NTPService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


// This schedule a runnable task every 1 minute
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                long[] offsets = new long[4];
                for(int i = 0; i<4; i++){
                    Log.d("Run",i+"");
                    long offset = getOffset();
                    if(offset==Long.MAX_VALUE){
                        i-=1;
                    } else {
                        offsets[i] = offset;
                    }
                }
                mOffset = getAverage(offsets);
                Log.d("mOffset", mOffset+"");
            }
        }, 0, 300, TimeUnit.SECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public long getOffsetFromService() {
        return mOffset;
    }

    private long getAverage(long[] offsets){
        Log.d("in here", "getAverage");
        Arrays.sort(offsets);

        for(long element : offsets){
            Log.d("Ordered Offset", String.valueOf(element));
        }

        return (offsets[1] + offsets[2]) / 2;

    }

    private long getOffset(){
        SntpClient client = new SntpClient();
        long offset = Long.MAX_VALUE;
        if (client.requestTime("us.pool.ntp.org", 2000)) {
            long systemTime = System.currentTimeMillis();
            long ntpTime = client.getNtpTime() + SystemClock.elapsedRealtime() -
                    client.getNtpTimeReference();
            offset = (ntpTime - systemTime);
        }
        Log.d("OFFSET",offset+"");
        //mOffset =  offset;
        return offset;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Local Service", "onUnbind Method Called");
        scheduleTaskExecutor.shutdownNow();
        return super.onUnbind(intent);
    }
}
