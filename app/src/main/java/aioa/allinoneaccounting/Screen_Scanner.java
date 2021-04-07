package aioa.allinoneaccounting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.Date;
import java.util.List;

public class Screen_Scanner extends AppCompatActivity {

    private ImageView image_preview;
    private GraphicOverlay graphic_overlay;
    private Button capture_button;
    private Button detect_button;
    private Bitmap bit_image;
    private Integer image_max_height;
    private Integer image_max_length;
    public static String cam_direct = "";
    private File photo_file;
    private Uri photo_uri;
    private int rotation;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_scanner);

        get_permissions();
        load_views();

        cam_direct = getExternalFilesDir(null).getAbsolutePath() + "/aioa_Images";

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void get_permissions(){
        int camera_permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (camera_permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101 );
        }

        int save_permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(save_permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102 );
        }
    }

    public void load_views(){
        image_preview = findViewById(R.id.image_view);
        graphic_overlay = findViewById(R.id.graphic_overlay);
        capture_button = findViewById(R.id.capture_image);
        detect_button = findViewById(R.id.detect_image);

        capture_button.setOnClickListener(v -> launch_camera(v));
        detect_button.setOnClickListener(v -> text_recognition());
    }

    public void launch_camera(View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(camera_intent.resolveActivity(getPackageManager()) != null){
            try{
                photo_file = create_image_file();
            } catch (IOException e){
                e.printStackTrace();
            }
            if(photo_file != null){
                photo_uri = FileProvider.getUriForFile(this,
                        "aioa.Scanner.fileprovider",
                        photo_file);

                camera_intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
                startActivityForResult(camera_intent, 101);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK){
            bit_image = BitmapFactory.decodeFile(photo_file.getAbsolutePath());
            rotation = get_camera_orientation(this, photo_uri, photo_file.getAbsolutePath());
            if(rotation == -1){
                Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
            }

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

            image_preview.setRotation(rotation);
            image_preview.setImageBitmap(resized_bitmap);

        }
        else{
            Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public int get_camera_orientation(Context context, Uri imageUri, String imagePath){
         try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                default:
                    return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         return -1;
    }

    private File create_image_file() throws IOException {

        File storage_dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String image_name = "JPG_" + time_stamp +"_";

        File image =File.createTempFile(
          image_name,
          ".jpg",
          storage_dir
        );
        cam_direct = image.getAbsolutePath();
        return image;
    }

    public void text_recognition(){
        InputImage image = InputImage.fromBitmap(bit_image, rotation);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
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
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new android.util.Pair<>(targetWidth, targetHeight);
    }


}
