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

import fil.libre.repwifiapp.R;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class CreditsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        String content = getResources().getString(R.string.credit_text);
        TextView tv = (TextView) findViewById(R.id.txt_credits);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(Html.fromHtml(content));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_credits, menu);
        return true;
    }

}
