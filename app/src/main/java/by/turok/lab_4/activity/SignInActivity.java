package by.turok.lab_4.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import by.turok.lab_4.R;
import by.turok.lab_4.databinding.ActivitySignInBinding;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;
import by.turok.lab_4.utils.UserValidator;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.signUpTextView.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.signInButton.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    private void signIn() {
        loading(true);

        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(success -> {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, email)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null
                                        && task.getResult().getDocuments().size() > 0) {
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                    preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                                    preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                                    preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                                    preferenceManager.putString(Constants.KEY_BIRTHDAY, documentSnapshot.getString(Constants.KEY_BIRTHDAY));
                                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    loading(false);
                                    showToast(getResources().getString(R.string.toast_unable_sign_in));
                                }
                            });
                })
                .addOnFailureListener(failure -> {
                    showToast(failure.getLocalizedMessage());
                    loading(false);
                });

    }

    private boolean isValidSignInDetails() {
        boolean result = true;
        if (!UserValidator.validateEmail(binding.emailEditText.getText().toString().trim())) {
            result = false;
            binding.emailEditText.setError(getResources().getString(R.string.msg_validation_email));
        }
        if (!UserValidator.validatePassword(binding.passwordEditText.getText().toString().trim())) {
            result = false;
            binding.passwordEditText.setError(getResources().getString(R.string.msg_validation_password));
        }
        return result;
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.signInButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.signInButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}