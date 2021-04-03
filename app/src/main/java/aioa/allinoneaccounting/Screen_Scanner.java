package aioa.allinoneaccounting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;

public class Screen_Scanner extends AppCompatActivity {

    private ImageView image_preview;
    private TextView image_text;
    private GraphicOverlay graphic_overlay;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_scanner);

        image_preview = findViewById(R.id.image_view);
        image_text = findViewById(R.id.text_image);
        graphic_overlay = findViewById(R.id.graphic_overlay);

        int camera_permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (camera_permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101 );
        }
    }

    public void launch_camera(View view){

        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle_image = data.getExtras();
        Bitmap bit_image = (Bitmap) bundle_image.get("data");
        image_preview.setImageBitmap(bit_image);


        InputImage image = InputImage.fromBitmap(bit_image, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        image_text.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            image_text.setEnabled(true);
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            image_text.setEnabled(true);
                            e.printStackTrace();
                        });

    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(Screen_Scanner.this, R.string.scan_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        graphic_overlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    TextGraphic textGraphic = new TextGraphic(graphic_overlay, elements.get(k));
                    graphic_overlay.add(textGraphic);

                }
            }
        }
    }

}
