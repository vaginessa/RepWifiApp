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

import org.apache.http.conn.util.InetAddressUtils;
import fil.libre.repwifiapp.Commons;

public class Engine6p0 extends Engine {

    @Override
    protected String getCmdWpaStart() {
        return "wpa_supplicant -B -dd -i" + Commons.INTERFACE_NAME + " -C" + Commons.SOCKET_DIR
                        + " -P" + Commons.PID_FILE + " -I" + Commons.OVERLAY_FILE + " -e"
                        + Commons.ENTROPY_FILE;
    }

    @Override
    public boolean connect(AccessPointInfo info) {

        killBackEndProcesses();

        if (info == null) {
            Utils.logDebug("Engine's connect() received a null AccessPointInfo");
            return false;
        }

        // clear any previously set network
        if (!destroyNetwork()) {
            Utils.logDebug("Unable to ndc destroy network");
            return false;
        }

        // clear interface's ip
        if (!clearAddrs()) {
            Utils.logDebug("Unable to ndc clearaddrs");
            return false;
        }

        // bring up interface
        if (!interfaceUp()) {
            Utils.logDebug("Unable to bring up interface.");
            return false;
        }

        // launch wpa_supplicant specifying our custom configuration and the
        // socket file
        if (!executeRootCmd(getCmdWpaStart())) {
            Utils.logDebug("Unable to run wpa start");
            return false;
        }

        // create new network and get network id
        String netID = createNetworkGetId();
        if (netID == null) {
            Utils.logDebug("Unable to fetch network id");
            return false;
        }

        // set network SSID
        if (!setNetworkSSID(info.getSsid(), netID)) {
            Utils.logDebug("Failed to set network ssid");
            return false;
        }

        if (info.isHidden() && !setNetworkScanSSID(netID)) {
            Utils.logDebug("Failed to set scan_ssid 1 for hidden network.");
            return false;
        }

        // set password (if any)
        if (!setNetworkPSK(info, netID)) {
            Utils.logDebug("Failed to set network psk");
            return false;
        }

        // select the network we just created
        if (!selectNetwork(netID)) {
            Utils.logDebug("Unable to wpa_cli select network");
            return false;
        }

        // enable the newtork
        if (!enableNetwork(netID)) {
            Utils.logDebug("Unable to wpa_cli enable_newtork");
            return false;
        }

        // try to reassociate to Access Point
        /*
         * if (! reassociate()){
         * Utils.logDebug("Unable to wpa_cli reassociate"); return false; }
         */

        // get DHCP
        Utils.logDebug("Attempt to run dhcpcd..");
        if (!runDhcpcd()) {
            Utils.logDebug("Failed to run dhcpcd");
            return false;
        }

        // try to fetch gateway
        String gw = getGateway();
        if (gw == null || gw.trim().length() < 7) {
            // failed to get gateway
            Utils.logDebug("Failed to get gateway");
            return false;
        }

        if (!executeRootCmd("ndc network create 1")) {
            Utils.logDebug("Failed to wpa_cli network create 1 ");
            return false;
        }

        if (!executeRootCmd("ndc network interface add 1 " + Commons.INTERFACE_NAME)) {
            Utils.logDebug("Failed to add interface.");
            return false;
        }

        // set route to gateway for all traffic
        if (!executeRootCmd("ndc network route add 1 " + Commons.INTERFACE_NAME + " 0.0.0.0/0 "
                        + gw)) {
            Utils.logDebug("Failed to add route to gateway");
            return false;
        }

        if (!setDns(Commons.getDnss(), gw)) {
            Utils.logDebug("Failed to set DNS");
            return false;
        }

        // use network
        if (!executeRootCmd("ndc network default set 1")) {
            Utils.logDebug("Failed to set network as default");
            return false;
        }

        return true;

    }

