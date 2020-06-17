package com.kdc.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.ConversationActions;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context mContext;

    public MessageAdapter(List<Messages> userMessageList, Context mContext){
        this.userMessageList = userMessageList;
        this.mContext = mContext;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture, messageSenderSticker, messageReceiverSticker;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverSticker = itemView.findViewById(R.id.message_receiver_sticker_view);
            messageSenderSticker = itemView.findViewById(R.id.message_sender_sticker_view);



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
            messageViewHolder.messageSenderSticker.setVisibility(View.GONE);
            messageViewHolder.messageReceiverSticker.setVisibility(View.GONE);

            String messageSenderID = mAuth.getCurrentUser().getUid();
            Messages messages = userMessageList.get(position);

            String fromUserID = messages.getFrom();
            String fromMessageType = messages.getType();
            String name = messages.getName();

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

                    if (checkNextMessageSender(userMessageList, position)) {
                        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                    } else {
                        messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    }
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setText(messages.getMessage());

                }
                else if(fromMessageType.equals("location")){
                    if (checkNextMessageSender(userMessageList, position)) {
                        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                    } else {
                        messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                    }
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.location,0,0,0);
                    messageViewHolder.receiverMessageText.setText("My's location");


                }
                else if(fromMessageType.equals("image")) {
                    if (checkNextMessageSender(userMessageList, position)) {
                        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                    } else {
                        messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    }

                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

                }

                else if(fromMessageType.equals("sticker")) {
                    if (name.endsWith(".gif")) {
                        messageViewHolder.messageReceiverSticker.setVisibility(View.VISIBLE);
                        Glide.with(mContext).asGif().load(messages.getMessage()).into(messageViewHolder.messageReceiverSticker);
                    } else {
                        messageViewHolder.messageReceiverSticker.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(messages.getMessage()).into(messageViewHolder.messageReceiverSticker);
                    }

                    if (checkNextMessageSender(userMessageList, position)) {
                        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                    } else {
                        messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    }


                }

                else {
                    if (checkNextMessageSender(userMessageList, position)) {
                        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                    } else {
                        messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                    }
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setText(messages.getName());
                    messageViewHolder.receiverMessageText
                            .setPaintFlags(messageViewHolder.receiverMessageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                }


            } else {

                if(fromMessageType.equals("text")){

                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.senderMessageText.setText(messages.getMessage());

                }
                else if(fromMessageType.equals("location")){

                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.senderMessageText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.location,0,0,0);
                    messageViewHolder.senderMessageText.setText("My's location");

                }
                else if(fromMessageType.equals("image")) {
                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                }

                else if(fromMessageType.equals("sticker")) {
                    if (name.endsWith(".gif")) {
                        messageViewHolder.messageSenderSticker.setVisibility(View.VISIBLE);
                        Glide.with(mContext).asGif().load(messages.getMessage()).into(messageViewHolder.messageSenderSticker);
                    } else {
                        messageViewHolder.messageSenderSticker.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(messages.getMessage()).into(messageViewHolder.messageSenderSticker);
                    }

                }

                else {
                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.senderMessageText.setText(messages.getName());
                    messageViewHolder.senderMessageText
                            .setPaintFlags(messageViewHolder.senderMessageText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

                }
            }



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
                                            "View this Image in your browser",
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
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (i == 3) {
                                        deleteMessageForEveryone(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("location")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Open maps",
                                            "Cancel"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Choose your action");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
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
                                            "View this Image in your browser",
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
                                    else if (i == 2) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessageList.get(position).getType().equals("location")) {
                            CharSequence option[] = new CharSequence[]
                                    {
                                            "Open maps",
                                            "Cancel"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Choose your action");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private boolean checkNextMessageSender(List<Messages> messages, int position) {

        if (position != 0 && messages.size() > 0) {
            if (messages.get(position).getFrom().equals(messages.get(position - 1).getFrom())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
