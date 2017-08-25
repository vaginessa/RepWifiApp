package fil.libre.repwifiapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import fil.libre.repwifiapp.activities.InputPasswordActivity;
import fil.libre.repwifiapp.activities.InputSsidActivity;
import fil.libre.repwifiapp.activities.LongTaskActivity;
import fil.libre.repwifiapp.activities.NetworkDetailsActivity;
import fil.libre.repwifiapp.activities.SelectNetworkActivity;
import fil.libre.repwifiapp.activities.ShowStatusActivity;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.NetworkManager;

public class ActivityLauncher {

    // ------------- Activity Interaction -----------------
    public static final String EXTRA_APINFO = "ExAPInfo";
    public static final String EXTRA_APINFO_ARR = "ExAPInfoArr";
    public static final String EXTRA_CONSTATUS = "ExConSts";
    public static final String EXTRA_BOOLEAN = "ExBool";
    public static final String EXTRA_REQCODE = "ExReqCode";
    public static final String EXTRA_RESCAN = "ExRescan";
    public static final String EXTRA_DELETE = "ExDelete";

    public class RequestCode {

        public static final int NONE = 0;
        public static final int SELECT_CONN = 1;
        public static final int PASS_INPUT = 2;
        public static final int STATUS_SHOW = 3;
        public static final int STATUS_GET = 4;
        public static final int CONNECT = 5;
        public static final int NETWORKS_GET = 6;
        public static final int SELECT_DETAILS = 7;
        public static final int DETAILS_SHOW = 8;
        public static final int NETWORK_DELETE = 9;
        public static final int CONNECT_HIDDEN = 10;
        public static final int USB_ATTACHED = 11;
        public static final int USB_DETACHED = 12;

    }

    // ----------------------------------------------------

    private Activity currentContext;

    public ActivityLauncher(Activity caller) {
        this.currentContext = caller;
    }

    public void launchLongTaskActivityDebug() {

        Intent intent = new Intent(currentContext, LongTaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ActivityLauncher.EXTRA_REQCODE, RequestCode.NONE);
        currentContext.startActivityForResult(intent, RequestCode.NONE);
    }

    public void launchLongTaskActivityConnect(AccessPointInfo info) {

        Intent intent = new Intent(currentContext, LongTaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ActivityLauncher.EXTRA_REQCODE, RequestCode.CONNECT);
        intent.putExtra(ActivityLauncher.EXTRA_APINFO, info);
        currentContext.startActivityForResult(intent, RequestCode.CONNECT);
    }

    public void launchLongTaskActivityScan() {

        Intent intent = new Intent(currentContext, LongTaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ActivityLauncher.EXTRA_REQCODE, RequestCode.NETWORKS_GET);
        currentContext.startActivityForResult(intent, RequestCode.NETWORKS_GET);
    }

    public void launchPasswordActivity(AccessPointInfo info) {

        Intent intent = new Intent();
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setClass(currentContext, InputPasswordActivity.class);
        intent.putExtra(EXTRA_APINFO, info);
        currentContext.startActivityForResult(intent, RequestCode.PASS_INPUT);
    }

    public void launchStatusActivity(ConnectionStatus status) {

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(EXTRA_CONSTATUS, status);
        intent.setClass(currentContext, ShowStatusActivity.class);
        currentContext.startActivityForResult(intent, RequestCode.STATUS_SHOW);

    }

    public void launchSelectActivity(AccessPointInfo[] nets, boolean forConnection,
                    boolean fromStorage) {

        if (fromStorage) {
            NetworkManager manager = new NetworkManager(Commons.getNetworkStorageFile());
            nets = manager.getKnownNetworks();

            if (nets == null || nets.length == 0) {
                Toast toast = Toast.makeText(currentContext, "No saved network", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }

        Intent intent = new Intent(currentContext, SelectNetworkActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(EXTRA_APINFO_ARR, nets);

        if (forConnection) {
            intent.putExtra(EXTRA_REQCODE, RequestCode.SELECT_CONN);
            currentContext.startActivityForResult(intent, RequestCode.SELECT_CONN);
        } else {
            intent.putExtra(EXTRA_REQCODE, RequestCode.SELECT_DETAILS);
            currentContext.startActivityForResult(intent, RequestCode.SELECT_DETAILS);
        }

    }

    public void launchDetailsActivity(AccessPointInfo info) {

        Intent intent = new Intent(currentContext, NetworkDetailsActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(EXTRA_APINFO, info);
        currentContext.startActivityForResult(intent, RequestCode.DETAILS_SHOW);

    }

    public void launchInputSsidActivity() {

        Intent intent = new Intent();
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setClass(currentContext, InputSsidActivity.class);
        currentContext.startActivityForResult(intent, RequestCode.CONNECT_HIDDEN);

    }

}
