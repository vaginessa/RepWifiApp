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

import java.io.File;
import java.util.ArrayList;

public class NetworkManager {

	private static final String F_SEP = "\t";
	private static final int NET_MAX_AGE = 365; //Expressed in days
	
	private String _knownNetworksFile = null;
		
	public NetworkManager(String networksFilePath){
		this._knownNetworksFile = networksFilePath;
	}
	
	private AccessPointInfo searchInFile(AccessPointInfo i){
		
		if (i == null){
			return null;
		}
		
		String bssid = i.getBSSID();
		String ssid = i.getSSID();
		
		if (bssid == null || ssid == null || bssid.trim().equals("") || ssid.trim().equals("")){
			return null;
		}		
		
		AccessPointInfo ret = null;
		AccessPointInfo[] list = getKnownNetworks();
		
		if (list == null){
			return null;
		}
		
		for(AccessPointInfo toTest : list){
		
			// try to match both bssid and ssid.
			// if bssid doesn't match, but ssid does, 
			// then the network is a candidate.
			// if no bssid equality is found,
			// then return the best match (only ssid), if any
			if (toTest.getSSID().equals(ssid)){
				
				if (toTest.getBSSID().equals(bssid)){
					i.setPassword(toTest.getPassword());
					return i;
					
				}else{
					i.setPassword(toTest.getPassword());
					ret = i;
				}
				
			}
			
		}		
		
		return ret;
		
	}
	
	private boolean saveOrRemove(AccessPointInfo info, boolean save){
		
		String iText = InfoToString(info);
		if (iText == null){
			return false;
		}

		AccessPointInfo[] existingNets = getKnownNetworks();
				
		ArrayList<AccessPointInfo> newlist = new ArrayList<AccessPointInfo>();

		if (existingNets == null || existingNets.length == 0){
			//no existing storage yet, create it
			
			if (save){
				//set timestamp
				info.setLastTimeUsed(System.currentTimeMillis());
				newlist.add(info);
				AccessPointInfo[] newContents = new AccessPointInfo[newlist.size()];
				newContents = newlist.toArray(newContents);

				return saveList(newContents);

			}else{
				//nothing to do, return
				return true;
			}
			
		}
		
		
		if (save){
			//add the updated info to the storage
			info.setLastTimeUsed(System.currentTimeMillis());
			newlist.add(info);
		}

		for(AccessPointInfo old : existingNets){

			if (old == null){
				//error while loading from file. skip.
				continue;
			}
		
			// keep network only if it's not older than the max age for a network 
			else if (old.isOlderThan(NET_MAX_AGE)){
				//skip it
				continue;
			}
			
			else if (old.getBSSID().equals(info.getBSSID()) && old.getSSID().equals(info.getSSID())){
				//found previously saved entry for the same network we are managing
				//skip it
				continue;
			}
			
			else{
				//old network info that can be kept in the storage
				newlist.add(old);
			}
			
		}
		

		AccessPointInfo[] newContents = new AccessPointInfo[newlist.size()];
		newContents = newlist.toArray(newContents);

		return saveList(newContents);

	}
	
	private AccessPointInfo getFromString(String savedString){
		
		if (savedString == null || savedString.trim().equals("")) {
			return null;
		}
		
		String[] fields = savedString.split(F_SEP);
		
		if (fields.length != 4 ){
			return null;
		}
		
		String bssid = fields[0];
		String ssid = fields[1];
		String pass = fields[2];
		String lastUsed = fields[3];
		
		long lastusedmillis = 0;
		try {
			lastusedmillis = Long.parseLong(lastUsed);			
		} catch (NumberFormatException e) {
			//invalid format
			Utils.logError("Invalid time format in network manager \""+lastUsed +"\". Network BSSID: " + bssid, e);
		}
				
		if (bssid.trim().equals("") || ssid.trim().equals("") || pass.trim().equals("")){
			return null;
		}
		
		AccessPointInfo i = new AccessPointInfo(ssid, bssid, null, null, null);
		i.setPassword(pass);
		i.setLastTimeUsed(lastusedmillis);
				
		return i;
		
	}
	
	private String InfoToString(AccessPointInfo info){
		
		if (info == null){
			return null;
		}
		
		String bssid = info.getBSSID();
		String ssid = info.getSSID();
		String pass = info.getPassword();
		String tsLastUsed = "" + info.getLastTimeUsed();
		
		if (bssid == null || bssid.trim().equals("")){
			return null;
		}
		
		if (ssid == null || ssid.trim().equals("")){
			return null;
		}
		
		if (pass == null || pass.trim().equals("")){
			return null;
		}		
			
		String iText = info.getBSSID() + F_SEP + info.getSSID() + F_SEP + info.getPassword() + F_SEP + tsLastUsed;
		return iText;
		
	}
	
	private boolean saveList(AccessPointInfo[] list){
		
		if (list == null){
			return false;
		}
		
		String[] lines = new String[list.length];
		
		for (int i = 0; i<list.length; i++){
			
			String storeString = InfoToString(list[i]);
			if (storeString == null){
				return false;
			}
			lines[i] = storeString; 
		
		}
		
		return Utils.writeFileLines(this._knownNetworksFile,lines, true);
		
	}
	
	public AccessPointInfo[] getKnownNetworks(){
		
		ArrayList<AccessPointInfo> list = new ArrayList<AccessPointInfo>();
		
		File f = new File(this._knownNetworksFile);
		if (! f.exists()){
			return null;
		}

		String[] lines = Utils.readFileLines(_knownNetworksFile);
		if (lines == null || lines.length == 0){
			return null;
		}
		
		for(String l : lines){
			
			AccessPointInfo info = getFromString(l);
			if (info != null){
				list.add(info);
			}
			
		}
		
		AccessPointInfo[] ret = new AccessPointInfo[list.size()];
		ret = list.toArray(ret);
		
		return ret;
		
	}
	
	public boolean isKnown(AccessPointInfo info){
		
		AccessPointInfo i = searchInFile(info);
		if (i == null){
			return false;
		}else {
			return true;
		}
		
	}
	
	public boolean save(AccessPointInfo info){
		return saveOrRemove(info, true);
	}
	
	public boolean remove(AccessPointInfo info){
		return saveOrRemove(info, false);
	}
	
	public AccessPointInfo getSavedNetwork(AccessPointInfo i){
		return searchInFile(i);
	}
	
	
	
}
