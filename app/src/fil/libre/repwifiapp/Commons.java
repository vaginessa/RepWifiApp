//
// Copyright 2017 Filippo "Fil" Bergamo <fil.bergamo@riseup.net>
// 
// This file is part of RepWifiApp.
//
// RepWifiApp is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// RepWifiApp is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with RepWifiApp.  If not, see <http://www.gnu.org/licenses/>.
// 
// ********************************************************************

package fil.libre.repwifiapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import fil.libre.repwifiapp.activities.MainActivity;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.Engine6p0;
import fil.libre.repwifiapp.helpers.IEngine;
import fil.libre.repwifiapp.helpers.NetworkManager;
import fil.libre.repwifiapp.helpers.Utils;

public abstract class Commons {

    private static Context currentContext;

    public Context getContext() {
        return currentContext;
    }

    // ------------- Enviromnet Constants -----------------
    public static final int EXCOD_ROOT_DISABLED = 255;
    public static final int EXCOD_ROOT_DENIED = 1;
    public static final int WAIT_ON_USB_ATTACHED = 1500;
    public static final String BSSID_NOT_AVAILABLE = "[BSSID-NOT-AVAILABLE]";
    public static final String v4p2 = "4.2";
    public static final String v6p0 = "6.0";
    public static final String SCAN_FILE_HDR = "bssid / frequency / signal level / flags / ssid";
    public static final String INTERFACE_NAME = "wlan0";
    public static final String WORKDIR = "/data/misc/wifi";
    public static final String PID_FILE = WORKDIR + "/pidfile";
    public static final String SOCKET_DIR = WORKDIR + "/sockets/";
    public static final String SOFTAP_FILE = WORKDIR + "/softap.conf";
    public static final String P2P_CONF = WORKDIR + "/p2p_supplicant.conf";
    public static final String WPA_CONF = WORKDIR + "/wpa_supplicant.conf";
    public static final String ENTROPY_FILE = WORKDIR + "/entropy.bin";
    public static final String OVERLAY_FILE = "/system/etc/wifi/wpa_supplicant_overlay.conf";
    // ---------------------------------------------

    // ------------- Shared Engines -----------------------
    public static IEngine connectionEngine = null;
    public static NetworkManager storage = null;
    // ----------------------------------------------------

    // ------------- Shared Resources ---------------------
    public static int colorThemeDark;
    public static int colorThemeLight;
    public static int colorBlack;
    public static String dns1Default = "";
    public static String dns2Default = "";

    private static final int NOTIFICATION_ID = 1;

    public static void updateNotification(Context context) {

        ConnectionStatus status = connectionEngine.getConnectionStatus();

        Notification.Builder builder = new Notification.Builder(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        int iconId = R.drawable.ic_stat_discon;
        String msg = "RepWifi";
        if (status != null) {
            if (status.isConnected()) {
                iconId = R.drawable.ic_stat_repwifi;
                msg += " - " + status.SSID;
            } else {
                msg += " - " + status.status;
            }

        }

        builder.setSmallIcon(iconId);

        builder.setContentTitle(msg);
        builder.setContentText("Touch to open.");

        Notification n = builder.build();
        n.flags |= Notification.FLAG_NO_CLEAR;

        NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, n);

    }

    public static void showMessage(String msg) {
        showMessage(msg, currentContext);
    }

