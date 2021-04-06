package aioa.allinoneaccounting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Pair;

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
    private Button capture_button;
    private Button detect_button;
    private Bundle bundle_image;
    private Bitmap bit_image;
    private Integer image_max_height;
    private Integer image_max_length;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_scanner);

        int camera_permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (camera_permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101 );
        }

        image_preview = findViewById(R.id.image_view);
        image_text = findViewById(R.id.text);
        graphic_overlay = findViewById(R.id.graphic_overlay);
        capture_button = findViewById(R.id.capture_image);
        detect_button = findViewById(R.id.detect_image);

        capture_button.setOnClickListener(v -> launch_camera(v));
        detect_button.setOnClickListener(v -> text_recognition());
    }

    public void launch_camera(View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        bundle_image = data.getExtras();
        bit_image = (Bitmap) bundle_image.get("data");

        Pair<Integer, Integer> target_size = getTargetedWidthHeight();

        int target_width = target_size.first;
        int max_height = target_size.second;

        float scaleFactor =
                Math.max((float) bit_image.getWidth() / (float) target_width,
                         (float) bit_image.getHeight() / (float) max_height);

        Bitmap resized_bitmap = Bitmap.createScaledBitmap(
                bit_image,
                (int) (bit_image.getWidth() / scaleFactor),
                (int) (bit_image.getHeight() / scaleFactor), true);

        image_preview.setImageBitmap(resized_bitmap);
        bit_image = resized_bitmap;

    }

    public void text_recognition(){
        InputImage image = InputImage.fromBitmap(bit_image, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
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
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphic_overlay, elements.get(k));
                    graphic_overlay.add(textGraphic);

                }
            }
        }
    }

    private Integer getImageMaxWidth() {
        if (image_max_length == null) {
            image_max_length = image_preview.getWidth();
        }

        return image_max_length;
    }

    private Integer getImageMaxHeight() {
        if (image_max_height == null) {
            image_max_height = image_preview.getHeight();
        }

        return image_max_height;
    }

    // Gets the targeted width / height.
    private android.util.Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new android.util.Pair<>(targetWidth, targetHeight);
    }
}
