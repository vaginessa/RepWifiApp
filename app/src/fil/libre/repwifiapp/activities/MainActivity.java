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

package fil.libre.repwifiapp.activities;

import java.io.IOException;
import java.net.SocketException;
import fil.libre.repwifiapp.ActivityLauncher;
import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.ActivityLauncher.RequestCode;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.NetworkManager;
import fil.libre.repwifiapp.helpers.RootCommand;
import fil.libre.repwifiapp.helpers.Utils;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends MenuEnabledActivity {

    private ActivityLauncher launcher = new ActivityLauncher(this);
    private BroadcastReceiver detachReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Commons.init(this)) {
            Utils.logDebug("Failed to initialize Commons. Aborting.");
            finish();
            return;
        }

        setImage();
        setUsbDeviceMonitor();
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.logDebug("Main onStart()");

        Commons.updateNotification(this);

        if (handleIntent()) {
            // app called for a specific intent.
            // avoid any other task.
            Log.d("RepWifi", "handleIntent returned true");
            return;
        }

        checkConditions();

        ConnectionStatus status = Commons.connectionEngine.getConnectionStatus();
        if (status != null && status.isConnected()) {
            Utils.logDebug("Main about to launch status activity...");
            launcher.launchStatusActivity(status);
        }

        Utils.logDebug("Main onStart() returning.");

    }

    private boolean handleIntent() {

        Intent i = getIntent();
        if (i != null && i.hasExtra(ActivityLauncher.EXTRA_REQCODE)) {

            switch (i.getIntExtra(ActivityLauncher.EXTRA_REQCODE, -1)) {

            case RequestCode.NONE:
                moveTaskToBack(true);

                break;

            default:
                break;
            }

            return true;

        } else {
            // no intent to handle.
            return false;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Utils.logDebug("Main onActivityResult(): ", 1);

        if (intent == null) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        AccessPointInfo i = null;
        if (intent.hasExtra(ActivityLauncher.EXTRA_APINFO)) {
            Bundle xtras = intent.getExtras();
            i = (AccessPointInfo) xtras.getSerializable(ActivityLauncher.EXTRA_APINFO);
        }

        switch (requestCode) {

        case RequestCode.PASS_INPUT:
            handleResultSetPass(i);
            break;

        case RequestCode.SELECT_CONN:
            boolean rescan = intent.getExtras().getBoolean(ActivityLauncher.EXTRA_RESCAN);
            handleResultSelect(i, rescan);
            break;

        case RequestCode.CONNECT:
            boolean conres = intent.getExtras().getBoolean(ActivityLauncher.EXTRA_BOOLEAN);
            handleFinishedConnecting(conres, i);
            break;

        case RequestCode.STATUS_GET:
            ConnectionStatus status = (ConnectionStatus) intent.getExtras().getSerializable(
                            ActivityLauncher.EXTRA_CONSTATUS);
            handleResultGetStatus(status);
            break;

        case RequestCode.NETWORKS_GET:
            AccessPointInfo[] nets = (AccessPointInfo[]) intent.getExtras().getSerializable(
                            ActivityLauncher.EXTRA_APINFO_ARR);
            launcher.launchSelectActivity(nets, true, false);

        case RequestCode.STATUS_SHOW:
            // do nothing
            break;

        case RequestCode.SELECT_DETAILS:
            launcher.launchDetailsActivity(i);
            break;

        case RequestCode.DETAILS_SHOW:
            boolean del = intent.getExtras().getBoolean(ActivityLauncher.EXTRA_DELETE);
            if (del) {
                deleteNetwork(i);
            }
            break;

        case RequestCode.CONNECT_HIDDEN:

            if (i != null) {
                handleResultSelect(i, false);
            }
            break;

        default:

            break;

        }

    }

    private void setImage() {

        ImageView img = (ImageView) findViewById(R.id.img_logo);

        try {
            Drawable d = Drawable.createFromStream(getAssets().open("repwifi-logo-0.png"), null);
            img.setImageDrawable(d);
        } catch (IOException e) {
            Utils.logError("Error while loading logo image", e);
        }

    }

    private void setUsbDeviceMonitor() {
        detachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    handleUsbEvent(true);
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    handleUsbEvent(false);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(detachReceiver, filter);

        IntentFilter filt2 = new IntentFilter();
        filt2.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(detachReceiver, filt2);
    }

    private boolean checkConditions() {
        return (checkRootEnabled() && checkInterface(true));
    }

    private boolean checkInterface(boolean alert) {

        boolean res = false;
        String msg = "";

        try {
            res = Commons.connectionEngine.isInterfaceAvailable(Commons.INTERFACE_NAME);
        } catch (SocketException e) {
            Utils.logError("SocketException during isInterfaceAvailable()", e);
            msg = "Error while retrieving interface list!";
            res = false;
        }

        if (res == false && alert) {
            msg = getResources().getString(R.string.msg_interface_not_found);
            Commons.showMessage(msg, this);
        }

        return res;

    }

    private boolean checkRootEnabled() {

        boolean result = false;
        String msg = "Unknown Root error";
        RootCommand su = new RootCommand(null);

        int excode = -1;

        try {
            excode = su.execute();
        } catch (Exception e) {
            Utils.logError("Error while trying to get first Super User access.", e);
            excode = -1;
            result = false;
        }

        switch (excode) {
        case 0:
            result = true;
            break;

        case Commons.EXCOD_ROOT_DENIED:
            result = false;
            msg = getResources().getString(R.string.msg_root_denied);
            break;

        case Commons.EXCOD_ROOT_DISABLED:
            result = false;
            msg = getResources().getString(R.string.msg_root_disabled);
            break;

        default:
            result = false;
            msg = "Unknown Root error.\nExit code " + excode;
            break;
        }

        if (!result) {
            Commons.showMessage(msg, this);
        }

        return result;

    }

    private void handleResultSelect(AccessPointInfo i, boolean rescan) {

        if (rescan) {

            doScan();

        } else if (i != null) {

            if (i.needsPassword()) {

                // try to fetch network's password from storage
                AccessPointInfo fromStorage = Commons.storage.getSavedNetwork(i);
                if (fromStorage == null) {

                    launcher.launchPasswordActivity(i);
                    return;

                } else {
                    // use fetched network
                    i = fromStorage;
                }

            }

            launcher.launchLongTaskActivityConnect(i);
        }

    }

    private void handleResultSetPass(AccessPointInfo i) {
        launcher.launchLongTaskActivityConnect(i);
    }

    private void handleResultGetStatus(ConnectionStatus status) {
        if (status != null && status.isConnected()) {
            launcher.launchStatusActivity(status);
        }
    }

    private void handleFinishedConnecting(boolean connectionResult, AccessPointInfo info) {

        if (connectionResult && info.needsPassword()) {

            ConnectionStatus status = Commons.connectionEngine.getConnectionStatus();
            if (status != null) {
                // update APinfo with the right BSSID
                info.setBssid(status.BSSID);
            }

            // Save network
            if (Commons.storage.save(info)) {
                Toast toast2 = Toast.makeText(getApplicationContext(), "Network Saved!",
                                Toast.LENGTH_LONG);
                toast2.show();

            } else {
                Toast toast2 = Toast.makeText(getApplicationContext(), "FAILED to save network!",
                                Toast.LENGTH_LONG);
                toast2.show();
            }

            // show status
            launcher.launchStatusActivity(status);

        } else {
            // alert that connection failed
            Toast toast = Toast.makeText(getApplicationContext(), "FAILED to connect!",
                            Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void deleteNetwork(AccessPointInfo info) {

        NetworkManager manager = new NetworkManager(Commons.getNetworkStorageFile());
        String msg = "";
        if (manager.remove(info)) {
            msg = "Network info deleted!";
        } else {
            msg = "FAILED to delete network info!";
        }

        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();

    }

    private void handleUsbEvent(boolean detached) {

        if (detached && !checkInterface(false)) {
            // device disconnected, update the status bar:
            Commons.updateNotification(this);

        } else if (Commons.isAutoConnectEnabled()) {

            try {

                // waits for a maximum of WAIT_ON_USB_ATTACHED milliseconds
                // to let the interface being registered.
                int msWaited = 0;
                while (msWaited < Commons.WAIT_ON_USB_ATTACHED) {

                    Thread.sleep(100);
                    msWaited += 100;

                    if (checkInterface(false)) {
                        autoConnect();
                        return;
                    }
                }

            } catch (InterruptedException e) {
                // ignores and exits;
                return;
            }

        }

    }

    private void autoConnect() {

        try {

            AccessPointInfo[] nets = Commons.connectionEngine.getAvailableNetworks();
            if (nets == null || nets.length == 0) {
                return;
            }

            for (AccessPointInfo i : nets) {

                if (Commons.storage.isKnown(i)) {
                    launcher.launchLongTaskActivityConnect(i);
                    return;
                }

            }

            // if no network is known, shows available networks to the user.
            launcher.launchSelectActivity(nets, true, false);

        } catch (Exception e) {
            Utils.logError("Error while autoconnecting", e);
            Commons.showMessage("An error occured while trying to auto-connect", this);
        }

    }

    private void doScan() {
        if (checkConditions()) {
            launcher.launchLongTaskActivityScan();
        }
    }

    public void btnScanClick(View v) {
        doScan();
    }

    public void btnHiddenClick(View v) {

        if (checkConditions()) {
            launcher.launchInputSsidActivity();
        }

    }

    public void btnManageClick(View v) {
        launcher.launchSelectActivity(null, false, true);
    }

}
