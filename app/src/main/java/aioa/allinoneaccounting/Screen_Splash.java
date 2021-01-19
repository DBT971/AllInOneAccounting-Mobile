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
            if (checkPref(this, "first")){
                launchWelcome();
            }
            else {
                Intent splashIntent = new Intent(Screen_Splash.this, MainActivity.class);
                startActivity(splashIntent);
            }
            finish();
        },SPLASH_TIME_OUT);
    }

    public void launchWelcome() {
        Intent welcomeIntent = new Intent(Screen_Splash.this, Screen_Welcome.class);
        startActivity(welcomeIntent);
        changePrefs(this, "first");
        finish();
    }

    public static boolean checkPref(Context context, String prefKey){
        return context.getSharedPreferences("prefs",MODE_PRIVATE).getBoolean(prefKey, true);
    }


    public static void changePrefs(Context context,String key){
        SharedPreferences.Editor editor = context.getSharedPreferences("prefs",MODE_PRIVATE).edit();
        editor.putBoolean(key,false);
        editor.apply();
    }}
