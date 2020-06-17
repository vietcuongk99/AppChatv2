package com.kdc.chatapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kdc.chatapp.Activity.ChatActivity;
import com.kdc.chatapp.Model.Sticker;
import com.kdc.chatapp.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {
    private List<Sticker> stickers;
    private Context mContext;
    private String messageSenderID;
    private String messageReceiverID;
    private String saveCurrentTime;
    private String saveCurrentDate;
    private String currentGroupName, currentUserID;
    private String type;


    public StickerAdapter(List<Sticker> stickers, Context mContext, String messageSenderID,
                          String messageReceiverID, String saveCurrentTime, String saveCurrentDate, String type) {
        this.stickers = stickers;
        this.mContext = mContext;
        this.messageSenderID = messageSenderID;
        this.messageReceiverID = messageReceiverID;
        this.saveCurrentTime = saveCurrentTime;
        this.saveCurrentDate = saveCurrentDate;
        this.type = type;
    }

    public StickerAdapter(List<Sticker> stickers, Context mContext, String currentGroupName, String currentUserID, String type) {
        this.stickers = stickers;
        this.mContext = mContext;
        this.currentGroupName = currentGroupName;
        this.currentUserID = currentUserID;
        this.type = type;
    }

    @NonNull
    @Override
    public StickerAdapter.StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new View(mContext);

        view = LayoutInflater.from(mContext).inflate(R.layout.sticker_layout_item, parent, false);
        return new StickerAdapter.StickerViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final StickerAdapter.StickerViewHolder holder, final int position) {

        holder.title.setText(stickers.get(position).getName());
        Glide.with(mContext).load(stickers.get(position).getUrl()).into(holder.image);

        holder.stateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (type.equals("chat")) {
                    final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                    final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();

                    final String messagePushID = userMessageKeyRef.getKey();


                    final StorageReference storageReference = FirebaseStorage
                            .getInstance()
                            .getReference().child("Sticker" + "/" + messagePushID + "_" + stickers.get(position).getName());
                    storageReference.putBytes(getByteArray(holder.image)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    HashMap<String, Object> messageTextBody = new HashMap();
                                    messageTextBody.put("message", url);
                                    messageTextBody.put("name", stickers.get(position).getName());
                                    messageTextBody.put("type", "sticker");
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("to", messageReceiverID);
                                    messageTextBody.put("messageID", messagePushID);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("date", saveCurrentDate);


                                    HashMap<String, Object> messageBodyDetails = new HashMap<>();
                                    messageBodyDetails.put(messageSenderRef + "/listMessage/" + messagePushID, messageTextBody);
                                    messageBodyDetails.put(messageReceiverRef + "/listMessage/" + messagePushID, messageTextBody);

                                    FirebaseDatabase.getInstance().getReference().updateChildren(messageBodyDetails)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        HashMap<String, Object> messageBodyDetails = new HashMap<>();
                                                        messageBodyDetails.put(messageSenderRef + "/stateUserSee", 1);
                                                        messageBodyDetails.put(messageReceiverRef + "/stateUserSee", 0);
                                                        FirebaseDatabase.getInstance().getReference().updateChildren(messageBodyDetails);
                                                    } else {
                                                        Toast.makeText(mContext, "Error.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }
                            });

                        }
                    });

                }
                else {


                    DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups")
                            .child(currentGroupName).push();

                    final String messagePushID = userMessageKeyRef.getKey();

                    final StorageReference storageReference = FirebaseStorage
                            .getInstance()
                            .getReference().child("Sticker" + "/" + messagePushID + "_" + stickers.get(position).getName());
                    storageReference.putBytes(getByteArray(holder.image)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    DatabaseReference GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
                                    DatabaseReference GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messagePushID);

                                    HashMap<String, Object> messageInfoMap = new HashMap<>();
                                    messageInfoMap.put("from", currentUserID);
                                    messageInfoMap.put("name", stickers.get(position).getName());
                                    messageInfoMap.put("message", url);
                                    messageInfoMap.put("date", saveCurrentDate);
                                    messageInfoMap.put("time", saveCurrentTime);
                                    messageInfoMap.put("type", "sticker");
                                    GroupMessageKeyRef.updateChildren(messageInfoMap);

                                }
                            });

                        }
                    });
                }

            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    public static class StickerViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView image;
        private FrameLayout stateItem;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.image_name);
            image = itemView.findViewById(R.id.image);
            stateItem = itemView.findViewById(R.id.frame_container);
        }
    }


    public byte[] getByteArray(ImageView imageView){
        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }

}
