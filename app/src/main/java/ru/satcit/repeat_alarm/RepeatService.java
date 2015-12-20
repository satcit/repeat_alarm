package ru.satcit.repeat_alarm;

import java.util.Date;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;

/**
 * Created by Aleksei on 20.12.2015.
 */

public class RepeatService extends Service implements Runnable {
    private Thread serviceThread;
    private Vibrator vibrator;
    private Date nextAlarmDate;
    private long alarmDuration;
    private long alarmRepeatDuration;

    public RepeatService() {
    }

    @Override
    public void onCreate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        alarmDuration = preferences.getInt(getString(R.string.alarmDuration), getResources().getInteger(R.integer.defaultAlarmDuration));
        alarmRepeatDuration = preferences.getInt(getString(R.string.alarmRepeatDuration), getResources().getInteger(R.integer.defaultAlarmRepeatDuration));
        refreshAlarmDate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(serviceThread != null) {
            serviceThread.interrupt();
        }
        serviceThread = new Thread(this);
        serviceThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(serviceThread != null) {
            serviceThread.interrupt();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding to service
        return null;
    }

    private void refreshAlarmDate() {
        nextAlarmDate = new Date();
        nextAlarmDate.setTime(nextAlarmDate.getTime() + alarmRepeatDuration * 1000L);
    }

    @Override
    public void run() {
        try {
            while(true) {
                long diff = nextAlarmDate.getTime() - System.currentTimeMillis();
                if (diff > 0) {

                    Thread.sleep(diff);

                }
                vibrator.vibrate(alarmDuration);
                refreshAlarmDate();
            }
        } catch (InterruptedException e) {
            // TODO handle exception
        }
    }
}
