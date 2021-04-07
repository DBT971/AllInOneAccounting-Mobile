package aioa.allinoneaccounting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Screen_Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_menu);
    }

    public void open_scan(View view){
        Intent scan_intent = new Intent(this, Screen_Scanner.class);
        startActivity(scan_intent);
    }

    public void open_account(View view){

    }

    public void open_preferences(View view){
        Intent preferences_intent = new Intent(this, Screen_Settings.class);
        startActivity(preferences_intent);
    }

}
