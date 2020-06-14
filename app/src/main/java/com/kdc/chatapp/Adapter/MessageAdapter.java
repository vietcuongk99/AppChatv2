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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, senderName;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
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
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {

            messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
            messageViewHolder.senderMessageText.setVisibility(View.GONE);
            messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
            messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

            String messageSenderID = mAuth.getCurrentUser().getUid();
            Messages messages = userMessageList.get(position);

            String fromUserID = messages.getFrom();
            String fromMessageType = messages.getType();

            if (!fromUserID.equals(messageSenderID)) {
                usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(fromUserID);
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image")){
                            String receiverImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(receiverImage).into(messageViewHolder.receiverProfileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



                if(fromMessageType.equals("text")){

                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setText(messages.getMessage());


                }
                else if(fromMessageType.equals("image")) {

                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

                }

                else {
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                    messageViewHolder.receiverMessageText.setText(messages.getName());

                }


            } else {

                if(fromMessageType.equals("text")){

                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.senderMessageText.setText(messages.getMessage());

                }
                else if(fromMessageType.equals("image")) {
                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                }

                else {
                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                    messageViewHolder.senderMessageText.setText(messages.getName());

                }
            }



    /*

            if(fromMessageType.equals("text")){

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
            else if(fromMessageType.equals("image")) {
                if(fromUserID.equals(messageSenderID)) {

                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                }
                else {
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

                }
            }

            else {
                if(fromUserID.equals(messageSenderID)){
                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.senderMessageText.setText(messages.getName());
                    messageViewHolder.senderMessageText
                            .setPaintFlags(messageViewHolder.senderMessageText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                }
                else {

                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setText(messages.getName());
                    messageViewHolder.receiverMessageText
                            .setPaintFlags(messageViewHolder.receiverMessageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }

     */

            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "Download and View this Document",
                                            "Delete for everyone",
                                            "Cancel"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteSentMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1) {

                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }
                                    else if(i == 2){
                                        deleteMessageForEveryone(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("text")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "Delete for everyone",
                                            "Cancel"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteSentMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1) {
                                        deleteMessageForEveryone(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("image")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "View this Image",
                                            "Delete for everyone",
                                            "Cancel"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteSentMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1) {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                        intent.putExtra("url", userMessageList.get(position).getMessage());
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 2){
                                        deleteMessageForEveryone(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();
                        }


                    }
                });

            }





            else {
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "Download and View this Document",
                                            "Cancel",
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1) {

                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("text")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "Cancel",
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("image")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Delete for me",
                                            "View this Image",
                                            "Cancel",
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message ?");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1) {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                        intent.putExtra("url", userMessageList.get(position).getMessage());
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }
                                }
                            });

                            builder.show();
                        }


                    }
                });
            }
    }





    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child("listMessage")
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Delete Successfully.", Toast.LENGTH_SHORT).show();

                }

                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child("listMessage")
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Delete Successfully.", Toast.LENGTH_SHORT).show();

                }

                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child("listMessage")
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child("listMessage")
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Delete Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