    public static void showMessage(String msg, Context context) {

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context,
                        R.style.Theme_RepWifiDialogTheme);
        dlgAlert.setMessage(msg);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });

        dlgAlert.setCancelable(false);
        AlertDialog diag = dlgAlert.create();

        diag.show();

    }

    public static void resetSettingsDefault(Context context, boolean silent) {

        if (silent) {
            Editor e = getSettings().edit();
            e.clear();
            e.commit();
        } else {

            String msg = context.getString(R.string.confirm_reset_settings);
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context,
                            R.style.Theme_RepWifiDialogTheme);
            dlgAlert.setMessage(msg);
            dlgAlert.setPositiveButton(context.getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    resetSettingsDefault(null, true);
                                    return;
                                }
                            });
            dlgAlert.setNegativeButton(context.getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    return;
                                }
                            });

            dlgAlert.setCancelable(false);
            AlertDialog diag = dlgAlert.create();

            diag.show();

            return;
        }

    }

    public static void killBackEnd(Context context, boolean silent) {

        if (silent) {

            if (connectionEngine != null) {
                connectionEngine.killBackEndProcesses();
            }

        } else {

            String msg = context.getString(R.string.confirm_kill_backend);
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context,
                            R.style.Theme_RepWifiDialogTheme);
            dlgAlert.setMessage(msg);
            dlgAlert.setPositiveButton(context.getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    killBackEnd(null, true);
                                    return;
                                }
                            });
            dlgAlert.setNegativeButton(context.getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    return;
                                }
                            });

            dlgAlert.setCancelable(false);
            AlertDialog diag = dlgAlert.create();

            diag.show();
            return;

        }

    }

    public static int getLogPriority() {

        SharedPreferences sets = getSettings();
        return Integer.parseInt(sets.getString("debug_priority", "3"));

    }

    public static boolean isProgbarEnabled() {
        return getSettings().getBoolean("enable_progbar", true);
    }

    public static boolean isAutoConnectEnabled() {
        return getSettings().getBoolean("enable_autoconnect", false);
    }

    public static String[] getDnss() {

        String dns1 = getSettings().getString("dns1", dns1Default);
        String dns2 = getSettings().getString("dns2", dns2Default);

        if (dns1 == null || dns1.isEmpty()) {
            return null;
        }

        return new String[] { dns1, dns2 };

    }

    public static SharedPreferences getSettings() {

        return PreferenceManager.getDefaultSharedPreferences(currentContext);

    }

    // ----------------------------------------------------

    // ----------------- Application Files --------------------
    private static String APP_DATA_FOLDER;

    public static String getNetworkStorageFile() {
        if (APP_DATA_FOLDER == null) {
            return null;
        } else {
            return APP_DATA_FOLDER + "/repwifi_storage.conf";
        }
    }

    public static String getTempFile() {
        return APP_DATA_FOLDER + "/file.tmp";
    }

    public static String getScriptScan() {
        return APP_DATA_FOLDER + "/scan_app.sh";
    }

    public static String getScriptScanRes() {
        return APP_DATA_FOLDER + "/get_scan_results_app.sh";
    }

    public static String getScriptDhcpcd() {
        return APP_DATA_FOLDER + "/run_dhcpcd.sh";
    }

    public static String getOldSelectScript() {
        return WORKDIR + "/select_network.sh";
    }

    public static String getScanFile() {
        return APP_DATA_FOLDER + "/scanres.txt";
    }

    public static String getStatusFile() {
        return APP_DATA_FOLDER + "/tmpStatus";
    }

    public static String getGwFile() {
        return APP_DATA_FOLDER + "/gw.txt";
    }

    public static String getTempOutFile() {
        return APP_DATA_FOLDER + "/tmpout.txt";
    }

    // --------------------------------------------------------

    // ----------- Initialization methods ---------------------------
    public static boolean init(Context context) {

        currentContext = context;

        try {

            colorThemeDark = currentContext.getResources().getColor(R.color.ThemeDark);
            colorThemeLight = currentContext.getResources().getColor(R.color.ThemeLight);
            colorBlack = currentContext.getResources().getColor(R.color.black);
            APP_DATA_FOLDER = currentContext.getExternalFilesDir(null).getAbsolutePath();
            dns1Default = currentContext.getResources().getString(R.string.dns1_default);
            dns2Default = currentContext.getResources().getString(R.string.dns2_default);

            initEngine();
            initNetworkStorage();

            return true;

        } catch (Exception e) {
            Utils.logError("Error initializing common resources.", e);
            return false;
        }
    }

    private static void initEngine() throws Exception {

        connectionEngine = new Engine6p0();

        String vers = android.os.Build.VERSION.RELEASE;

        if (!vers.startsWith(Commons.v6p0)) {
            showMessage("UNSUPPORTED OS VERSION\nThe current version of Replicant is not supported by RepWifi.\nPlease upgrade to the latest version as soon as possible!");
        }

    }

    private static void initNetworkStorage() throws Exception {

        Commons.storage = new NetworkManager(getNetworkStorageFile());

    }
    // --------------------------------------------------------------

}
