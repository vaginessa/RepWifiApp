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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import fil.libre.repwifiapp.Commons;
import android.util.Log;

public class Utils {

	private static final long MILLIS_IN_DAY = 86400000;
	
	public static final String APP_NAME = "RepWifi";
		
	private static Exception _lastException = null;
	
	public static Exception getLastException(){
		return _lastException;
	}
	
	public static void logError(String msg, Exception e){
		Log.e(APP_NAME,msg,e);
	}
	
	public static void logError(String msg){
		Log.e(APP_NAME,msg);
	}
	
	public static void logDebug(String msg){
		logDebug(msg,0);
	}
	
	public static void logDebug(String msg, int level){
		
		if (level < Commons.getLogPriority()){
			return;
		}
		
		Log.d(APP_NAME,msg);
	}
	

	public static boolean writeFile(String filePath, String text, boolean overwrite){
					
		FileWriter writer = null;
		boolean retval = false;
		
		try {
			
			writer = new FileWriter(filePath, (! overwrite));
			writer.write(text);
					
			retval = true;
			
		} catch (Exception e) {
			_lastException = e;
			retval = false;
		}
		finally{
			
			if (writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					logError("error while closing filewriter",e);
				}
			}		
					
		}
		
		return retval;
		
	}
	
	public static boolean writeFileLines(String filePath, String[] lines, boolean overwrite){
		
		if (lines == null){
			return false;
		}
		
		FileWriter writer = null;
		boolean retval = false;
		
		try {
			
			writer = new FileWriter(filePath, (! overwrite));
			
			if (lines.length == 0){
				writer.write("");
			}
			
			for(String l : lines){
				writer.write(l + "\n");
			}
					
			retval = true;
			
		} catch (Exception e) {
			_lastException = e;
			retval = false;
		}
		finally{
			
			if (writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					logError("error while closing filewriter",e);
				}
			}		
					
		}
		
		return retval;
		
	}

	public static String[] readFileLines(String filePath){
		
		if (filePath == null){
			return null;
		}
		
		File f = new File(filePath);
		if (! f.exists()){
			logError("File doesn't exist: " + filePath);
			return null;
		}
		
		FileReader fr = null;
        BufferedReader bufr = null;
		
        List<String> lines = new ArrayList<String>();
        String[] ret = null;
        
		try {
			
			fr = new FileReader(filePath);
			bufr = new BufferedReader(fr);
	        String line ="";
	        
	        while((line = bufr.readLine()) != null){
	            lines.add(line);
	        }
	        
	        String[] ar = new String[lines.size()];
	        ret = lines.toArray(ar);
	        
		} catch (Exception e) {
			logError("Error while reading file " + filePath,e);
			ret = null;
		}
		finally{
			try {
				if (bufr != null){
					bufr.close();
				}
			} catch (IOException ex) {
				logError("error while closing filereader",ex);
			}
			try {
				if (fr != null){
					fr.close();
				}
			}catch(IOException exc){
				logError("error while closing filereader",exc);
			}
		}
        
		return ret;
		
	}

	public static long daysToMilliseconds(int days){		
		return (days * MILLIS_IN_DAY);		
	}
	
	public static long millisecondsToDays(long milliseconds){
		return (milliseconds / MILLIS_IN_DAY);
	}
	
}
