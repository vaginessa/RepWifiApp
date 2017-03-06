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

package fil.libre.repwifiapp.helpers;

import fil.libre.repwifiapp.Commons;


public class Engine4p2 extends Engine{

	@Override
	protected String getCmdWpaStart(){
		return "wpa_supplicant -B -dd -i" + Commons.INTERFACE_NAME + " -C\"" + Commons.SOCKET_DIR + "\" -c\"" + Commons.WPA_CONF + "\" -P\"" + Commons.PID_FILE + "\"";
	}
	
	public boolean loadModules(){
		try {
			//TODO
			//implement kernel modules loading
			return true;
		} catch (Exception e) {
			Utils.logError("Error while loading kernel modules",e);
			return false;
		}
	}
	
	@Override
	public boolean connect(AccessPointInfo info){
		
		killPreviousConnections();
				
		if (info == null){
			Utils.logDebug("Engine's connect() received a null AccessPointInfo");
			return false;
		}
		
		if (! createConfigFile(info)){
			return false;
		}
		
		//launch wpa_supplicant specifying our custom configuration and the socket file
		if (! executeRootCmd(getCmdWpaStart())){
			Utils.logError("wpa_supplicant connection command failed.");
			return false;
		}

		//negotiate DHCP lease
		if (!runDhcpcd()){
			return false;
		}

		//set DNS's
		if (! executeRootCmd("setprop net.dns1 " + DNS1)){
			Utils.logError("setting dns1 failed");
			return false;
		}

		if (! executeRootCmd("setprop net.dns2 " + DNS2)){
			Utils.logError("setting dns2 failed");
			return false;
		}

		//TODO
		//implement wpa_cli command to query wpa_supplicant's state
		//in order to confirm that connection was successful.

		return true;
		
	}
	
	private boolean createConfigFile(AccessPointInfo info){
		
		try {
						
			if (! deleteFileIfExists(Commons.WPA_CONF)){
				Utils.logError("Unable to remove wpa_supplicant.conf before writing it.");
				return false;				
			}
			
			String configText = "ctrl_interface=DIR=" + Commons.SOCKET_DIR + "\n" +
							"update_config=1\n" +
							"network={\n"+
							"	ssid=\"" + info.getSSID() + "\"\n";
						
			if (info.needsPassword()){
				configText += "	psk=\""+ info.getPassword() + "\"\n";
			}else {
				configText += "	key_mgmt=NONE\n";
			}					
			
			configText += "}\n";
			
			if ( ! Utils.writeFile(Commons.WPA_CONF, configText, true) ){
				Utils.logError("Unable to write wpa_supplicant.conf file!");
				return false;
			}
			
			//chmod wpa_supplicant.conf, in order to make it accessible
			if(chmodFile(Commons.WPA_CONF, "666")){
				return true;
			}else {
				Utils.logError("Unable to chmod wpa_supplicant.conf");
				return false;			
			}
						
		} catch (Exception e) {
			Utils.logError("Error while creating wpa_supplicant.conf",e);
			return false;
		}
		
	}

	
}
