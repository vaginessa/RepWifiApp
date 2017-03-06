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

import java.io.Serializable;

public class ConnectionStatus implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String STATUS_CONNECTED = "COMPLETED";
	public static final String STATUS_INACTIVE = "INACTIVE";
	public static final String STATUS_DISCONNECTED = "DISCONNECTED";
	public static final String STATUS_UNDEFINED = "UNDEFINED";
	
	public String status;
	public String SSID;
	public String BSSID;
	public String IP;
	
	private static final String F_SEP = "=";
	private static final String KeyStatus = "wpa_state";
	private static final String KeySSID = "ssid";
	private static final String KeyBSSID = "bssid";
	private static final String KeyIP = "ip_address";
	
	public static ConnectionStatus parseWpaCliOutput(String wpaCliOutput){
		
		if (wpaCliOutput == null){
			return null;
		}
		
		if (wpaCliOutput.trim().length() == 0){
			return null;
		}
		
		String[] lines = wpaCliOutput.split("\n");
		
		ConnectionStatus s = new ConnectionStatus();
		for(String line : lines){
			
			if (line.trim().equals("")){
				continue;
			}
			
			String[] fields = line.split(F_SEP);
			if(fields.length < 2){
				continue;
			}
			
			String key = fields[0];
			String val = fields[1];
			
			if (key.equals(KeyBSSID)){
				s.BSSID = val;
			}
			else if (key.equals(KeySSID)){
				s.SSID = val;
			}
			else if (key.equals(KeyStatus)){
				s.status = val;
			}
			else if (key.equals(KeyIP)){
				s.IP = val;
			}
			
		}
		
		return s;
				
	}
	
	public boolean isConnected(){
		
		if (this.status == null){
			return false;
		}
		
		if (this.status.equals(STATUS_CONNECTED)){
			return true;
		}else{
			return false;
		}
	}
	
}
