package ru.satcit.repeat_alarm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {
  public static final String PREFERENCES_NAME = "repeatalarm.preferences";

  private EditText repeatRateText;
  private Spinner repeatRateUnitSpinner;
  private EditText repeatDurationText;
  private Spinner repeatDurationUnitSpinner;
  private EditText restDurationText;
  private Spinner restDurationUnitSpinner;
  private Button startAlarmButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    bindControls();
    loadSettings();
    initListeners();
  }

  private void bindControls() {
    repeatRateText = (EditText)findViewById(R.id.repeatRateText);
    repeatRateUnitSpinner = (Spinner)findViewById(R.id.repeatRateUnitSpinner);
    repeatDurationText = (EditText)findViewById(R.id.repeatDurationText);
    repeatDurationUnitSpinner = (Spinner)findViewById(R.id.repeatDurationUnitSpinner);
    restDurationText = (EditText)findViewById(R.id.restDurationText);
    restDurationUnitSpinner = (Spinner)findViewById(R.id.restDurationUnitSpinner);
    startAlarmButton = (Button)findViewById(R.id.startAlarmButton);

    ArrayAdapter<TimeUnit> timeUnitValues = new ArrayAdapter<TimeUnit>(this, android.R.layout.simple_spinner_item, TimeUnit.values());//ArrayAdapter.createFromResource(this, R.array.timeUnits, android.R.layout.simple_spinner_item);
    timeUnitValues.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    repeatRateUnitSpinner.setAdapter(timeUnitValues);
    repeatDurationUnitSpinner.setAdapter(timeUnitValues);
    restDurationUnitSpinner.setAdapter(timeUnitValues);
  }

  private void initListeners() {
    startAlarmButton.setText(isServiceRunning(RepeatService.class) ? R.string.stop : R.string.start);
    startAlarmButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isServiceRunning(RepeatService.class)) {
          saveSettings();
          startRepeatService();
          startAlarmButton.setText(R.string.stop);
        } else {
          stopService(new Intent(MainActivity.this, RepeatService.class));
          startAlarmButton.setText(R.string.start);
        }
      }
    });
  }

  private boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  private void startRepeatService() {
    Intent intent = new Intent(this, RepeatService.class);
    //TODO validate parameters
    intent.putExtra(RepeatService.AUDIO_RESID_PROPERTY, R.raw.sound);
    intent.putExtra(RepeatService.REPEAT_DURATION_PROPERTY, Long.parseLong(repeatDurationText.getText().toString()));
    intent.putExtra(RepeatService.REPEAT_DURATION_UNIT_PROPERTY, ((TimeUnit)repeatDurationUnitSpinner.getSelectedItem()).name());
    intent.putExtra(RepeatService.REPEAT_RATE_PROPERTY, Long.parseLong(repeatRateText.getText().toString()));
    intent.putExtra(RepeatService.REPEAT_RATE_UNIT_PROPERTY, ((TimeUnit)repeatRateUnitSpinner.getSelectedItem()).name());
    intent.putExtra(RepeatService.REST_DURATION_PROPERTY, Long.parseLong(restDurationText.getText().toString()));
    intent.putExtra(RepeatService.REST_DURATION_UNIT_PROPERTY, ((TimeUnit)restDurationUnitSpinner.getSelectedItem()).name());
    startService(intent);
  }

  private void saveSettings() {
    SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit();
    editor.putLong(RepeatService.REPEAT_DURATION_PROPERTY, Long.parseLong(repeatDurationText.getText().toString()));
    editor.putString(RepeatService.REPEAT_DURATION_UNIT_PROPERTY, ((TimeUnit) repeatDurationUnitSpinner.getSelectedItem()).name());
    editor.putLong(RepeatService.REPEAT_RATE_PROPERTY, Long.parseLong(repeatRateText.getText().toString()));
    editor.putString(RepeatService.REPEAT_RATE_UNIT_PROPERTY, ((TimeUnit) repeatRateUnitSpinner.getSelectedItem()).name());
    editor.putLong(RepeatService.REST_DURATION_PROPERTY, Long.parseLong(restDurationText.getText().toString()));
    editor.putString(RepeatService.REST_DURATION_UNIT_PROPERTY, ((TimeUnit)restDurationUnitSpinner.getSelectedItem()).name());
    editor.apply();
  }

  private void loadSettings() {
    SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
    repeatDurationText.setText(String.format("%d", preferences.getLong(RepeatService.REPEAT_DURATION_PROPERTY, 0L)));
    repeatDurationUnitSpinner.setSelection(TimeUnit.valueOf(preferences.getString(RepeatService.REPEAT_DURATION_UNIT_PROPERTY, TimeUnit.SECONDS.name())).ordinal());
    repeatRateText.setText(String.format("%d", preferences.getLong(RepeatService.REPEAT_RATE_PROPERTY, 0L)));
    repeatRateUnitSpinner.setSelection(TimeUnit.valueOf(preferences.getString(RepeatService.REPEAT_RATE_UNIT_PROPERTY, TimeUnit.SECONDS.name())).ordinal());
    restDurationText.setText(String.format("%d", preferences.getLong(RepeatService.REST_DURATION_PROPERTY, 0L)));
    restDurationUnitSpinner.setSelection(TimeUnit.valueOf(preferences.getString(RepeatService.REST_DURATION_UNIT_PROPERTY, TimeUnit.SECONDS.name())).ordinal());
  }
}
