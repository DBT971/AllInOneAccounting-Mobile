package aioa.allinoneaccounting;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import static android.Manifest.permission.CAMERA;

public class Screen_Scan extends AppCompatActivity {

    private TextView text;
    private SurfaceView preview;
    private CameraSource camera;
    private TextRecognizer detect;
    private String captureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);
        setContentView(R.layout.screen_scan);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void textRecognizer(){

        detect = new TextRecognizer.Builder(getApplicationContext()).build();
        camera = new CameraSource.Builder(getApplicationContext(), detect)
                .setRequestedPreviewSize(1280,1024).build();

        preview = findViewById(R.id.preview);

        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                try{
                    camera.start(preview.getHolder());
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera.stop();
            }
        });

        detect.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();

                StringBuilder stringBuilder = new StringBuilder();

                for(int i = 0; i<sparseArray.size(); i++){
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null && textBlock.getValue() != null){
                        stringBuilder.append(textBlock.getValue() + " ");
                    }
                }


                String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        captureResult = stringText;
                        resultObtained();
                    }
                });
            }
        });
    }

    private void resultObtained(){
        setContentView(R.layout.screen_scan);
        text = findViewById(R.id.detect);
        text.setText(captureResult);
    }

    public void buttonStart(View view){
        setContentView(R.layout.surface_scan);
        textRecognizer();

    }
}
