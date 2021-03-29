package aioa.allinoneaccounting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import java.util.List;

public class Screen_Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("mode", Context.MODE_PRIVATE);
        final String patternl = pref.getString("pattern","");
        final Intent intent = new Intent(this, Screen_Menu.class);

        if (patternl.equals("")){
            startActivity(intent);
            finish();
        }
        else{
            setContentView(R.layout.screen_login);

            final PatternLockView patternLockView = findViewById(R.id.patternView);
            patternLockView.addPatternLockListener(new PatternLockViewListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onProgress(List<PatternLockView.Dot> progressPattern) {

                }

                @Override
                public void onComplete(List<PatternLockView.Dot> pattern) {
                    if (PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase(patternl.toString())){
                        Toast.makeText(Screen_Login.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(Screen_Login.this, R.string.login_fail, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCleared() {

                }
            });
        }
    }
}
