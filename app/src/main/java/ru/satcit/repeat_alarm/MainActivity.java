package ru.satcit.repeat_alarm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends ActionBarActivity {
    public static final String PREFERENCES_NAME = "alarmrepeater.preferences";

    private EditText repeatDurationText;
    private EditText durationText;
    private Button startAlarmButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindControls();

        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        repeatDurationText.setText(Integer.toString(preferences.getInt(getString(R.string.alarmRepeatDuration), getResources().getInteger(R.integer.defaultAlarmRepeatDuration))));
        durationText.setText(Integer.toString(preferences.getInt(getString(R.string.alarmDuration), getResources().getInteger(R.integer.defaultAlarmDuration))));

        initListeners();
    }

    private void bindControls() {
        repeatDurationText = (EditText) findViewById(R.id.alarmRepeatDurationText);
        durationText = (EditText) findViewById(R.id.alarmDurationText);
        startAlarmButton = (Button) findViewById(R.id.startAlarmButton);
    }

    private void initListeners() {
        startAlarmButton.setText(isServiceRunning(RepeatService.class) ? R.string.stop : R.string.start);
        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isServiceRunning(RepeatService.class)) {
                    RadioButton minutesRadio = (RadioButton) MainActivity.this.findViewById(R.id.alarmRepeatDurationMinutes);
                    SharedPreferences.Editor editor = preferences.edit();
                    int repeatDuration = Integer.parseInt(repeatDurationText.getText().toString());
                    editor.putInt(getString(R.string.alarmRepeatDuration), minutesRadio.isChecked() ? repeatDuration * 60 : repeatDuration);
                    editor.putInt(getString(R.string.alarmDuration), Integer.parseInt(durationText.getText().toString()));
                    editor.apply();
                    startService(new Intent(MainActivity.this, RepeatService.class));
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
}
