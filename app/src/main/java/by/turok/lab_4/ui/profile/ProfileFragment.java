package by.turok.lab_4.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import by.turok.lab_4.R;
import by.turok.lab_4.activity.SignInActivity;
import by.turok.lab_4.databinding.FragmentProfileBinding;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getContext());
        loadUserDetails();
        setListeners();

        View root = binding.getRoot();
        return root;
    }

    private void setListeners() {
        binding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.logOutButton.setOnClickListener(view -> signOut());

        binding.changeLangButton.setOnClickListener(view -> {
            //// TODO: 08.05.2022 доделать свитч между локалями, а не только на инглишь
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Resources resources = this.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());

            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        });
    }

    private void loadUserDetails() {
        binding.nameTextView.setText(preferenceManager.getString(Constants.KEY_FIRST_NAME) + " " + preferenceManager.getString(Constants.KEY_LAST_NAME));
        binding.emailTextView.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.phoneTextView.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.birthdayTextView.setText(preferenceManager.getString(Constants.KEY_BIRTHDAY));
        if (preferenceManager.getString(Constants.KEY_IMAGE) != null) {
            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
            binding.addImageTextView.setVisibility(View.GONE);
        }
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, FieldValue.delete())
                .addOnSuccessListener(success -> {
                    preferenceManager.clear();
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .addOnFailureListener(failure -> showToast(getResources().getString(R.string.toast_unable_sign_out)));
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
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
                            loadingImage(true);
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String encodedImage = encodeImage(bitmap);

                            FirebaseFirestore database = FirebaseFirestore.getInstance();

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                    .update(Constants.KEY_IMAGE, encodedImage)
                                    .addOnSuccessListener(success -> {
                                        loadingImage(false);
                                        binding.imageProfile.setImageBitmap(bitmap);
                                        binding.addImageTextView.setVisibility(View.GONE);
                                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                    })
                                    .addOnFailureListener(failure -> {
                                        loadingImage(false);
                                        showToast(failure.getLocalizedMessage());
                                    });

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void loadingImage(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}