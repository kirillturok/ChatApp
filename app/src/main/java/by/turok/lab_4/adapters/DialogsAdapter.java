package by.turok.lab_4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import by.turok.lab_4.databinding.ItemContainerDiaologBinding;
import by.turok.lab_4.entity.Dialog;
import by.turok.lab_4.entity.User;
import by.turok.lab_4.listener.UserListener;
import by.turok.lab_4.utils.Base64Coder;
import by.turok.lab_4.utils.Constants;

public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.DialogViewHolder> {

    private final List<Dialog> dialogs;
    private final String userId;
    private final UserListener userListener;


    public DialogsAdapter(List<Dialog> dialogs, String userId, UserListener userListener) {
        this.dialogs = dialogs;
        this.userId = userId;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DialogViewHolder(
                ItemContainerDiaologBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
        holder.setData(dialogs.get(position));
    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }

    class DialogViewHolder extends RecyclerView.ViewHolder {

        ItemContainerDiaologBinding binding;

        public DialogViewHolder(ItemContainerDiaologBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(Dialog dialog) {

            //Set dialog data
            binding.imageProfile.setImageBitmap(Base64Coder.decode(dialog.getUserImage()));
            binding.nameTextView.setText(dialog.getUserName());
            binding.msgText.setText(dialog.getLastMessage());
            if (dialog.getMessageCount() == 0 || dialog.getSenderId().equals(userId)) {
                binding.msgCountText.setVisibility(View.INVISIBLE);
            } else {
                binding.msgCountText.setText(Long.toString(dialog.getMessageCount()));
                binding.msgCountText.setVisibility(View.VISIBLE);
            }
            if (dialog.isOnline()) {
                binding.onlineIndicator.setVisibility(View.VISIBLE);
            } else {
                binding.onlineIndicator.setVisibility(View.INVISIBLE);
            }

            //Set online indicator
            setOnlineIndicator(dialog);

            //Set on clicked user
            User user = new User();
            user.setId(dialog.getUserId());
            user.setImage(dialog.getUserImage());
            String[] words = dialog.getUserName().split("\\s");
            user.setFirstName(words[0]);
            user.setLastName(words[1]);
            binding.getRoot().setOnClickListener(view -> userListener.onUserClicked(user));
        }

        private void setOnlineIndicator(Dialog dialog) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(dialog.getUserId())
                    .get()
                    .addOnSuccessListener(success -> {
                        if (success.getLong(Constants.KEY_AVAILABILITY) == 1) {
                            binding.onlineIndicator.setVisibility(View.VISIBLE);
                        } else {
                            binding.onlineIndicator.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }
}
