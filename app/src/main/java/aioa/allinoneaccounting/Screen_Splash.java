package aioa.allinoneaccounting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class Screen_Splash extends AppCompatActivity {
    private final static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        /**
         * Checks what the current theme is set to, and then changes the theme to match
         */
        SharedPreferences pref = getSharedPreferences("light_mode", MODE_PRIVATE);
        final String lmSummary = pref.getString("light_mode", "");
        if (lmSummary.equals(getApplicationContext().getString(R.string.settings_theme_summary_disabled))){
            getApplicationContext().setTheme(R.style.darkTheme);
            getApplicationInfo().theme = R.style.darkTheme;
        }
        else if (lmSummary.equals(getApplicationContext().getString(R.string.settings_theme_summary_enabled))){
            getApplicationContext().setTheme(R.style.lightTheme);
            getApplicationInfo().theme = R.style.lightTheme;
        }
        setTheme(getApplicationInfo().theme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_splash);

        new Handler().postDelayed(() -> {
            Intent splashIntent = new Intent(Screen_Splash.this, Screen_Login.class);
            startActivity(splashIntent);
            finish();
        },SPLASH_TIME_OUT);
    }
}
