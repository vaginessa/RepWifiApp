package fil.libre.repwifiapp.activities;

import fil.libre.repwifiapp.R;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MenuEnabledActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();
        switch (id) {
        case R.id.menu_credits:
            launchCreditsActivity();
            break;

        case R.id.menu_config:
            launchSettingsActivity();
            break;

        default:
            break;
        }

        return true;
    }

    private void launchCreditsActivity() {
        Intent intent = new Intent(this, CreditsActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void launchSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
