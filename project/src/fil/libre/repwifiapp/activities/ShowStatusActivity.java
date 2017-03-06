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
import fil.libre.repwifiapp.helpers.ConnectionStatus;
import fil.libre.repwifiapp.helpers.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShowStatusActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_status);
		showStatus();		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_show_status, menu);
		return true;
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		showStatus();
	}
	
	private void setMessage(String msg){
		TextView view = (TextView)findViewById(R.id.txt_status);
		view.setText(msg);
	}
	
	private void showStatus(){
		
		ConnectionStatus status = Commons.connectionEngine.getConnectionStatus();
		if (status == null){
			this.finish();
		
		}else if (status.isConnected()){
			Utils.logDebug("StatusActivity isConnected,showing buttons");
			setMessage("Connected to " + status.SSID + "\n\n" + "IP Address: " + status.IP + "\n");
			toggleBtnDisconnect(true);
			
			
		}else{
			Utils.logDebug("StatusActivity status Else");
			setMessage("Status:\n" + status.status);
			toggleBtnDisconnect(false);
			
		}
	}
	
	private void toggleBtnDisconnect(boolean enable){
		
		Button b = (Button)findViewById(R.id.btn_disconnect);
		Button bk = (Button)findViewById(R.id.btn_back);
		b.setEnabled(enable);
		bk.setEnabled(! enable);
		
		if (enable){
			b.setVisibility(View.VISIBLE);
			bk.setVisibility(View.INVISIBLE);
		}else{
			b.setVisibility(View.INVISIBLE);
			bk.setVisibility(View.VISIBLE);
		}
		
	}
	
	public void onBtnDisconnectClick(View v){
	
		boolean res = Commons.connectionEngine.disconnect();
		String msg = "";
		if (res){
			msg = "Disconnected.";
		}
		else{
			msg = "FAILED to disconnect!";
		}
		
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
		
		showStatus();
		
	}
	
	public void onBtnMainClick(View v){
		finish();
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
}
