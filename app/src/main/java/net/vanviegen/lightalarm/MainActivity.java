  package net.vanviegen.lightalarm;

  import androidx.appcompat.app.AppCompatActivity;

  import android.animation.ArgbEvaluator;
  import android.annotation.SuppressLint;
  import android.app.AlarmManager;
  import android.content.BroadcastReceiver;
  import android.content.Context;
  import android.content.Intent;
  import android.content.IntentFilter;
  import android.net.Uri;
  import android.os.Build;
  import android.os.Bundle;
  import android.os.Handler;
  import android.os.PowerManager;
  import android.provider.Settings;
  import android.util.Log;
  import android.view.WindowManager;
  import android.widget.Button;
  import android.widget.TextView;

  public class MainActivity extends AppCompatActivity {

    AlarmManager.AlarmClockInfo nextAlarm;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        // Retrigger 50ms after the next minute has started
        java.util.Date date = new java.util.Date();
        long time = date.getTime();
        long delay;
        if (nextAlarm != null && nextAlarm.getTriggerTime() >= time && nextAlarm.getTriggerTime() <= time + fadeMinutes*60*1000) {
          // We're fading in. Update every 3 seconds.
          delay = (time / 3000) * 3000 + 3000 - time;
        } else {
          // Update every minute.
          delay = (time / 60000) * 60000 + 60000 - time;
        }
        handler.postDelayed(this, delay+50);

        update(date);
      }
    };

    static public final String FINISH_RECEIVER_INTENT = "net.vanviegen.lightalarm.finish";

    private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        finish();
      }
    };

    final int fadeMinutes = 30;

    @Override
    public void onAttachedToWindow() {
      super.onAttachedToWindow();
      getWindow().addFlags(
          WindowManager.LayoutParams.FLAG_FULLSCREEN
          | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      );
    }

    public
    void openOverlaySettings(android.view.View view) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,  Uri.parse("package:" + getPackageName()));
      startActivity(intent);
    }

    @Override
    protected void onResume() {
      super.onResume();

      Button overlayButton = findViewById(R.id.overlay_button);
      overlayButton.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) ? overlayButton.VISIBLE : overlayButton.GONE);

      PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

      int level = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
      wakeLock = pm.newWakeLock(level, "backgroundmode:wakelock");
      wakeLock.setReferenceCounted(false);
      wakeLock.acquire(2000);

      AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
      nextAlarm = alarmManager.getNextAlarmClock();

      handler.post(runnable);
    }

    protected void update(java.util.Date date) {
      TextView timeView = findViewById(R.id.time);
      CharSequence time = android.text.format.DateFormat.format("HH:mm", date);
      timeView.setText(time);

      TextView timeLeftView = findViewById(R.id.timeLeft);
      long msLeft = -999999999;
      long minutesLeft = -999999999;
      float brightness = -1; // automatic
      int backgroundColor = 0xff000000;
      int textColor = 0xffa37d0d;

      if (nextAlarm != null) {
        minutesLeft = nextAlarm.getTriggerTime()/60000 - date.getTime()/60000;
        msLeft = nextAlarm.getTriggerTime() - date.getTime();
      }

      if (minutesLeft > -60) {
        if (minutesLeft <= fadeMinutes) {
          brightness = Math.min(1, Math.max(0, 1 - (float) msLeft / (float) fadeMinutes / 1000f / 60f));
          if (brightness < 0.5) {
            backgroundColor = 0xff000000 | (int) new ArgbEvaluator().evaluate(brightness*2f, 0x2e1300, 0xff6a00);
          } else {
            backgroundColor = 0xff000000 | (int) new ArgbEvaluator().evaluate(brightness*2f-1f, 0xff6a00, 0xfffbd2);
          }
          if (brightness > 0.25) {
            textColor = 0xff000000;
          }
        }

        String sign = minutesLeft < 0 ? "- " : "+ ";
        minutesLeft = Math.abs(minutesLeft);
        long hoursLeft = minutesLeft / 60;
        minutesLeft -= hoursLeft * 60;
        timeLeftView.setText(sign + hoursLeft + ":" + (minutesLeft>9 ? "" : "0") + minutesLeft);
      } else {
        timeLeftView.setText("");
      }

      findViewById(R.id.background).setBackgroundColor(backgroundColor);
      timeView.setTextColor(textColor);
      timeLeftView.setTextColor(textColor);

      WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
      layoutParams.screenBrightness = brightness;
      getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onPause() {
      super.onPause();

      wakeLock.release();
      wakeLock = null;

      handler.removeCallbacks(runnable);
    }

    PowerManager.WakeLock wakeLock;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Log.i("laxx", "MainActivity onCreate");

      getSupportActionBar().hide();
      setShowWhenLocked(true);

      setContentView(R.layout.activity_main);

      registerReceiver(finishReceiver, new IntentFilter(FINISH_RECEIVER_INTENT));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(finishReceiver);
    }
  }