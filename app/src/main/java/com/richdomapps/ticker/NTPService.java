package com.richdomapps.ticker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Date;
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
                getOffset();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public long getOffsetFromService() {
        return mOffset;
    }

    private void getOffset(){
        Log.d("get items", "called");
        SntpClient client = new SntpClient();
        long offset = 0;
        if (client.requestTime("pool.ntp.org", 10000)) {
            long systemTime = System.currentTimeMillis();
            long ntpTime = client.getNtpTime() + SystemClock.elapsedRealtime() -
                    client.getNtpTimeReference();
            Date current = new Date(ntpTime);
            Log.d("NTP tag", current.toString());

            offset = (ntpTime - systemTime);

            Log.d("systemTime: ", String.valueOf(systemTime));
            Log.d("ntpTime   : ", String.valueOf(ntpTime));

            Log.d("Offset    : ", String.valueOf(offset));
        }

        mOffset =  offset;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Local Service", "onUnbind Method Called");
        scheduleTaskExecutor.shutdownNow();
        return super.onUnbind(intent);
    }
}
