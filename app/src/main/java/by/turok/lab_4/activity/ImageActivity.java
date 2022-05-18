package by.turok.lab_4.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import by.turok.lab_4.R;
import by.turok.lab_4.databinding.ActivityImageBinding;
import by.turok.lab_4.utils.Constants;

public class ImageActivity extends AppCompatActivity {

    private ActivityImageBinding binding;
    private Bitmap bitmap;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        imageName = getIntent().getExtras().getString(Constants.KEY_IMAGE_NAME);
        byte[] bytes = getIntent().getByteArrayExtra(Constants.KEY_IMAGE);
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageView.setImageBitmap(bitmap);
        binding.imageName.setText(imageName);
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.saveImageText.setOnClickListener(view -> {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "Image",
                    "Saved image"
            );
            String text = imageName + " " + R.string.toast_image_saved;
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        });
    }

}