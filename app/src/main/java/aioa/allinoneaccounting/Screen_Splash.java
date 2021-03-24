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

        /**
         * Checks whether this is the first time the application is loaded, and if it is loads up a
         * Welcome Screen
         */
        new Handler().postDelayed(() -> {
            if (check_pref(this, "first")){
                launch_welcome();
            }
            else {
                Intent splashIntent = new Intent(Screen_Splash.this, Screen_Menu.class);
                startActivity(splashIntent);
            }
            finish();
        },SPLASH_TIME_OUT);
    }

    /**
     * Checks if this is the first time this program is ran.
     * @param context The current application environment.
     * @param prefKey The preference key that is to be checked.
     * @return
     */
    public static boolean check_pref(Context context, String prefKey){
        return context.getSharedPreferences("prefs",MODE_PRIVATE).getBoolean(prefKey, true);
    }

    /**
     *  Code that launches the Welcome page if this is the first time the application is ran
     */
    public void launch_welcome() {
        Intent welcomeIntent = new Intent(Screen_Splash.this, Screen_Welcome.class);
        startActivity(welcomeIntent);
        change_pref(this, "first");
        finish();
    }

    /**
     * After showing the welcome screen, this will change the "First" preference to False, and make it
     * so that the user won't see the welcome screen again.
     * @param context The current application environment.
     * @param key The preference key that is to be checked.
     */
    public static void change_pref(Context context,String key){
        SharedPreferences.Editor editor = context.getSharedPreferences("prefs",MODE_PRIVATE).edit();
        editor.putBoolean(key,false);
        editor.apply();
    }}
