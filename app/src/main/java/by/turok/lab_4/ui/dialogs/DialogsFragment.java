package by.turok.lab_4.ui.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import by.turok.lab_4.activity.ChatActivity;
import by.turok.lab_4.adapters.DialogsAdapter;
import by.turok.lab_4.databinding.FragmentDialogsBinding;
import by.turok.lab_4.entity.Dialog;
import by.turok.lab_4.entity.User;
import by.turok.lab_4.listener.UserListener;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;

public class DialogsFragment extends Fragment implements UserListener {

    private FragmentDialogsBinding binding;
    private List<Dialog> dialogs;
    private DialogsAdapter dialogsAdapter;
    PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDialogsBinding.inflate(inflater, container, false);
        init();
        listenDialogs();
        View root = binding.getRoot();
        return root;
    }

    private void init() {
        dialogs = new ArrayList<>();
        preferenceManager = new PreferenceManager(getContext());
        dialogsAdapter = new DialogsAdapter(dialogs, preferenceManager.getString(Constants.KEY_USER_ID), this);
        binding.dialogsRecycler.setAdapter(dialogsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenDialogs() {
        database.collection(Constants.KEY_COLLECTION_DIALOGS)
                .whereEqualTo(Constants.KEY_FIRST_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(dialogsEventListener);
        database.collection(Constants.KEY_COLLECTION_DIALOGS)
                .whereEqualTo(Constants.KEY_SECOND_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(dialogsEventListener);
    }

    private final EventListener<QuerySnapshot> dialogsEventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot document = documentChange.getDocument();
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Dialog dialog = new Dialog();
                    if (document.get(Constants.KEY_FIRST_USER_ID)
                            .equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        dialog.setUserId(document.getString(Constants.KEY_SECOND_USER_ID));
                        dialog.setUserName(document.getString(Constants.KEY_SECOND_USER_NAME));
                        dialog.setUserImage(document.getString(Constants.KEY_SECOND_USER_IMAGE));
                    } else {
                        dialog.setUserId(document.getString(Constants.KEY_FIRST_USER_ID));
                        dialog.setUserName(document.getString(Constants.KEY_FIRST_USER_NAME));
                        dialog.setUserImage(document.getString(Constants.KEY_FIRST_USER_IMAGE));
                    }
                    dialog.setLastMessage(document.getString(Constants.KEY_LAST_MESSAGE));
                    dialog.setTimestamp(document.getDate(Constants.KEY_TIMESTAMP));
                    dialog.setSenderId(document.getString(Constants.KEY_SENDER_ID));
                    dialog.setMessageCount(document.getLong(Constants.KEY_MESSAGE_COUNT));
                    dialog.setOnline(false);
                    dialogs.add(dialog);
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    Dialog dialog;
                    if (document.get(Constants.KEY_FIRST_USER_ID)
                            .equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        dialog = dialogs.stream()
                                .filter(d -> d.getUserId().equals(document.getString(Constants.KEY_SECOND_USER_ID)))
                                .findFirst()
                                .get();
                    } else {
                        dialog = dialogs.stream()
                                .filter(d -> d.getUserId().equals(document.getString(Constants.KEY_FIRST_USER_ID)))
                                .findFirst()
                                .get();
                    }
                    dialog.setLastMessage(document.getString(Constants.KEY_LAST_MESSAGE));
                    dialog.setSenderId(document.getString(Constants.KEY_SENDER_ID));
                    dialog.setMessageCount(document.getLong(Constants.KEY_MESSAGE_COUNT));
                    dialog.setTimestamp(document.getDate(Constants.KEY_TIMESTAMP));
                }
                Collections.sort(dialogs, Comparator.comparing(Dialog::getTimestamp).reversed());
                dialogsAdapter.notifyDataSetChanged();
            }
        }
    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}