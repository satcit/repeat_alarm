package ru.satcit.repeat_alarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RepeatService extends Service {
  public static final String ACTION_PROPERTY = "RepeatService.action";
  public static final String AUDIO_RESID_PROPERTY = "RepeatService.audio";
  public static final String REPEAT_RATE_PROPERTY = "RepeatService.repeat.rate";
  public static final String REPEAT_RATE_UNIT_PROPERTY = "RepeatService.repeat.rate.unit";
  public static final String REPEAT_DURATION_PROPERTY = "RepeatService.repeat.duration";
  public static final String REPEAT_DURATION_UNIT_PROPERTY = "RepeatService.duration.unit";
  public static final String REST_DURATION_PROPERTY = "RepeatService.rest.duration";
  public static final String REST_DURATION_UNIT_PROPERTY = "RepeatService.rest.duration.unit";

  private Runnable action;
  private long repeatDuration;
  private TimeUnit repeatDurationUnit;
  private long repeatRate;
  private TimeUnit repeatRateUnit;
  private long restDuration;
  private TimeUnit restDurationUnit;
//  private Vibrator vibrator;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> repeatFuture;

  @Override
  public void onCreate() {
    //TODO place vibrator to action
//    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if(!parseParameters(intent)){
      return START_NOT_STICKY;
    }
    long totalDuration = repeatDurationUnit.toMillis(repeatDuration) + restDurationUnit.toMillis(restDuration);
    //repeatedly schedule action repeats
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        repeatFuture = scheduler.scheduleAtFixedRate(action, 0, repeatRate, repeatRateUnit);
      }
    }, 0, totalDuration, TimeUnit.MILLISECONDS);
    //in rest time cancel action repetition
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        if(repeatFuture!= null && !repeatFuture.isCancelled()) {
          repeatFuture.cancel(true);
        }
      }
    }, repeatDurationUnit.toMillis(repeatDuration), totalDuration, TimeUnit.MILLISECONDS );
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    scheduler.shutdownNow();
  }

  @Override
  public IBinder onBind(Intent intent) {
    // no binding to service
    return null;
  }

  private boolean parseParameters(Intent intent) {
    Bundle b = intent.getExtras();
    if(!b.keySet().containsAll(Arrays.asList(
            AUDIO_RESID_PROPERTY,
            REPEAT_DURATION_PROPERTY,
            REPEAT_DURATION_UNIT_PROPERTY,
            REPEAT_RATE_PROPERTY,
            REPEAT_RATE_UNIT_PROPERTY,
            REST_DURATION_PROPERTY,
            REST_DURATION_UNIT_PROPERTY))){
      return false;
    }
    final int resId = b.getInt(AUDIO_RESID_PROPERTY);
    //TODO create subclass?
    //TODO include vibration
    action = new Runnable() {
      @Override
      public void run() {
        final MediaPlayer player = MediaPlayer.create(RepeatService.this, resId);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mp) {
            player.release();
          }
        });
        player.start();
      }
    };
    repeatDuration = b.getLong(REPEAT_DURATION_PROPERTY);
    repeatDurationUnit = TimeUnit.valueOf(b.get(REPEAT_DURATION_UNIT_PROPERTY).toString());
    repeatRate = b.getLong(REPEAT_RATE_PROPERTY);
    repeatRateUnit = TimeUnit.valueOf(b.get(REPEAT_RATE_UNIT_PROPERTY).toString());
    restDuration = b.getLong(REST_DURATION_PROPERTY);
    restDurationUnit = TimeUnit.valueOf(b.get(REST_DURATION_UNIT_PROPERTY).toString());

    return true;
  }
}
