package by.turok.lab_4.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import by.turok.lab_4.databinding.ActivityUserProfileBinding;
import by.turok.lab_4.entity.User;
import by.turok.lab_4.utils.Constants;

public class UserProfileActivity extends BaseActivity {

    private ActivityUserProfileBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUserData();
        setListeners();
    }

    private void setListeners() {

        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.sendMsgBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();
        });
    }

    private Bitmap getUserImage(String encodedImage) {
        try {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private void setUserData() {
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
        binding.nameText.setText(user.getFirstName() + " " + user.getLastName());
        binding.emailText.setText(user.getEmail());
        binding.phoneText.setText(user.getPhone());
        binding.birthdayText.setText(user.getBirthday());
    }
}