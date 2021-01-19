package aioa.allinoneaccounting;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class Screen_Preferences extends AppCompatActivity {

    private static SharedPreferences.Editor prefEd;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);

        prefEd = getSharedPreferences(name:"light_mode", MODE_PRIVATE).edit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
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
        if(item.getItemId() == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
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
        }
    }

    private void updateTheme(String summary){
        prefEd.putString("light_mode",summary);
        prefEd.commit();
    }
}
