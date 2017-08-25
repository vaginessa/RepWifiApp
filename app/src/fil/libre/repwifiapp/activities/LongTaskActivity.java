package fil.libre.repwifiapp.activities;

import fil.libre.repwifiapp.ActivityLauncher;
import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.Utils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class LongTaskActivity extends Activity {

    private class Task extends AsyncTask<Object, Object, Object> {

        private int REQ_CODE;

        public Task(int reqCode, Object input) {
            this.REQ_CODE = reqCode;
        }

        @Override
        protected Object doInBackground(Object... params) {

            Object ret = null;

            switch (this.REQ_CODE) {

            case ActivityLauncher.RequestCode.CONNECT:

                ret = Commons.connectionEngine.connect((AccessPointInfo) params[0]);
                break;

            case ActivityLauncher.RequestCode.NETWORKS_GET:

                ret = Commons.connectionEngine.getAvailableNetworks();
                break;

            case ActivityLauncher.RequestCode.STATUS_GET:

                ret = Commons.connectionEngine.getConnectionStatus();
                break;

            default:

                break;

            }

            return ret;

        }

        @Override
        protected void onPostExecute(Object result) {
            taskCompleted(result, this.REQ_CODE);
        }

    }

    private AccessPointInfo currentNetwork = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_task);

        toggleProgbar(Commons.isProgbarEnabled());
        startTask();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void startTask() {

        // retrieve the request code:
        Intent intent = getIntent();
        if (!intent.hasExtra(ActivityLauncher.EXTRA_REQCODE)) {
            this.setResult(RESULT_CANCELED);
            finish();
        }

        Object input = null;
        int reqCode = intent.getExtras().getInt(ActivityLauncher.EXTRA_REQCODE);

        switch (reqCode) {

        case ActivityLauncher.RequestCode.CONNECT:
            // Extract AccessPointInfo
            input = intent.getExtras().getSerializable(ActivityLauncher.EXTRA_APINFO);
            currentNetwork = (AccessPointInfo) input;
            setTitle("Connecting to " + currentNetwork.getSsid() + "...");
            setMessage("Connecting to " + currentNetwork.getSsid() + "...");
            break;

        case ActivityLauncher.RequestCode.NETWORKS_GET:
            setTitle("Scanning...");
            setMessage("Scanning for Networks...");

        case ActivityLauncher.RequestCode.STATUS_GET:
            setTitle("Checking status...");
            setMessage("Checking status...");

        default:
            setTitle("Please wait...");
            setMessage("Please wait...");
            break;
        }

        Task task = new Task(reqCode, input);
        task.execute(input);

    }

    private void taskCompleted(Object result, int reqCode) {

        Utils.logDebug("Finished long task reqCode: " + reqCode, 1);

        // Return to caller:
        Intent intent = this.getIntent();

        switch (reqCode) {

        case ActivityLauncher.RequestCode.CONNECT:

            intent.putExtra(ActivityLauncher.EXTRA_BOOLEAN, (Boolean) result);
            intent.putExtra(ActivityLauncher.EXTRA_APINFO, this.currentNetwork);
            break;

        case ActivityLauncher.RequestCode.NETWORKS_GET:

            intent.putExtra(ActivityLauncher.EXTRA_APINFO_ARR, (AccessPointInfo[]) result);
            break;

        case ActivityLauncher.RequestCode.STATUS_GET:

            intent.putExtra(ActivityLauncher.EXTRA_CONSTATUS, (ConnectionStatus) result);
            break;

        default:

            Utils.logDebug("Task terminating in null: ", 1);
            break;

        }

        this.setResult(RESULT_OK, intent);
        finish();

    }

    private void setMessage(String msg) {
        TextView txt = (TextView) findViewById(R.id.txt_msg);
        txt.setText(msg);
    }

    private void toggleProgbar(boolean enable) {

        ProgressBar pb = (ProgressBar) findViewById(R.id.progbar);

        if (enable) {
            pb.setVisibility(View.VISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        // suppress back button
    }

}
