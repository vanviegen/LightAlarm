package net.vanviegen.lightalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class PowerReceiver extends BroadcastReceiver {

  boolean isPluggedWireless(Context context) {
    Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    Log.i("laxx", "isPluggedWireless: "+plugged+" / "+BatteryManager.BATTERY_PLUGGED_WIRELESS);
    return plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i("laxx", "PowerReceiver onReceive: " + intent.getAction());
    if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED) && isPluggedWireless(context)) {
      Intent activityIntent = new Intent();
      activityIntent.setClassName("net.vanviegen.lightalarm", "net.vanviegen.lightalarm.MainActivity");
      activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(activityIntent);
    }
    else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
      if (!isPluggedWireless(context)) {
        //context.sendBroadcast(new Intent(MainActivity.FINISH_RECEIVER_INTENT));
      }
    }
  }
}
