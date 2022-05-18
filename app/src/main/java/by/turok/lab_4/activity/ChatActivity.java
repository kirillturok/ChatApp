package by.turok.lab_4.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;


import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import by.turok.lab_4.R;
import by.turok.lab_4.adapters.ChatAdapter;
import by.turok.lab_4.databinding.ActivityChatBinding;
import by.turok.lab_4.entity.ChatMessage;
import by.turok.lab_4.entity.User;
import by.turok.lab_4.utils.Constants;
import by.turok.lab_4.utils.PreferenceManager;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private StorageReference storage;
    private Uri imageUri;
    private String imageName;
    private Uri fileUri;
    private String fileName;

    private static boolean isOnChatFlag;
    private static String onChatUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        setReceiverDetails();
        init();
        listenMessages();
        isOnChatFlag = true;
        onChatUserId = receiverUser.getId();

    }

    private void setListeners() {
        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.sendImage.setOnClickListener(view -> sendMessage());

        binding.attachesImage.setOnClickListener(view -> {
            binding.msgImageFrameLayout.setVisibility(View.VISIBLE);
            binding.msgFileFrameLayout.setVisibility(View.VISIBLE);
        });

        binding.msgSelectedImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.msgSelectedFile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("application/pdf");
            intent.setAction(intent.ACTION_GET_CONTENT);
            pickFile.launch(intent);
        });

        binding.msgImageDeleteCross.setOnClickListener(view -> {
            imageUri = null;
            imageName = null;
            binding.msgSelectedImage.setImageResource(R.drawable.image_gallery);
            binding.msgImageFrameLayout.setVisibility(View.GONE);
        });

        binding.msgFileDeleteCross.setOnClickListener(view -> {
            fileUri = null;
            fileName = null;
            binding.fileName.setText(null);
            binding.msgFileFrameLayout.setVisibility(View.GONE);
        });
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)),
                getBitmapFromEncodedString(receiverUser.getImage()),
                preferenceManager.getString(Constants.KEY_USER_ID),
                this
        );
        binding.msgRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
    }

    private void sendMessage() {
        if (!binding.msgEditText.getText().toString().isEmpty()) {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            message.put(Constants.KEY_MESSAGE, binding.msgEditText.getText().toString());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            message.put(Constants.KEY_IMAGE_NAME, null);
            message.put(Constants.KEY_FILE_NAME, null);
            if (imageUri != null) {
                message.put(Constants.KEY_WITH_IMAGE, true);
            } else {
                message.put(Constants.KEY_WITH_IMAGE, false);
            }
            if (fileUri != null) {
                message.put(Constants.KEY_WITH_FILE, true);
            } else {
                message.put(Constants.KEY_WITH_FILE, false);
            }
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .add(message)
                    .addOnSuccessListener(success -> {
                        if (imageUri != null) {
                            uploadImage(success.getId());
                            binding.msgImageFrameLayout.setVisibility(View.GONE);
                        }
                        if (fileUri != null) {
                            uploadFile(success.getId());
                            binding.msgFileFrameLayout.setVisibility(View.GONE);
                        }
                    });
            updateDialog(message);
            binding.msgEditText.setText(null);
        }
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    chatMessage.setDateTime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.setImageName(documentChange.getDocument().getString(Constants.KEY_IMAGE_NAME));
                    chatMessage.setFileName(documentChange.getDocument().getString(Constants.KEY_FILE_NAME));
                    chatMessage.setWithImage(documentChange.getDocument().getBoolean(Constants.KEY_WITH_IMAGE));
                    chatMessage.setWithFile(documentChange.getDocument().getBoolean(Constants.KEY_WITH_FILE));
                    chatMessage.setMessageId(documentChange.getDocument().getId());
                    chatMessages.add(chatMessage);

                    //If message comes, than we label it as viewed
                    if (documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID)
                            .equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        //If we are on chat window
                        if (isOnChatFlag) {
                            updateDialogOnChat();
                        }
                    }
                }

                // If image or file was finally uploaded to storage
                // and imageName or fileName was changed from null
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    if (documentChange.getDocument().getString(Constants.KEY_IMAGE_NAME) != null) {
                        String imageName = documentChange.getDocument().getString(Constants.KEY_IMAGE_NAME);
                        ChatMessage updatedMessage = chatMessages.stream()
                                .filter(message -> message.getMessageId()
                                        .equals(documentChange.getDocument().getId()))
                                .findFirst()
                                .get();
                        updatedMessage.setFileName(imageName);
                        chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());
                    }
                    if (documentChange.getDocument().getString(Constants.KEY_FILE_NAME) != null) {
                        String fileName = documentChange.getDocument().getString(Constants.KEY_FILE_NAME);
                        ChatMessage updatedMessage = chatMessages.stream()
                                .filter(message -> message.getMessageId()
                                        .equals(documentChange.getDocument().getId()))
                                .findFirst()
                                .get();
                        updatedMessage.setFileName(fileName);
                        chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());
                    }
                }
            }

            // View updated messages list
            Collections.sort(chatMessages, Comparator.comparing(ChatMessage::getDateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.msgRecycler.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
    });

    private void updateDialogOnChat() {
        String senderId = preferenceManager.getString(Constants.KEY_USER_ID);
        String receiverId = receiverUser.getId();
        database.collection(Constants.KEY_COLLECTION_DIALOGS)
                .whereEqualTo(Constants.KEY_FIRST_USER_ID, senderId)
                .whereEqualTo(Constants.KEY_SECOND_USER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                    .whereEqualTo(Constants.KEY_FIRST_USER_ID, receiverId)
                                    .whereEqualTo(Constants.KEY_SECOND_USER_ID, senderId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        DocumentSnapshot document = task1.getResult().getDocuments().get(0);
                                        database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                                .document(document.getId())
                                                .update(Constants.KEY_MESSAGE_COUNT, 0);
                                    });
                        } else {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                    .document(document.getId())
                                    .update(Constants.KEY_MESSAGE_COUNT, 0);
                        }
                    }
                });
    }

    private void updateDialog(HashMap<String, Object> message) {
        String senderId = preferenceManager.getString(Constants.KEY_USER_ID);
        String receiverId = receiverUser.getId();

        //update dialog
        database.collection(Constants.KEY_COLLECTION_DIALOGS)
                .whereEqualTo(Constants.KEY_FIRST_USER_ID, senderId)
                .whereEqualTo(Constants.KEY_SECOND_USER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                    .whereEqualTo(Constants.KEY_FIRST_USER_ID, receiverId)
                                    .whereEqualTo(Constants.KEY_SECOND_USER_ID, senderId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.getResult().isEmpty()) {
                                            HashMap<String, Object> dialog = new HashMap<>();
                                            dialog.put(Constants.KEY_FIRST_USER_ID, senderId);
                                            dialog.put(Constants.KEY_SECOND_USER_ID, receiverId);
                                            dialog.put(Constants.KEY_FIRST_USER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                                            dialog.put(Constants.KEY_SECOND_USER_IMAGE, receiverUser.getImage());
                                            dialog.put(Constants.KEY_FIRST_USER_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME)
                                                    + " " + preferenceManager.getString(Constants.KEY_LAST_NAME));
                                            dialog.put(Constants.KEY_SECOND_USER_NAME, receiverUser.getFirstName()
                                                    + " " + receiverUser.getLastName());
                                            dialog.put(Constants.KEY_LAST_MESSAGE, message.get(Constants.KEY_MESSAGE));
                                            dialog.put(Constants.KEY_SENDER_ID, senderId);
                                            dialog.put(Constants.KEY_TIMESTAMP, message.get(Constants.KEY_TIMESTAMP));
                                            dialog.put(Constants.KEY_MESSAGE_COUNT, 1);
                                            database.collection(Constants.KEY_COLLECTION_DIALOGS).add(dialog);
                                        } else {
                                            HashMap<String, Object> changes = new HashMap<>();
                                            changes.put(Constants.KEY_LAST_MESSAGE, message.get(Constants.KEY_MESSAGE));
                                            changes.put(Constants.KEY_SENDER_ID, senderId);
                                            changes.put(Constants.KEY_TIMESTAMP, message.get(Constants.KEY_TIMESTAMP));
                                            DocumentSnapshot document = task1.getResult().getDocuments().get(0);
                                            if (document.get(Constants.KEY_SENDER_ID).equals(senderId)) {
                                                changes.put(Constants.KEY_MESSAGE_COUNT, FieldValue.increment(1));
                                            } else {
                                                changes.put(Constants.KEY_MESSAGE_COUNT, 1);
                                            }
                                            database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                                    .document(document.getId())
                                                    .update(changes);
                                        }
                                    });
                        } else {
                            HashMap<String, Object> changes = new HashMap<>();
                            changes.put(Constants.KEY_LAST_MESSAGE, message.get(Constants.KEY_MESSAGE));
                            changes.put(Constants.KEY_SENDER_ID, senderId);
                            changes.put(Constants.KEY_TIMESTAMP, message.get(Constants.KEY_TIMESTAMP));
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            if (document.get(Constants.KEY_SENDER_ID).equals(senderId)) {
                                changes.put(Constants.KEY_MESSAGE_COUNT, FieldValue.increment(1));
                            } else {
                                changes.put(Constants.KEY_MESSAGE_COUNT, 1);
                            }
                            database.collection(Constants.KEY_COLLECTION_DIALOGS)
                                    .document(document.getId())
                                    .update(changes);
                        }
                    }
                });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();
                        String filePath = result.getData().getData().getPath();
                        imageName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        Picasso.with(this).load(imageUri).into(binding.msgSelectedImage);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        fileUri = result.getData().getData();
                        Cursor returnCursor = getContentResolver().query(fileUri, null, null, null, null);
                        int fileNameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        returnCursor.moveToFirst();
                        fileName = returnCursor.getString(fileNameIndex);
                        binding.fileName.setText(fileName);
                    }
                }
            }
    );

    private void uploadImage(String documentId) {
        StorageReference imageRef = storage.child("uploads/" + documentId + "/image/" + imageName);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    database.collection(Constants.KEY_COLLECTION_CHAT)
                            .document(documentId)
                            .update(Constants.KEY_IMAGE_NAME, imageName)
                            .addOnCompleteListener(complete -> {
                                imageUri = null;
                                imageName = null;
                            });
                });
    }

    private void uploadFile(String documentId) {
        StorageReference imageRef = storage.child("uploads/" + documentId + "/file/" + fileName);
        imageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    database.collection(Constants.KEY_COLLECTION_CHAT)
                            .document(documentId)
                            .update(Constants.KEY_FILE_NAME, fileName)
                            .addOnCompleteListener(complete -> {
                                fileUri = null;
                                fileName = null;
                            });
                });
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        try {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }


    private void setReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.userNameText.setText(receiverUser.getFirstName() + " " + receiverUser.getLastName());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    @Override
    protected void onPause() {
        super.onPause();
        isOnChatFlag = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnChatFlag = true;
    }
}