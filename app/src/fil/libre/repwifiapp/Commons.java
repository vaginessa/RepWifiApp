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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fil.libre.repwifiapp.helpers.Engine4p2;
import fil.libre.repwifiapp.helpers.Engine6p0;
import fil.libre.repwifiapp.helpers.IEngine;
import fil.libre.repwifiapp.helpers.NetworkManager;
import fil.libre.repwifiapp.helpers.Utils;



public abstract class Commons {

	private static Context currentContext;
	public Context getContext(){
		return currentContext;
	}
	
	//------------- Enviromnet Constants ----------------------------------------
	public static final int EXCOD_ROOT_DISABLED = 255;
	public static final int EXCOD_ROOT_DENIED = 1;
	public static final String v4p2 = "4.2";
	public static final String v6p0 = "6.0";
	public static final String SCAN_FILE_HDR = "bssid / frequency / signal level / flags / ssid";
	public static final String INTERFACE_NAME="wlan0";
	public static final String WORKDIR = "/data/misc/wifi";
	public static final String PID_FILE = WORKDIR + "/pidfile";
	public static final String SOCKET_DIR = WORKDIR + "/sockets/";
	public static final String SOFTAP_FILE = WORKDIR + "/softap.conf";
	public static final String P2P_CONF = WORKDIR + "/p2p_supplicant.conf";
	public static final String WPA_CONF = WORKDIR + "/wpa_supplicant.conf";
	public static final String ENTROPY_FILE = WORKDIR + "/entropy.bin";
	public static final String OVERLAY_FILE = "/system/etc/wifi/wpa_supplicant_overlay.conf";
	//------------------------------------------------------------------------------
			
	//------------- Shared Engines -----------------------
	public static IEngine connectionEngine = null;
	public static NetworkManager storage = null;
	//----------------------------------------------------
	
	
	//------------- Shared Resources ---------------------
	public static int colorThemeDark;
	public static int colorThemeLight;
	public static int colorBlack;
	
	public static int getLogPriority(){
		
		SharedPreferences sets = getSettings();
		return Integer.parseInt(sets.getString("debug_priority","3"));
		
	}
	
	public static SharedPreferences getSettings(){
		return PreferenceManager.getDefaultSharedPreferences(currentContext);
	}
	//----------------------------------------------------
	
	
	//------------- Activity Interaction -----------------
	public static final String EXTRA_APINFO = "ExAPInfo";
	public static final String EXTRA_APINFO_ARR = "ExAPInfoArr";
	public static final String EXTRA_CONSTATUS = "ExConSts";
	public static final String EXTRA_BOOLEAN = "ExBool";
	public static final String EXTRA_REQCODE = "ExReqCode";
	public static final String EXTRA_RESCAN = "ExRescan";
	public static final String EXTRA_DELETE = "ExDelete";
	
	public class RequestCode{
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
	}
	//----------------------------------------------------
	

	//----------------- Application Files --------------------
	private static String APP_DATA_FOLDER;
	
	public static String getNetworkStorageFile(){
		if (APP_DATA_FOLDER == null){
			return null;
		}else{
			return APP_DATA_FOLDER + "/repwifi_storage.conf";
		}
	}
	public static String getScriptScan(){
		return APP_DATA_FOLDER + "/scan.sh";	
	}	
	public static String getScriptScanRes(){
		return APP_DATA_FOLDER + "/get_scan_results.sh";
	}
	public static String getScriptDhcpcd(){
		return APP_DATA_FOLDER + "/run_dhcpcd.sh";
	}
	public static String getScanFile(){
		return APP_DATA_FOLDER + "/scanres.txt";
	}
	public static String getStatusFile(){
		return APP_DATA_FOLDER + "/tmpStatus";
	}
	public static String getGwFile(){
		return APP_DATA_FOLDER + "/gw.txt";
	}
	public static String getTempOutFile(){
		return APP_DATA_FOLDER + "/tmpout.txt";
	}
	//--------------------------------------------------------
	
	
	//----------- Initialization methods ---------------------------
	public static boolean init(Context context){
		
		currentContext = context;
		
		try {
			

			colorThemeDark = currentContext.getResources().getColor(R.color.ThemeDark);
			colorThemeLight = currentContext.getResources().getColor(R.color.ThemeLight);
			colorBlack = currentContext.getResources().getColor(R.color.black);
			APP_DATA_FOLDER = currentContext.getExternalFilesDir(null).getAbsolutePath();
			
			initEngine();
			initNetworkStorage();
					
			
			return true;
			
		} catch (Exception e) {
			Utils.logError("Error initializing common resources.",e);
			return false;
		}
	}
	
	private static void initEngine() throws Exception{
		
		String vers = android.os.Build.VERSION.RELEASE;
		
		if (vers.startsWith(Commons.v4p2)){
			Commons.connectionEngine = new Engine4p2();
		}
		else if(vers.startsWith(Commons.v6p0)){
			Commons.connectionEngine = new Engine6p0();
		}
		else{
			throw new Exception("System version not recognized!");
		}
		
	}
	
	private static void initNetworkStorage() throws Exception{
		
		Commons.storage = new NetworkManager(getNetworkStorageFile());
		
	}
	//--------------------------------------------------------------
	
	
}
