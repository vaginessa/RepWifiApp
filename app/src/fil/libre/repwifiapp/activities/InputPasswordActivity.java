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
import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import fil.libre.repwifiapp.helpers.AccessPointInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class InputPasswordActivity extends Activity implements OnCheckedChangeListener {

    AccessPointInfo apinfo = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_input_password);

        CheckBox c = (CheckBox) findViewById(R.id.chk_show_pass);
        c.setOnCheckedChangeListener(this);

        setTitle("Input password");

        TextView v = (TextView) findViewById(R.id.txt_insert_pass);

        // get the network to set password to:
        this.apinfo = (AccessPointInfo) getIntent().getSerializableExtra(
                        ActivityLauncher.EXTRA_APINFO);
        v.append(" " + apinfo.getSsid());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // disable menu
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView == findViewById(R.id.chk_show_pass)) {
            chkShowPassChanged();
        }

    }

    public void onBtnNextClick(View v) {

        EditText txpass = (EditText) findViewById(R.id.txt_password);
        String pass = txpass.getText().toString();

        if (pass.length() == 0) {
            Commons.showMessage("Password can't be empty!", this);
        }

        this.apinfo.setPassword(pass);

        Intent intent = new Intent();
        intent.putExtra(ActivityLauncher.EXTRA_APINFO, this.apinfo);
        setResult(RESULT_OK, intent);

        finish();

    }

    public void chkShowPassChanged() {

        CheckBox c = (CheckBox) findViewById(R.id.chk_show_pass);
        EditText txtPass = (EditText) findViewById(R.id.txt_password);

        if (c.isChecked()) {
            txtPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txtPass.setSelection(txtPass.getText().length());

        } else {
            txtPass.setInputType(129);
            txtPass.setSelection(txtPass.getText().length());

        }

    }

}
