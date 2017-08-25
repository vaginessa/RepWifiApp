package fil.libre.repwifiapp;

import fil.libre.repwifiapp.ActivityLauncher.RequestCode;
import fil.libre.repwifiapp.activities.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RepWifiIntentReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context
                        .getApplicationContext());
        if (!prefs.getBoolean("enable_autostart", false)) {
            Log.d("RepWifi", "autostart is false");
            return;
        }

        Log.d("RepWifi", "Autostart is true");
        String a = intent.getAction();

        if (a.equals(Intent.ACTION_BOOT_COMPLETED) || a.equals(Intent.ACTION_REBOOT)) {
            launchRepWifiMainActivity(context, RequestCode.NONE);
        }

    }

    private void launchRepWifiMainActivity(Context context, int reqCode) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        if (reqCode >= 0) {
            intent.putExtra(ActivityLauncher.EXTRA_REQCODE, reqCode);
        }
        context.startActivity(intent);

    }
}
