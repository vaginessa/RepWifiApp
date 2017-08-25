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

import java.io.DataOutputStream;
import java.io.InputStream;
import fil.libre.repwifiapp.Commons;

public class RootCommand extends ShellCommand {

    public RootCommand(String commandText) {
        super(commandText);
        this._cmdTxt = commandText;
    }

    @Override
    public int execute() throws Exception {

        Process su = Runtime.getRuntime().exec("su");

        DataOutputStream stdin = new DataOutputStream(su.getOutputStream());
        InputStream os = su.getInputStream();
        InputStream es = su.getErrorStream();

        if (this._cmdTxt != null) {

            Utils.logDebug("SU:EXEC: " + this._cmdTxt);

            this._cmdTxt += " > " + Commons.getTempOutFile();

            stdin.writeBytes(this._cmdTxt + "\n");
            stdin.flush();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(getStringFromStream(es));
        sb.append(getStringFromStream(os));

        this._cmdOut = sb.toString();

        stdin.writeBytes("exit\n");
        stdin.flush();

        int res = su.waitFor();

        // re-read the output, in case it was empty when first tried
        sb.append(getStringFromStream(es));
        sb.append(getStringFromStream(os));

        Utils.logDebug("OUT: " + getOutput());

        return res;

    }

    @Override
    public String getOutput() {

        String[] lastOut = Utils.readFileLines(Commons.getTempOutFile());
        if (lastOut == null) {
            return this._cmdOut;
        }

        String fout = "";

        for (String s : lastOut) {
            fout += s + "\n";
        }

        return fout;

    }

}
