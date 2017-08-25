package fil.libre.repwifiapp.activities;

import org.apache.http.conn.util.InetAddressUtils;
import java.util.List;
import fil.libre.repwifiapp.Commons;
import fil.libre.repwifiapp.R;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_SettingsTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
    }

    public static class DebugSettingFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.debug_settings);

            setConfirmKillBackend();

        }

        private void setConfirmKillBackend() {

            Preference pref = getPreferenceScreen().findPreference("pref_kill_backend");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference p) {
                    Commons.killBackEnd(getActivity(), false);
                    return true;
                }
            });

        }

    }

    public static class GeneralSettingFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.general_settings);

            setValidationListener("dns1");
            setValidationListener("dns2");
            setConfirmRestore();

        }

        private void setValidationListener(String prefName) {
            EditTextPreference edit_Pref = (EditTextPreference) getPreferenceScreen()
                            .findPreference(prefName);

            edit_Pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // put validation here..
                    if (((String) newValue).isEmpty()
                                    || InetAddressUtils.isIPv4Address((String) newValue)) {
                        return true;
                    } else {
                        Commons.showMessage("ERROR:\nWrong IP format!", getActivity());
                        return false;
                    }
                }
            });
        }

        private void setConfirmRestore() {

            Preference pref = getPreferenceScreen().findPreference("pref_restore_default");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference p) {
                    Commons.resetSettingsDefault(getActivity(), false);
                    return true;
                }
            });

        }
    }

}
