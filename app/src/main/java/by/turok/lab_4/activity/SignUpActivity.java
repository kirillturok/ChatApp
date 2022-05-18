package by.turok.lab_4.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

import by.turok.lab_4.R;
import by.turok.lab_4.databinding.ActivitySignUpBinding;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;
import by.turok.lab_4.utils.UserValidator;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private DatePickerDialog datePickerDialog;
    private String encodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        initDatePicker();
        setListeners();
    }

    private void setListeners() {
        binding.dateRegEditText.setOnClickListener(view -> datePickerDialog.show());

        binding.signUpButton.setOnClickListener(view -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void signUp() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        loading(true);

        String email = binding.emailRegEditText.getText().toString().trim();
        String phone = binding.phoneRegEditText.getText().toString().trim();
        String firstName = binding.firstNameRegEditText.getText().toString().trim();
        String lastName = binding.lastNameRegEditText.getText().toString().trim();
        String birthday = binding.dateRegEditText.getText().toString().trim();
        String password = binding.passwordRegEditText.getText().toString().trim();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(success -> {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_EMAIL, email);
                    user.put(Constants.KEY_PHONE, phone);
                    user.put(Constants.KEY_FIRST_NAME, firstName);
                    user.put(Constants.KEY_LAST_NAME, lastName);
                    user.put(Constants.KEY_BIRTHDAY, birthday);
                    user.put(Constants.KEY_IMAGE, encodedImage);
                    user.put(Constants.KEY_AVAILABILITY, 0);
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .add(user)
                            .addOnSuccessListener(documentReference -> {
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                preferenceManager.putString(Constants.KEY_EMAIL, email);
                                preferenceManager.putString(Constants.KEY_PHONE, phone);
                                preferenceManager.putString(Constants.KEY_FIRST_NAME, firstName);
                                preferenceManager.putString(Constants.KEY_LAST_NAME, lastName);
                                preferenceManager.putString(Constants.KEY_BIRTHDAY, birthday);
                                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                loading(false);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(exception -> {
                                auth.getCurrentUser().delete();
                                showToast(exception.getLocalizedMessage());
                                loading(false);
                            });
                })
                .addOnFailureListener(failure -> {
                    showToast(failure.getLocalizedMessage());
                    loading(false);
                });
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.addImageTextView.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidSignUpDetails() {
        boolean result = true;
        if (!UserValidator.validateEmail(binding.emailRegEditText.getText().toString().trim())) {
            result = false;
            binding.emailRegEditText.setError(getResources().getString(R.string.msg_validation_email));
        }
        if (!UserValidator.validatePhone(binding.phoneRegEditText.getText().toString().trim())) {
            result = false;
            binding.phoneRegEditText.setError(getResources().getString(R.string.msg_validation_phone));
        }
        if (!UserValidator.validateFirstName(binding.firstNameRegEditText.getText().toString().trim())) {
            result = false;
            binding.firstNameRegEditText.setError(getResources().getString(R.string.msg_validation_first_name));
        }
        if (!UserValidator.validateLastName(binding.lastNameRegEditText.getText().toString().trim())) {
            result = false;
            binding.lastNameRegEditText.setError(getResources().getString(R.string.msg_validation_last_name));
        }
        if (binding.dateRegEditText.getText().toString().trim().isEmpty()) {
            result = false;
            binding.dateRegEditText.setError(getResources().getString(R.string.msg_validation_birthday));
        }
        if (!UserValidator.validatePassword(binding.passwordRegEditText.getText().toString().trim())) {
            result = false;
            binding.passwordRegEditText.setError(getResources().getString(R.string.msg_validation_password));
        }
        return result;
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month++;
            String dateString = day < 10 ? "0" + day + "." : day + ".";
            dateString += month < 10 ? "0" + month + "." : month + ".";
            dateString += year;

            binding.dateRegEditText.setText(dateString);
            binding.dateRegEditText.setError(null); // clear error
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.signUpButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.signUpButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}