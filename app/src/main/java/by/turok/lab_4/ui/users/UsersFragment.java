package by.turok.lab_4.ui.users;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import by.turok.lab_4.R;
import by.turok.lab_4.activity.UserProfileActivity;
import by.turok.lab_4.adapters.UsersAdapter;
import by.turok.lab_4.databinding.FragmentUsersBinding;
import by.turok.lab_4.entity.User;
import by.turok.lab_4.listener.UserListener;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;

public class UsersFragment extends Fragment implements UserListener {

    private FragmentUsersBinding binding;
    private PreferenceManager preferenceManager;

    private List<User> users;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUsersBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getContext());
        binding.searchImage.callOnClick();
        getUsers();
        setListeners();
        View root = binding.getRoot();
        return root;
    }

    private void setListeners() {
        binding.searchImage.setOnClickListener(view -> {
            if (!binding.searchEditText.getText().toString().isEmpty()) {
                clearSearch();
            }
        });
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();
                if (searchString.isEmpty()) {
                    binding.searchImage.setImageResource(R.drawable.search);
                } else {
                    binding.searchImage.setImageResource(R.drawable.back);
                }
                if (users != null) {
                    searchUsers(searchString);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void searchUsers(String searchString) {
        final String SPLIT_REGEX = "\\s+";
        String[] words = searchString.split(SPLIT_REGEX);
        List<User> matchedUsers = new ArrayList<>();
        for (User user : users) {
            for (String word : words) {
                if (user.getFirstName().contains(word) || user.getLastName().contains(word)) {
                    matchedUsers.add(user);
                }
            }
        }
        if (users.size() > 0) {
            matchedUsers.sort(new User.UserNameComparator());
            UsersAdapter usersAdapter = new UsersAdapter(matchedUsers, this);
            binding.usersRecyclerView.setAdapter(usersAdapter);
        }

    }

    private void getUsers() {
        //loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    //loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User(
                                    queryDocumentSnapshot.getId(),
                                    queryDocumentSnapshot.getString(Constants.KEY_EMAIL),
                                    queryDocumentSnapshot.getString(Constants.KEY_PHONE),
                                    queryDocumentSnapshot.getString(Constants.KEY_FIRST_NAME),
                                    queryDocumentSnapshot.getString(Constants.KEY_LAST_NAME),
                                    queryDocumentSnapshot.getString(Constants.KEY_BIRTHDAY),
                                    queryDocumentSnapshot.getString(Constants.KEY_IMAGE),
                                    queryDocumentSnapshot.getLong(Constants.KEY_AVAILABILITY),
                                    queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            users.sort(new User.UserNameComparator());
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                        }
                    }
                    clearSearch();
                });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        //// // TODO: 08.05.2022 финиш у фрагмета нужен не нужен хз как поступить
    }

    private void clearSearch() {
        binding.searchEditText.setText("");
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}