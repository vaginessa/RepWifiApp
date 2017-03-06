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


public abstract class Engine implements IEngine{

	protected String getCmdWpaSup(){
		return "wpa_supplicant -B -dd -i" + Commons.INTERFACE_NAME + " -C\"" +Commons.SOCKET_DIR + "\" -P\"" + Commons.PID_FILE + "\"";
	}
	
	protected String getCmdWpaCli() {
		return "wpa_cli -p" + Commons.SOCKET_DIR + " -P" + Commons.PID_FILE  + " -i" + Commons.INTERFACE_NAME;
	}
		
	protected abstract String getCmdWpaStart();
	
	public static final String DNS1 = "193.183.98.154";
	public static final String DNS2 = "87.98.175.85";
		
	public boolean deleteFileIfExists(String filePath){

		if (filePath == null){
			return false;
		}

		if (filePath.contains("*")){
			//it's safer to reject bulk rm'ing
			return false;
		}

		if (filePath.contains(" -r ")){
			//only file rm'ing acceppted
			return false;
		}

		return executeRootCmd("if [ -e \""+ filePath + "\" ]; then rm \"" + filePath + "\"; fi");
	}

	public boolean chmodFile(String filePath, String mod){
		return executeRootCmd("chmod " + mod + " \"" + filePath + "\"");
	}

	@Override
	public boolean killPreviousConnections() {

		Utils.logDebug("killing wpa_supplicant..:");
		if (executeRootCmd("killall -SIGINT wpa_supplicant")){
			Utils.logDebug("Killed wpa_supplicant"); 
		}else{
			Utils.logDebug("Wpa_supplicant NOT killed.");
		}

		Utils.logDebug("killing dhcpcd..");
		if (executeRootCmd("killall -SIGINT dhcpcd")){
			Utils.logDebug("Killed dhcpcd"); 
		}else{
			Utils.logDebug("dhcpcd NOT killed.");
		}
		
			
		return true;
	
	}

	@Override
	public boolean clearWorkingDir(){

		Utils.logDebug("clearWorkingDir():");

		if (executeRootCmd("rm -r " + Commons.SOCKET_DIR)){
			Utils.logDebug("removed socket dir");
		}

		if (executeRootCmd("rm " + Commons.ENTROPY_FILE)){
			Utils.logDebug("removed entropy file");
		}

		if (executeRootCmd("rm " + Commons.PID_FILE)){
			Utils.logDebug("removed pidfile");
		}

		if (executeRootCmd("rm " + Commons.SOFTAP_FILE)){
			Utils.logDebug("removed softap file");
		}

		if (executeRootCmd("rm " + Commons.WPA_CONF)){
			Utils.logDebug("removed wpa conf file");
		}

		if (executeRootCmd("rm " + Commons.P2P_CONF)){
			Utils.logDebug("removed p2p conf file");
		}

	
		return true;

	}

	@Override
	public boolean startWpaSupplicant(){

		Utils.logDebug("startWpaSupplicant():");

		if (executeRootCmd(getCmdWpaSup())){
			return true;
		}else{
			Utils.logDebug("Failed to start wpa");
			return false;
		}

	}

	@Override
	public AccessPointInfo[] getAvailableNetworks(){

		Utils.logDebug("getAvailableNetworks():");
		
		killPreviousConnections();
		
		if (! clearWorkingDir()){
			Utils.logError("Failed clearing dir");
			return null;
		}
		
		if (! startWpaSupplicant()){
			Utils.logError("Failed starting wpa_supplicant");
			return null;
		}		

		if (! createScanScripts()){
			Utils.logError("Failed creating scripts");
			return null;
		}

		if (! scanNetworks()){
			Utils.logError("failed scanning networks");
			return null;
		}

		if (!getScanResults()){
			Utils.logError("failed getting scan results");
			return null;
		}

		//chmod 666 scan_file to make it readable
		if (!chmodFile(Commons.getScanFile(), "666")){
			Utils.logError("failed chmodding scan_file");
			return null;
		}

		AccessPointInfo[] a = AccessPointInfo.parseScanResult(Commons.getScanFile());
		if (a == null){
			Utils.logError("Unable to parse scan file into AccessPointInfo array");
		}

		
		return a;

	}

	@Override
	public abstract boolean connect(AccessPointInfo info);

	@Override
	public boolean disconnect(){

		if (! isWpaSupplicantRunning()){
			return true;
		}

		try {

			RootCommand su = new RootCommand(getCmdWpaCli() + " disconnect");
			if (su.execute() == 0){
				String out = su.getOutput();
				if (out != null && out.trim().replace("\n", "").equals("OK")){
					return true;
				}else {
					return false;
				}
			}
			else{
				return false;
			}

		} catch (Exception e) {
			Utils.logError("Error while enabling network", e);
			return false;
		}
	}

