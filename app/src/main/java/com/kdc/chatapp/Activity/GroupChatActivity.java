package com.kdc.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kdc.chatapp.Adapter.GroupMsgAdapter;
import com.kdc.chatapp.Model.Messages;
import com.kdc.chatapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private RecyclerView recyclerView;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    private ArrayList<Messages> groupMessenger;
    private GroupMsgAdapter groupMsgAdapter;

    private LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_2);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        InitializeFields();

        groupMessenger = new ArrayList<>();
        getMessengerList();
        recyclerView = findViewById(R.id.group_chat_layout);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        groupMsgAdapter = new GroupMsgAdapter(groupMessenger);
        recyclerView.setAdapter(groupMsgAdapter);



        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");
            }
        });
    }

    private void getMessengerList() {
        groupMessenger.clear();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    groupMessenger.add(messages);

                    groupMsgAdapter.notifyDataSetChanged();

                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);

    }

    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("from", currentUserID);
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
                GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

}
