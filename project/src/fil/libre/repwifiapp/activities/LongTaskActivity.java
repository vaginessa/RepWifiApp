package fil.libre.repwifiapp.activities;


import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.Utils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class LongTaskActivity extends Activity {


	private class Task extends AsyncTask<Object, Object, Object>{

		private int REQ_CODE;

		public Task(int reqCode, Object input){
			this.REQ_CODE = reqCode;
		}

		@Override
		protected Object doInBackground(Object... params) {

			Object ret = null;

			switch (this.REQ_CODE){

			case Commons.RequestCode.CONNECT:

				ret = Commons.connectionEngine.connect((AccessPointInfo)params[0]);
				break;

			case Commons.RequestCode.NETWORKS_GET:

				ret = Commons.connectionEngine.getAvailableNetworks();
				break;

			case Commons.RequestCode.STATUS_GET:

				ret = Commons.connectionEngine.getConnectionStatus();
				break;

			default:

				break;

			}

			return ret;

		}

		protected void onPostExecute(Object result) {
			taskCompleted(result, this.REQ_CODE);
		}

	}

	private AccessPointInfo currentNetwork = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_long_task);
			
		startTask();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}

	private void startTask() {

		//retrieve the request code:
		Intent intent = getIntent();
		if (! intent.hasExtra(Commons.EXTRA_REQCODE)){
			this.setResult(RESULT_CANCELED);
			finish();
		}

		Object input = null;
		int reqCode = intent.getExtras().getInt(Commons.EXTRA_REQCODE);
			
		switch (reqCode) {
		
		case Commons.RequestCode.CONNECT:
			setTitle("Connecting...");
			setMessage("Connecting...");			
			//Extract AccessPointInfo
			input = intent.getExtras().getSerializable(Commons.EXTRA_APINFO);
			currentNetwork = (AccessPointInfo)input;			
			break;
			
		case Commons.RequestCode.NETWORKS_GET:
			setTitle("Scanning...");
			setMessage("Scanning for Networks...");
			
		case Commons.RequestCode.STATUS_GET:
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

	private void taskCompleted(Object result, int reqCode){

		Utils.logDebug("Finished long task reqCode: "+ reqCode,1);
		
		//Return to caller:
		Intent intent = this.getIntent();


		switch (reqCode){

		case Commons.RequestCode.CONNECT:

			intent.putExtra(Commons.EXTRA_BOOLEAN, (Boolean)result);
			intent.putExtra(Commons.EXTRA_APINFO, this.currentNetwork);
			break;

		case Commons.RequestCode.NETWORKS_GET:

			intent.putExtra(Commons.EXTRA_APINFO_ARR, (AccessPointInfo[])result);
			break;

		case Commons.RequestCode.STATUS_GET:

			intent.putExtra(Commons.EXTRA_CONSTATUS, (ConnectionStatus)result);
			break;

		default:

			Utils.logDebug("Task terminating in null: ",1);
			break;

		}

		this.setResult(RESULT_OK, intent);
		finish();

	}

	
	private void setMessage(String msg) {
		TextView txt = (TextView)findViewById(R.id.txt_msg);
		txt.setText(msg);
	}

	@Override
	public void onBackPressed() {
		//suppress
	}



}