	/***
	 * returns null if unable to determine connection status for any reason.
	 */
	@Override
	public ConnectionStatus getConnectionStatus(){

		Utils.logDebug("called getConnecitonStatus()");
		if (! isWpaSupplicantRunning()){
			//wpa_supplicant is not running.
			//unable to determin status.
			Utils.logDebug("wpa not running, cannot get connection status.");
			return null;

		}

		try {
			
			RootCommand su = new RootCommand(getCmdWpaCli() + " status");
			if(su.execute() == 0){
				String out = su.getOutput();
				if (out == null || out.trim().equals("")){
					return null;
				}
				else {
					return ConnectionStatus.parseWpaCliOutput(out);
				}
			}
			else {
				return null;
			}			
			
		} catch (Exception e) {
			Utils.logError("Error while executing wpa_cli status", e);
			return null;
		}
		
	}

	public boolean runDhcpcd(){
		
		return executeRootCmd("dhcpcd " + Commons.INTERFACE_NAME);
						
	}

	public boolean interfaceUp(){
		return executeRootCmd("ifconfig " + Commons.INTERFACE_NAME + " up");
	}
	
	protected boolean executeRootCmd(String cmd){

		try {

			RootCommand c = new RootCommand(cmd);
			if ( c.execute() == 0){
				return true;
			}else {
				return false;
			}

		} catch (Exception e) {
			Utils.logError("Error executing \"" + cmd + "\"",e);
			return false;
		}
	}
	
	protected boolean isWpaSupplicantRunning(){

		boolean retval = false;

		try {

			RootCommand su = new RootCommand("pidof wpa_supplicant");
			if (su.execute() == 0){

				if (su.getOutput().trim().equals("")){
					retval = false;
				}else{
					retval = true;
				}

			}else {
				retval = false;
			}


		} catch (Exception e) {
			Utils.logError("Exception during isWpaSupplicantRunning()",e);
			retval = false;
		}

		return retval;

	}

	protected boolean scanNetworks(){

		return executeRootCmd("bash " + Commons.getScriptScan());
		
	}

	protected boolean getScanResults(){

		return executeRootCmd("bash " + Commons.getScriptScanRes());
	
	}

	protected boolean createScanScripts(){

		try {

			String scan = getCmdWpaCli() + " scan\n" +
					"if [ $? -ne 0 ]; then\n" +
					"exit 1\n" +
					"fi\n" +
					"sleep 2s\n";

			String scanRes = "if [ -e \"" + Commons.getScanFile() + "\" ]; then\n" +
					"	rm \"" + Commons.getScanFile() + "\"\n" +
					"fi\n" + 
					getCmdWpaCli() + " scan_results > \""+ Commons.getScanFile() + "\"\n" +
					"if [ $? -ne 0 ]; then\n" +
					"	exit 1\n" +
					"fi\n" +
					"sleep 1s\n";

			
			//Try to create and chmod script scan
		/*	executeRootCmd("echo > " + Commons.getSCRIPT_SCAN());
			chmodFile(Commons.getSCRIPT_SCAN(), "666");*/
			
			
			if (! Utils.writeFile(Commons.getScriptScan(),scan,true) ){

				Exception e = Utils.getLastException();
				if (e != null){
					Utils.logError("Error while writing scan script.",e);
				}

				return false;
			}

			//Try to create and chmod script scanres
			/*executeRootCmd("echo > " + Commons.getSCRIPT_SCANRES());
			chmodFile(Commons.getSCRIPT_SCANRES(), "666");*/
			
			if (! Utils.writeFile(Commons.getScriptScanRes(),scanRes,true) ){

				Exception e = Utils.getLastException();
				if (e != null){
					Utils.logError("Error while writing getScanResults script.",e);
				}

				return false;
			}


			return true;

		} catch (Exception e) {

			Utils.logError("Error while creating the scanning script.",e);
			return false;
		}

	}
	
	/*protected boolean createDhcpcdScritp(){
		
		String scriptDhcp = "dhcpcd "+ Commons.INTERFACE_NAME + "\n" +
				"sleep 3s\n";

		if (! Utils.writeFile(Commons.getScriptDhcpcd(),scriptDhcp,true) ){

			Exception e = Utils.getLastException();
			if (e != null){
				Utils.logError("Error while writing dhcpcd script.",e);
			}

			return false;
		}
		
		return true;
		
	}*/
}
