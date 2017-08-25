package fil.libre.repwifiapp.helpers;

import java.io.IOException;
import java.io.InputStream;

public class ShellCommand {

    protected String _cmdOut = "";
    protected String _cmdTxt = "";

    public ShellCommand(String commandText) {
        this._cmdTxt = commandText;
    }

    public int execute() throws Exception {

        if (this._cmdTxt == null) {
            return -9;
        }

        Utils.logDebug("EXEC: " + this._cmdTxt);

        Process cmd = Runtime.getRuntime().exec(this._cmdTxt);

        InputStream os = cmd.getInputStream();
        InputStream es = cmd.getErrorStream();

        StringBuilder sb = new StringBuilder();

        sb.append(getStringFromStream(es));
        sb.append(getStringFromStream(os));

        int res = cmd.waitFor();

        // re-read the output, in case it was empty when first tried
        sb.append(getStringFromStream(es));
        sb.append(getStringFromStream(os));

        this._cmdOut = sb.toString();

        Utils.logDebug("EXITCODE: " + res);
        Utils.logDebug("OUT: " + getOutput());

        return res;

    }

    protected String getStringFromStream(InputStream s) throws IOException {

        StringBuilder sb = new StringBuilder();
        while ((s.available() > 0)) {
            int b = s.read();
            if (b >= 0) {
                sb.append((char) b);
            } else {
                break;
            }
        }

        return sb.toString();

    }

    public String getOutput() {

        return this._cmdOut;

        /*
         * String[] lastOut = Utils.readFileLines(Commons.getTempOutFile()); if
         * (lastOut == null){ return this._cmdOut; }
         * 
         * String fout = "";
         * 
         * for (String s : lastOut){ fout += s + "\n"; }
         * 
         * return fout;
         */

    }

}
