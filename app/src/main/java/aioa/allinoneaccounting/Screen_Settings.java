package aioa.allinoneaccounting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class Screen_Settings extends AppCompatActivity {

    private static SharedPreferences.Editor prefEd;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);

        /**
         * Gets the light mode preference.
         */
        prefEd = getSharedPreferences("light_mode", MODE_PRIVATE).edit();

        /**
         * Loads up the preferences fragment for this page.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        /**
         * If the Home button is pressed, go back to the previous screen.
         */
        if(item.getItemId() == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
            /**
             * When the fragment is created, load up the preferences for the settings menu
             */
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference pattern = findPreference("pattern");
            SwitchPreferenceCompat lightMode = findPreference("light_mode");
            lightMode.setOnPreferenceChangeListener((preference, newValue) -> {
                if(preference.getKey().equals("light_mode")){
                    lightMode.setChecked((Boolean) newValue);
                    if(lightMode.isChecked()){ updateTheme(getString(R.string.settings_theme_summary_enabled));}
                    else{ updateTheme(getString(R.string.settings_theme_summary_disabled));}
                    Toast.makeText(SettingsFragment.this.getContext(), R.string.settings_info_restart_required, Toast.LENGTH_LONG).show();
                }
                return true;
            });
            pattern.setOnPreferenceClickListener(preference -> {
                if (preference.getKey().equals("pattern")){
                    updatePattern();
                }
                return true;
            });
        }

        /**
         * If the Light Mode setting is changed, update light mode
         * @param summary A string value of whether Light Mode is Enabled or Disabled.
         */
        private void updateTheme(String summary){
            prefEd.putString("light_mode",summary);
            prefEd.commit();
        }

        private void updatePattern() {
            Intent pattern_intent = new Intent(this.getContext(), Screen_Pattern.class);
            startActivity(pattern_intent);
        }
    }
}
