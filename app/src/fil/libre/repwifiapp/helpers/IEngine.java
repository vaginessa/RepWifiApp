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


public interface IEngine {
	
	public boolean startWpaSupplicant();
	
	public boolean killPreviousConnections();
	
	public boolean clearWorkingDir();
		
	public AccessPointInfo[] getAvailableNetworks();
	
	public boolean connect(AccessPointInfo info);
	
	public boolean disconnect();
	
	public ConnectionStatus getConnectionStatus();
	
		
}
