package aioa.allinoneaccounting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

public class Screen_Scanner extends AppCompatActivity {

    private ImageView imagePreview;
    private Button captureButton;
    private Button detectButton;
    private Button sendButton;
    private Button settingsButton;
    private Bitmap bitImage;
    private boolean scanned = false;
    private static String camDirect = "";
    private static String debugDirect = "";
    private File photoFile;
    private Uri photoUri;
    private int rotation;
    private String debug = "";
    private String debugFame = "";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState){
        setTheme(getApplicationInfo().theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_scanner);

        getPermissions();
        loadViews();

        camDirect = getExternalFilesDir(null).getAbsolutePath() + "/aioa_Images";
        debugDirect = getExternalFilesDir(null).getAbsolutePath() + "/aoia_Debug";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissions(){
        int cameraPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101 );
        }

        int savePermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(savePermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102 );
        }
        int internetPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (internetPermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 103 );
        }
    }

    public void loadViews(){
        imagePreview = findViewById(R.id.image_view);
        captureButton = findViewById(R.id.capture_image);
        detectButton = findViewById(R.id.detect_image);
        sendButton = findViewById(R.id.send_details);
        settingsButton = findViewById(R.id.settings);

        captureButton.setOnClickListener(v -> launchCamera(v));
        detectButton.setOnClickListener(v -> textRecognition());
        sendButton.setOnClickListener(v -> sendDetails());
        settingsButton.setOnClickListener(v -> openSettings());

    }

    public void launchCamera(View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(camera_intent.resolveActivity(getPackageManager()) != null){
            try{
                photoFile = createImageFile();
            } catch (IOException e){
                e.printStackTrace();
            }
            if(photoFile != null){
                photoUri = FileProvider.getUriForFile(this,
                        "aioa.Scanner.fileprovider",
                        photoFile);

                camera_intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(camera_intent, 101);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK){
            bitImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            rotation = getCameraOrientation(this, photoUri, photoFile.getAbsolutePath());
            if(rotation == -1){
                Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
            }
            imagePreview.setRotation(rotation);
            imagePreview.setImageBitmap(bitImage);

        }
        else{
            Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public int getCameraOrientation(Context context, Uri imageUri, String imagePath){
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

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "AiOA_" + timeStamp +"_";

        File image = File.createTempFile(imageName, ".jpg", storageDir);
        camDirect = image.getAbsolutePath();
        return image;
    }

    public void textRecognition(){
        try{
            InputImage image = InputImage.fromBitmap(bitImage, rotation);

            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(image)
                    .addOnSuccessListener(
                            texts -> {
                                processTextRecognitionResult(texts);
                                saveDebug(debug);
                                Toast.makeText(this, R.string.scan_success, Toast.LENGTH_SHORT).show();
                                debug = "";
                                scanned = true;
                            })
                    .addOnFailureListener(
                            e -> {
                                Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
        } catch (Exception ex){
            Toast.makeText(this, R.string.scan_no_image, Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }

    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(Screen_Scanner.this, R.string.scan_empty, Toast.LENGTH_SHORT).show();
        }
        else{
            debug += "DEBUG TEXT RECOGNITION: \n";
            debug += "BLOCKS:\n";
            for(Text.TextBlock i : blocks){
                debug += "" + i.getText() + "\n";
                debug += "\tLINES:\n";
                for(Text.Line j : i.getLines()){
                    debug += "\t" + j.getText() + "\n";
                    debug += "\t\tELEMENTS:\n";
                    for(Text.Element k : j.getElements()){
                        debug += "\t\t" + k.getText() + "\n";
                    }
                }
            }
        }
    }

    private File createDebugFile() throws IOException{
        File debugDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String debugTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String debugName = "DBG_" + debugTime + "_";

        File debugFile = new File(debugDir, (debugName + ".txt"));
        debugFame = debugFile.getName();
        debugDirect = debugFile.getAbsolutePath();
        return debugFile;
    }

    private void saveDebug(String dbg){
        try{

            FileOutputStream fos = new FileOutputStream(createDebugFile());
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(dbg);
            osw.flush();
            osw.close();
            fos.flush();
            fos.close();
        }catch (IOException ie){
            ie.printStackTrace();
        }
    }

    private void sendDetails(){
        if(!scanned){
            Toast.makeText(this, R.string.scan_not_scanned, Toast.LENGTH_SHORT).show();
        }
        else{
            postData();
        }
    }

    public void openSettings(){
        Intent preferences_intent = new Intent(this, Screen_Settings.class);
        startActivity(preferences_intent);
    }

    private void postData(){
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String username = "prototype";
            String password ="protopass";
            File file = new File(debugDirect);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("password", password)
                    .addFormDataPart("file", debugFame, RequestBody.create(file, MediaType.parse("text/csv")))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .url("https://mmaps.org/uploads.php")
                    .post(requestBody)
                    .build();

            System.out.println("\n\nREQUEST " + request.body().toString());

            try(Response response = client.newCall(request).execute()){
                if(response.isSuccessful()){
                    runOnUiThread(() -> Toast.makeText(Screen_Scanner.this, R.string.scan_server_success, Toast.LENGTH_SHORT).show());
                }else{
                    runOnUiThread(() -> Toast.makeText(Screen_Scanner.this, R.string.scan_server_failure, Toast.LENGTH_SHORT).show());
                }
            }catch(IOException ie){
                ie.printStackTrace();
            }
        }).start();

    }
}
