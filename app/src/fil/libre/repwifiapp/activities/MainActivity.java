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

import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.Commons.RequestCode;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.NetworkManager;
import fil.libre.repwifiapp.helpers.RootCommand;
import fil.libre.repwifiapp.helpers.Utils;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity{
	
	private AlertDialog diag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(! Commons.init(this)){
			Utils.logDebug("Failed to initialize Commons. Aborting.");
			finish();
			return;
		}
		
		setImage();

		//checkConnectionStatus();
							
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Utils.logDebug("Main onStart()");
		
		checkConditions();
		
		ConnectionStatus status = Commons.connectionEngine.getConnectionStatus();
		if (status != null && status.isConnected()){
			Utils.logDebug("Main about to launch status activity...");
			launchStatusActivity(status);
		}
		
		Utils.logDebug("Main onStart() returning.");
			
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		if (this.diag != null){
			this.diag.dismiss();
		}
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_credits:
	        launchCreditsActivity();
	        break;
	        
	    case R.id.menu_config:
	    	launchSettingsActivity();
	    	break;
	    
	    default:
	       	break;
	    }
	    
	    return true;
	}
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		
		Utils.logDebug("Main onActivityResult(): ",1);
		
		if (intent == null){
			return;
		}
		
		if (resultCode != RESULT_OK){
			return;
		}
		
		AccessPointInfo i = null;
		if (intent.hasExtra(Commons.EXTRA_APINFO)){
			i = (AccessPointInfo)intent.getExtras().getSerializable(Commons.EXTRA_APINFO); 
		}
		
		switch (requestCode) {
		
		case RequestCode.PASS_INPUT:
			handleResultSetPass(i);
			break;

		case RequestCode.SELECT_CONN:
			boolean rescan = intent.getExtras().getBoolean(Commons.EXTRA_RESCAN);
			handleResultSelect(i, rescan);
			break;
			
		case RequestCode.CONNECT:
			boolean conres = intent.getExtras().getBoolean(Commons.EXTRA_BOOLEAN);
			handleFinishedConnecting(conres, i);			
			break;
			
		case RequestCode.STATUS_GET:
			ConnectionStatus status = (ConnectionStatus)intent.getExtras().getSerializable(Commons.EXTRA_CONSTATUS);
			handleResultGetStatus(status);
			break;
			
		case RequestCode.NETWORKS_GET:
			AccessPointInfo[] nets = (AccessPointInfo[])intent.getExtras().getSerializable(Commons.EXTRA_APINFO_ARR);
			launchSelectActivity(nets, true);
						
		case RequestCode.STATUS_SHOW:
			//do nothing			
			break;
			
		case RequestCode.SELECT_DETAILS:
			launchDetailsActivity(i);
			break;
			
		case RequestCode.DETAILS_SHOW:
			boolean del = intent.getExtras().getBoolean(Commons.EXTRA_DELETE);
			if (del){ deleteNetwork(i);	}
			break;
			
		default:
								
			break;
			
		}
		
		
	}
	
	private void setImage(){
		
		ImageView img = (ImageView)findViewById(R.id.img_logo);
		
		try {
			Drawable d = Drawable.createFromStream(getAssets().open("repwifi-logo-0.png"),null);
			img.setImageDrawable(d);
		} catch (IOException e) {
			Utils.logError("Error while loading logo image",e);
		}
		
	}
	

	private void handleResultSelect(AccessPointInfo i, boolean rescan){
		
		if (rescan){
			
			doScan();
			
		}else if (i != null){
			
			if (i.needsPassword()){
			
				//try to fetch network's password from storage
				AccessPointInfo fromStorage = Commons.storage.getSavedNetwork(i);
				if (fromStorage == null){
					
					launchPasswordActivity(i);
					return;
					
				}else{
					//use fetched network
					i = fromStorage;
				}
				
			}

			connectToNetwork(i);
		}
		
	}
	
	private void handleResultSetPass(AccessPointInfo i){
		connectToNetwork(i);
	}
		
	private void handleResultGetStatus(ConnectionStatus status){
		if (status != null && status.isConnected()){
			launchStatusActivity(status);
		}
	}

	private void handleFinishedConnecting(boolean connectionResult, AccessPointInfo info){
		
		if(connectionResult && info.needsPassword()){
			
			//Save network
			if (Commons.storage.save(info)){
				Toast toast2 = Toast.makeText(getApplicationContext(), "Network Saved!",Toast.LENGTH_LONG);
				toast2.show();				
				
			}else {
				Toast toast2 = Toast.makeText(getApplicationContext(), "FAILED to save network!",Toast.LENGTH_LONG);
				toast2.show();
			}
			
			checkConnectionStatus();
						
		}else{
			//alert that connection failed
			Toast toast = Toast.makeText(getApplicationContext(), "FAILED to connect!", Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	private void launchPasswordActivity(AccessPointInfo info){
		
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setClass(getApplicationContext(), InputPasswordActivity.class);
		intent.putExtra(Commons.EXTRA_APINFO, info);
		
		startActivityForResult(intent, RequestCode.PASS_INPUT);
		
	}
	
	private void launchStatusActivity(ConnectionStatus status){
		
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_CONSTATUS, status);
		intent.setClass(getApplicationContext(), ShowStatusActivity.class);
		startActivityForResult(intent, RequestCode.STATUS_SHOW);
		
	}

	private void launchSelectActivity(AccessPointInfo[] nets,boolean forConnection){
	
		Intent intent = new Intent(this, SelectNetworkActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_APINFO_ARR, nets);
		
		if (forConnection){
			intent.putExtra(Commons.EXTRA_REQCODE, RequestCode.SELECT_CONN);
			startActivityForResult(intent, RequestCode.SELECT_CONN);
		}
		else{
			intent.putExtra(Commons.EXTRA_REQCODE, RequestCode.SELECT_DETAILS);
			startActivityForResult(intent, RequestCode.SELECT_DETAILS);
		}
		
	}
	
	private void launchDetailsActivity(AccessPointInfo info){
		
		Intent intent = new Intent(this, NetworkDetailsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_APINFO, info);
		startActivityForResult(intent, RequestCode.DETAILS_SHOW);
		
	}
	
	private void launchCreditsActivity(){
		Intent intent = new Intent(this, CreditsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivityForResult(intent, RequestCode.NONE);
	}
	
	private void launchSettingsActivity() {
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}
	
	private void deleteNetwork(AccessPointInfo info){
		
		NetworkManager manager = new NetworkManager(Commons.getNetworkStorageFile());
		String msg = "";
		if (manager.remove(info)){
			msg = "Network info deleted!";
		}else{
			msg = "FAILED to delete network info!";
		}
		
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.show();
		
	}
	
	private void connectToNetwork(AccessPointInfo info){
		
		Intent intent = new Intent(this, LongTaskActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_REQCODE, RequestCode.CONNECT);
		intent.putExtra(Commons.EXTRA_APINFO, info);
		startActivityForResult(intent, RequestCode.CONNECT);
				
	}
	
	private void checkConnectionStatus(){
		
		Intent intent = new Intent(this, LongTaskActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_REQCODE, RequestCode.STATUS_GET);
		startActivityForResult(intent, RequestCode.STATUS_GET);
		
	}
	
	private boolean checkConditions(){
		return (checkRootEnabled() && checkInterface());
	}
	
	private boolean checkInterface(){
		
		boolean res = Commons.connectionEngine.isInterfaceAvailable(Commons.INTERFACE_NAME);
				
		if(res == false ){
			String msg = getResources().getString(R.string.msg_interface_not_found);
			showMessage(msg);
		}
		
		return res;
				
	}
	
	private boolean checkRootEnabled(){
		
		boolean result = false;
		String msg = "Unknown Root error";
		RootCommand su = new RootCommand(null);
		
		int excode = -1;
		
		try {
			excode = su.execute();
		} catch (Exception e) {
			Utils.logError("Error while trying to get first Super User access.",e);
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
		
		if (!result){
			showMessage(msg);
		}

		return result;
				
	}
	
	private void doScan(){
		
		if (!checkConditions()){
			return;
		}
		
		Intent intent = new Intent(this, LongTaskActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(Commons.EXTRA_REQCODE, RequestCode.NETWORKS_GET);
		startActivityForResult(intent, RequestCode.NETWORKS_GET);
				
	}
	
	public void btnScanClick(View v){

		doScan();

	}

	public void btnManageClick(View v){

		NetworkManager manager = new NetworkManager(Commons.getNetworkStorageFile());
		AccessPointInfo[] infos = manager.getKnownNetworks();
		
		if (infos == null || infos.length == 0){
			Toast toast = Toast.makeText(this, "No saved network", Toast.LENGTH_LONG);
			toast.show();
		}
		else{
			launchSelectActivity(infos, false);
		}
		
	}
	
	private void showMessage(String msg){
		
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
	    dlgAlert.setMessage(msg); 
	    dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int whichButton) {
	        	return;
	        }
	    });
	    
	    dlgAlert.setCancelable(false);
	    this.diag = dlgAlert.create();
	    this.diag.show();
	    
	}
		
}