    private String createNetworkGetId() {

        try {

            RootCommand su = new RootCommand(getCmdWpaCli() + " add_network");
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out == null || out.trim().equals("")) {
                    return null;
                } else {
                    return out.replace("\n", "");
                }
            } else {
                return null;
            }

        } catch (Exception e) {
            Utils.logError("Error while creating network", e);
            return null;
        }

    }

    private boolean destroyNetwork() {
        // needs root (tested)
        return executeRootCmd("ndc network destroy 1");
    }

    private boolean setNetworkSSID(String ssid, String networkID) {

        try {

            // needs root (wpa_cli)
            RootCommand su = new RootCommand(getCmdWpaCli() + " set_network " + networkID
                            + " ssid '\"" + ssid + "\"'");
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out != null && out.trim().replace("\n", "").equals("OK")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            Utils.logError("Error while setting network SSID", e);
            return false;
        }

    }

    private boolean setNetworkPSK(AccessPointInfo info, String networkID) {

        try {

            // needs root (wpa_cli)

            String cmdSetPass = null;
            if (info.needsPassword()) {
                cmdSetPass = getCmdWpaCli() + " set_network " + networkID + " psk '\""
                                + info.getPassword() + "\"'";
            } else {
                cmdSetPass = getCmdWpaCli() + " set_network " + networkID + " key_mgmt NONE";
            }

            RootCommand su = new RootCommand(cmdSetPass);
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out != null && out.trim().replace("\n", "").equals("OK")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            Utils.logError("Error while setting network PSK", e);
            return false;
        }

    }

    private boolean setNetworkScanSSID(String networkID) {

        try {

            // needs root (wpa_cli)
            RootCommand su = new RootCommand(getCmdWpaCli() + " set_network " + networkID
                            + " scan_ssid 1");
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out != null && out.trim().replace("\n", "").equals("OK")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            Utils.logError("Error while setting network SSID", e);
            return false;
        }
    }

    private boolean selectNetwork(String networkID) {

        try {

            // needs root (wpa_cli)
            RootCommand su = new RootCommand(getCmdWpaCli() + " select_network " + networkID);
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out != null && out.trim().replace("\n", "").equals("OK")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            Utils.logError("Error while selecting network", e);
            return false;
        }

    }

    private boolean enableNetwork(String networkID) {

        try {

            // needs root (wpa_cli)

            RootCommand su = new RootCommand(getCmdWpaCli() + " enable_network " + networkID);
            if (su.execute() == 0) {
                String out = su.getOutput();
                if (out != null && out.trim().replace("\n", "").equals("OK")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            Utils.logError("Error while enabling network", e);
            return false;
        }

    }

    private boolean setDns(String[] dnss, String gateway) {

        if (dnss == null || dnss.length == 0) {
            // the DNS setting has been left blank
            // try to use the gateway as dns

            if (gateway == null || gateway.length() == 0) {
                // no possible DNS.
                return false;
            }

            dnss = new String[] { gateway, gateway };

        }

        if (!InetAddressUtils.isIPv4Address(dnss[0])) {
            // invalid ip can't proceed.
            return false;
        }

        String cmd = "ndc resolver setnetdns 1 " + dnss[0];

        if (dnss.length > 1 && InetAddressUtils.isIPv4Address(dnss[1])) {
            cmd += " " + dnss[1];
        } else {
            cmd += " " + dnss[0];
        }

        return executeRootCmd(cmd);
    }

    private String getGateway() {

        try {

            // doesn't need root (tested)
            RootCommand cmd = new RootCommand("ip route show dev " + Commons.INTERFACE_NAME);
            if (cmd.execute() != 0) {
                Utils.logDebug("command failed show route");
                return null;
            }

            // read command output
            String out = cmd.getOutput();
            if (out == null) {
                return null;
            }

            String[] lines = out.split("\n");

            for (String l : lines) {

                if (l.contains("default via")) {

                    String[] f = l.split(" ");
                    if (f.length > 2) {

                        // found route's address:
                        return f[2];

                    }
                }
            }

            return null;

        } catch (Exception e) {
            Utils.logError("Error while trying to fetch route", e);
            return null;
        }

    }

    private boolean clearAddrs() {
        // needs root (tested)
        return executeRootCmd("ndc interface clearaddrs " + Commons.INTERFACE_NAME);
    }

}