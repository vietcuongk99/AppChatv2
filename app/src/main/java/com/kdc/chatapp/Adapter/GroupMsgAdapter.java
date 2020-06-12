package com.kdc.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kdc.chatapp.Activity.ImageViewerActivity;
import com.kdc.chatapp.Activity.MainActivity;
import com.kdc.chatapp.Model.Messages;
import com.kdc.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMsgAdapter extends RecyclerView.Adapter<GroupMsgAdapter.GroupMsgViewHolder> {

    private List<Messages> groupMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public GroupMsgAdapter(List<Messages> groupMessageList){
        this.groupMessageList = groupMessageList;
    }

    public class GroupMsgViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public GroupMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @Override
    public GroupMsgAdapter.GroupMsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        return new GroupMsgAdapter.GroupMsgViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final GroupMsgAdapter.GroupMsgViewHolder messageViewHolder, final int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = groupMessageList.get(position);

        String fromUserID = messages.getFrom();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                            .into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);

        if(fromUserID.equals(messageSenderID)){
            messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

            messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
            messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
            messageViewHolder.senderMessageText.setText(messages.getMessage());
        }
        else{

            messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
            messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

            messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
            messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
            messageViewHolder.receiverMessageText.setText(messages.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return groupMessageList.size();
    }


}
