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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import fil.libre.repwifiapp.Commons;

public class RootCommand {

	private String _cmdOut = "";
	private String _cmdTxt = "";

	public RootCommand(String commandText){
		this._cmdTxt = commandText;
	}
	
	public int execute() throws Exception{
		return execute(0);
	}
	
	public int execute(int sleepSecsAfterCmd) throws Exception{

		Process su = Runtime.getRuntime().exec("su");

		DataOutputStream stdin = new DataOutputStream(su.getOutputStream());
		InputStream os = su.getInputStream();
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(os));
		InputStream es = su.getErrorStream();
		BufferedReader stdError = new BufferedReader(new InputStreamReader(es));

		if ( this._cmdTxt != null ){
			
			Utils.logDebug("EXEC: " + this._cmdTxt);
						
			this._cmdTxt += " > " + Commons.getTempOutFile();
			
			stdin.writeBytes(this._cmdTxt + "\n");
			stdin.flush();
		}

	/*	if (sleepSecsAfterCmd > 0){
			Thread.sleep(sleepSecsAfterCmd * 1000);
		}*/
		
		StringBuilder sb = new StringBuilder();
		String s = null;

		while ( (es.available() > 0) && (s = stdError.readLine()) != null) {
			sb.append(s + "\n");
		}	

		while ( (os.available() > 0) && (s = stdOut.readLine()) != null) {
			sb.append(s + "\n");
		}

		this._cmdOut = sb.toString();
		
		stdin.writeBytes("exit\n");
		stdin.flush();
		
		int res = su.waitFor();
		
		Utils.logDebug("OUT: " + getOutput());
		
		return res;
		
	}

	public String getOutput(){
		
		String[] lastOut = Utils.readFileLines(Commons.getTempOutFile());
		if (lastOut == null){
			return this._cmdOut;
		}
		
		String fout = "";
		
		for (String s : lastOut){
			fout += s + "\n";
		}
		
		return fout;
		
	}

	
	
}
