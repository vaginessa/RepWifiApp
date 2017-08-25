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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import fil.libre.repwifiapp.Commons;

public class AccessPointInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_SSID_LENGTH = 32;

    private String _ssid;
    private String _bssid;
    private String _auth;
    private String _level;
    private String _freq;
    private String _password;
    private boolean _isHidden = false;
    private long _lastTimeUsed;

    public AccessPointInfo(String ssid, String bssid, String authType, String level, String freq) {

        this._ssid = ssid;
        this._bssid = bssid;
        this._auth = authType;
        this._level = level;
        this._freq = freq;

    }

    public String getSsid() {
        return this._ssid;
    }

    public String getSsid(int maxLength) {
        String txt = getSsid();
        if (maxLength > 4 && txt.length() > maxLength) {
            txt = txt.substring(0, maxLength - 3) + "...";
        }
        return txt;
    }

    public void setHidden(boolean hidden) {
        this._isHidden = hidden;
    }

    public boolean isHidden() {
        return this._isHidden;
    }

    public String getBssid() {
        return this._bssid;
    }

    public void setBssid(String bssid) {
        this._bssid = bssid;
    }

    public String getAuthType() {
        return this._auth;
    }

    public String getSignlalStrength() {
        return this._level;
    }

    public String getFrequency() {
        return this._freq;
    }

    public long getLastTimeUsed() {
        return this._lastTimeUsed;
    }

    public void setLastTimeUsed(long timeStampInMillis) {
        this._lastTimeUsed = timeStampInMillis;
    }

    public boolean isOlderThan(int days) {

        if (this._lastTimeUsed == 0) {
            return false;
        }

        long timeDiff = System.currentTimeMillis() - this._lastTimeUsed;
        long spanMillis = Utils.daysToMilliseconds(days);

        if (timeDiff > spanMillis) {
            return true;
        } else {
            return false;
        }

    }

    public String getPassword() {
        return this._password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public boolean needsPassword() {

        if ((this._auth == null) || (this._auth.equals(""))) {
            // TODO
            // check if default behavior should be with or without password,
            // when no auth info is available.
            return false;
        }

        if (this._auth.contains("WPA2") || this._auth.contains("WPA")) {
            return true;
        } else {
            return false;
        }

    }

    protected static AccessPointInfo parseLine(String line) {

        try {

            String[] params = line.split("\t");
            if (params.length != 5) {
                return null;
            }

            String bssid = params[0];
            String freq = params[1];
            String level = params[2];
            String auth = params[3];
            String ssid = params[4];

            if (ssid.length() == 0 || ssid.length() > MAX_SSID_LENGTH) {
                // invalid SSID.
                return null;
            }

            AccessPointInfo info = new AccessPointInfo(ssid, bssid, auth, level, freq);
            return info;

        } catch (Exception e) {
            Utils.logError("Error while parsing line: " + line, e);
            return null;
        }

    }

    public static AccessPointInfo[] parseScanResult(String scanResultFile) {

        try {

            Utils.logDebug("AccesPointInfo trying to parse file: " + scanResultFile);

            File f = new File(scanResultFile);
            if (!f.exists()) {
                Utils.logError("AccessPointInfo.parseScanResult(): The provided scan result file doesn't exist");
                return null;
            }

            String[] lines = Utils.readFileLines(scanResultFile);
            List<AccessPointInfo> nets = new ArrayList<AccessPointInfo>();

            if (lines == null) {
                return null;
            }

            for (String l : lines) {
                if (l.startsWith(Commons.SCAN_FILE_HDR)) {
                    // strip off the header
                    continue;
                }

                if (l.trim().equals("")) {
                    // empty line, skip.
                    continue;
                }

                // try to parse line into network info
                AccessPointInfo info = AccessPointInfo.parseLine(l);
                if (info == null) {
                    Utils.logError("Failed to parse line into AccessPointInfo: " + l);
                    continue;
                }

                nets.add(info);

            }

            AccessPointInfo[] a = new AccessPointInfo[nets.size()];
            a = nets.toArray(a);
            return a;

        } catch (Exception e) {
            Utils.logError("Error while parsing scan results in class AccessPointInfo", e);
            return null;
        }

    }

}
