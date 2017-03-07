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




import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import fil.libre.repwifiapp.helpers.NetworkButton;
import fil.libre.repwifiapp.helpers.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SelectNetworkActivity extends Activity implements OnClickListener {

	private AccessPointInfo[] aps;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_network);

		setTitle("Select network");
		
		getNetworks();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_select_network, menu);
		return true;
	}
	
	private void writeOut(String msg) {

		TextView v = (TextView)findViewById(R.id.txt_selnets);
		v.setText(msg);

	}

	private void getNetworks(){
		
		Intent intent = getIntent();
		if(! intent.hasExtra(Commons.EXTRA_APINFO_ARR)){
			this.setResult(RESULT_CANCELED);
			finish();
			return;
		}
		AccessPointInfo[] nets = (AccessPointInfo[])intent.getExtras().getSerializable(Commons.EXTRA_APINFO_ARR);
		if (nets == null){
			this.setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		int reqCode = intent.getExtras().getInt(Commons.EXTRA_REQCODE);
		
		this.aps = nets;
		
		if (reqCode == Commons.RequestCode.SELECT_CONN){
			showNetworksForConnection(nets);
		}
		else{
			showNetworksForManagement(nets);
		}
		
	}
	
	public void btnScanClick(View v){
		returnResults(null, true);
	}
	
	@Override
    public void onClick(View v) {

		if (v instanceof NetworkButton){
			networkNameClick((NetworkButton)v);
		}

    }
	
	public void networkNameClick(NetworkButton b){
		
		for(AccessPointInfo i : this.aps){

			if (i.getBSSID().equals(b.getNetworkBSSID())){

				returnResults(i,false);

			}

		}
	}

	private void returnResults(AccessPointInfo i, boolean rescan){
		
		Intent intent = new Intent();
		intent.putExtra(Commons.EXTRA_APINFO, i);
		intent.putExtra(Commons.EXTRA_RESCAN, rescan);
		setResult(RESULT_OK, intent);
		finish();
		
	}

	private void showNetworksForConnection(AccessPointInfo[] info) {
				
		if (info == null){
			Utils.logError("Unable to retrieve network list!");
			writeOut("Unable to retrieve network list!");
			return;
		}

		if (info.length == 0){
			writeOut("No network found.");
			toggleBtnRescan(true);
			return;
		}
		
		writeOut("Select the network you want to connect to:");
		toggleBtnRescan(false);
		
		for (AccessPointInfo i : info){
			
			addButtonForNetwork(i);
			
		}

	}

	private void showNetworksForManagement(AccessPointInfo[] info){
		
		if (info == null || info.length == 0){
			return;
		}

		writeOut("Select network info to manage:");
		toggleBtnRescan(false);
		
		for (AccessPointInfo i : info){
			
			addButtonForNetwork(i);
			
		}
		
	}
	
	private void toggleBtnRescan(boolean enable) {

		Button b = (Button)findViewById(R.id.btn_rescan);
		if (enable){
			b.setVisibility(View.VISIBLE);
		}
		else{
			b.setVisibility(View.INVISIBLE);
		}
	}

	private void addButtonForNetwork(AccessPointInfo info){
		
		TableLayout s = (TableLayout)findViewById(R.id.table_networks);
		TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
		TableRow row = new TableRow(this);
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.gravity = Gravity.FILL_HORIZONTAL;
		row.setPadding(10, 10, 10, 10);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.FILL_HORIZONTAL);
		row.setLayoutParams(rowParams);
		
		NetworkButton button = new NetworkButton(this, info.getBSSID());
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		button.setLayoutParams(params);
		button.setBackgroundColor(Commons.colorThemeDark);
		button.setTextColor(Commons.colorThemeLight);
		button.setTextSize(20);
		button.setPadding(10, 10, 10, 10);
		button.setGravity(Gravity.CENTER_HORIZONTAL);
		button.setText(info.getSSID());
		button.setOnClickListener(this);
		
		row.addView(button,params);
		s.addView(row,tableParams);
		s.setGravity(Gravity.FILL_HORIZONTAL);
		
	}

}
