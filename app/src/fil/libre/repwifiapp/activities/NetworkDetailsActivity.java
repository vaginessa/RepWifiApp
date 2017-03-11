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

import java.util.Date;


import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NetworkDetailsActivity extends Activity implements OnCheckedChangeListener {

	private AccessPointInfo currentNetwor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_details);
		
		CheckBox c = (CheckBox)findViewById(R.id.chk_show_pass_details);
		c.setOnCheckedChangeListener(this);
		
		Intent intent = getIntent();
		if (! intent.hasExtra(Commons.EXTRA_APINFO)){
			this.setResult(RESULT_CANCELED);
			this.finish();
			return;
		}
		
		this.currentNetwor = (AccessPointInfo)intent.getExtras().getSerializable(Commons.EXTRA_APINFO);
		if (this.currentNetwor == null){
			this.setResult(RESULT_CANCELED);
			this.finish();
			return;
		}
		
		loadNetwork(false);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_manage_networks, menu);
		return true;
	}
	
	private void loadNetwork(boolean showPassword){
		
		setTitle(this.currentNetwor.getSSID());
		
		TextView v = (TextView)findViewById(R.id.txt_net_details);
		v.setText("SSID: " + this.currentNetwor.getSSID());
		v.append("\nBSSID: " + this.currentNetwor.getBSSID());
		
		long lastused = this.currentNetwor.getLastTimeUsed();
		if (lastused > 0){
			Date ts = new Date(lastused);
			String formstring = "dd-MMM-yyyy kk:mm:ss";
			v.append("\nLast Used: " + DateFormat.format(formstring, ts));
		}
		
		if (showPassword){
			v.append("\n\nPassword:\n" + this.currentNetwor.getPassword());
		}else{
			v.append("\n\n\n");
		}
		
	}
	
	public void btnDeleteClick(View v){
		
		String msg = getResources().getString(R.string.msg_confirm_delete_network);
		String yes = getResources().getString(R.string.yes);
		String no = getResources().getString(R.string.no);
		
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
	    dlgAlert.setMessage(msg); 
	    dlgAlert.setPositiveButton(yes,new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int whichButton) {
	             returnResult(true);
	        }
	   });
	    dlgAlert.setNegativeButton(no,new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int whichButton) {
	        	 //nothing
	        }
	   });
	    
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if (buttonView == findViewById(R.id.chk_show_pass_details)){
			loadNetwork(isChecked);			
		}
		
	}
	
	private void returnResult(boolean delete){
		
		Intent i = new Intent();
		i.putExtra(Commons.EXTRA_DELETE, delete);
		i.putExtra(Commons.EXTRA_APINFO, this.currentNetwor);
		this.setResult(RESULT_OK,i);
		finish();
		
	}

	
	
}
