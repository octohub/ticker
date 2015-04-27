package com.richdomapps.ticker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class MainActivity extends ActionBarActivity {
    //arbitrary change for guest branch

    private ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
    private static TextView offsetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    Handler startFullScreenActivityHandler = new Handler();
    Runnable startFullScreenActivityRunnable = new Runnable() {
        @Override
        public void run() {
            openFullScreenActivity();
        }

    };

    Handler getOffSetHandler = new Handler();
    Runnable getOffSetRunnable = new Runnable() {
        @Override
        public void run() {
            new FinalOffsetTask().execute();
//            start();
//            openFullScreenActivity();
        }

    };

    public int getWaitTimeBeforeGetOffSet(){
        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int minute = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 50);

        //long currTime = System.currentTimeMillis()+offset;
        //long waitTime = playTime-currTime;

        long startTime = calendar.getTimeInMillis();
        long waitTimeLong = startTime - currentTime;
        int  waitTimeInt = (int) waitTimeLong;
        return waitTimeInt;

    }

    @Override
    protected void onResume() {
        super.onResume();
        getOffSetHandler.removeCallbacks(getOffSetRunnable);
        int waitTimeInt = getWaitTimeBeforeGetOffSet();
        getOffSetHandler.postDelayed(getOffSetRunnable, waitTimeInt);
        new InitialOffsetTask().execute();

        new CountDownTimer(waitTimeInt, 1000) {

            public void onTick(long millisUntilFinished) {
                offsetTextView.setText(initialLine + "Calculating Final Offset in: " + millisUntilFinished / 1000 + " seconds");
            }

            public void onFinish() {
                offsetTextView.setText(initialLine + "Calculating Final Offset in: 0 seconds \nCalculating Final Offset");
            }
        }.start();

    }

    public void fullScreenActivity(View view){
        long currentTime;

        currentTime = System.currentTimeMillis() + initialOffset;



        Log.d("currentTime",String.valueOf(currentTime));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int minute = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 58);

        //long currTime = System.currentTimeMillis()+offset;
        //long waitTime = playTime-currTime;

        long startTime = calendar.getTimeInMillis();
        long waitTimeLong = startTime - currentTime;
        int  waitTimeInt = (int) waitTimeLong;

        startFullScreenActivityHandler.removeCallbacks(startFullScreenActivityRunnable);
        startFullScreenActivityHandler.postDelayed(startFullScreenActivityRunnable, waitTimeInt);

        new CountDownTimer(waitTimeInt + 2000, 1000) { //add 2000 because its 2 seconds after opening new activity

            public void onTick(long millisUntilFinished) {
                offsetTextView.setText(initialLine + "Calculating Final Offset in: 0 seconds \nCalculating Final Offset" +
                        "\nFinal Offset is: " + finalOffset + "ms" +
                        "\nStarting Music in:  " + millisUntilFinished / 1000 + " seconds");
            }

            public void onFinish() {
                //offsetTextView.setText("Calculating Offset in: 0 seconds\nCalculating Offset");
            }
        }.start();

    }

    public void openFullScreenActivity(){
        Intent intent = new Intent(this, FullscreenActivity.class);
        //finalOffset = Long.MAX_VALUE; leave this commented unless testing that finalOffset failed, fall back on initialOffset
        if(finalOffset != Long.MAX_VALUE){
            intent.putExtra("offset", finalOffset);;

        } else {
            Log.d("Using", "IINNITITAL");
            intent.putExtra("offset", initialOffset);;

        }
        //intent.putExtra("offset", finalOffset);
        startActivity(intent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private long start() {
        long[] offsets = new long[3];
        for(int i = 0; i<3; i++){
            Log.d("Run",i+"");
            long offset = getOffset();
            if(offset==Long.MAX_VALUE){
                i-=1;
            } else {
                offsets[i] = offset;
            }
        }
        long offset = getAverage(offsets);
        return offset;

    }

    private long getAverage(long[] offsets){
        Log.d("in here", "getAverage");
        Arrays.sort(offsets);

        for(long element : offsets){
            Log.d("Ordered Offset", String.valueOf(element));
        }

        return offsets[1];

    }

    private long getOffset(){
        SntpClient client = new SntpClient();
        long offset = Long.MAX_VALUE;
        if (client.requestTime("us.pool.ntp.org", 6000)) {
            long systemTime = System.currentTimeMillis();
            long ntpTime = client.getNtpTime() + SystemClock.elapsedRealtime() -
                    client.getNtpTimeReference();
            offset = (ntpTime - systemTime);
        }
        Log.d("OFFSET",offset+"");
        //mOffset =  offset;
        return offset;

    }

    private long initialOffset = Long.MAX_VALUE;
    private String initialLine = "";

    private class InitialOffsetTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void...params) {
            initialOffset = start();
            return initialOffset;

        }

        @Override
        protected void onPostExecute(Long result) {
            offsetTextView.setText("Calculating Initial Offset\nInitial Offset: " + initialOffset + "ms\n");
            initialLine = "Calculating Initial Offset\nInitial Offset: " + initialOffset + "ms\n";




        }
    }
    private long finalOffset = Long.MAX_VALUE;
    private class FinalOffsetTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void...params) {

            finalOffset = start();
            return finalOffset;

        }

            @Override
            protected void onPostExecute(Long result) {
                fullScreenActivity(null);

            }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {



        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            offsetTextView = (TextView) rootView.findViewById(R.id.offsetTextView);
            return rootView;
        }
    }


}
