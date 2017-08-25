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

package fil.libre.repwifiapp.activities;

import fil.libre.repwifiapp.ActivityLauncher;
import fil.libre.repwifiapp.ActivityLauncher.RequestCode;
import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class InputSsidActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_input_ssid);
        setTitle("Insert Network's parameters");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // disable menu
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (intent == null) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {

        case RequestCode.SELECT_CONN:

            if (intent.hasExtra(ActivityLauncher.EXTRA_APINFO)) {
                Bundle xtras = intent.getExtras();
                AccessPointInfo i = (AccessPointInfo) xtras
                                .getSerializable(ActivityLauncher.EXTRA_APINFO);
                returnAccessPointInfo(i);
            }

            break;

        default:
            break;
        }

    }

    public void onBtnNextClick(View v) {

        EditText txssid = (EditText) findViewById(R.id.txt_ssid);
        String ssid = txssid.getText().toString();

        if (ssid.length() == 0) {
            Commons.showMessage("Network name can't be empty!", this);
            return;
        }

        AccessPointInfo apinfo = new AccessPointInfo(ssid, Commons.BSSID_NOT_AVAILABLE, "WPA2",
                        null, null);

        returnAccessPointInfo(apinfo);

    }

    public void onBtnSelectClick(View v) {
        ActivityLauncher launcher = new ActivityLauncher(this);
        launcher.launchSelectActivity(null, true, true);
    }

    private void returnAccessPointInfo(AccessPointInfo apinfo) {

        apinfo.setHidden(true);

        Intent intent = new Intent();
        intent.putExtra(ActivityLauncher.EXTRA_APINFO, apinfo);
        setResult(RESULT_OK, intent);

        finish();
    }

}