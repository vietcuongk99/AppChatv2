package com.kdc.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nikartm.button.FitButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.kdc.chatapp.Adapter.GroupMsgAdapter;
import com.kdc.chatapp.Model.Messages;
import com.kdc.chatapp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FitButton SendMessageButton, SendFilesButton;
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

    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    private ImageButton Location;
    LocationManager locationManager;
    LocationListener locationListener;

    private ImageButton add_member;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        InitializeFields();

        groupMessenger = new ArrayList<>();
        getMessengerList();
        recyclerView = findViewById(R.id.group_chat_layout);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        groupMsgAdapter = new GroupMsgAdapter(groupMessenger);
        recyclerView.setAdapter(groupMsgAdapter);


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();

            }
        });

        loadingBar = new ProgressDialog(this);
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Image",
                                "PDF Files",
                                "Ms Word Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);

                        }
                        if (i == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);

                        }
                        if (i == 2) {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);

                        }
                    }
                });

                builder.show();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location = findViewById(R.id.Location);
        Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLocation();
            }
        });

        add_member = findViewById(R.id.add_member);
        add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map AddUser = new HashMap();
                AddUser.put("position", "admin");
                GroupNameRef.child("members").child(currentUserID).updateChildren(AddUser);

                Intent intent = new Intent(GroupChatActivity.this, AddMemActivity.class);
                intent.putExtra("groupName", currentGroupName);
                startActivity(intent);
            }
        });
    }

    private void getMessengerList() {
        groupMessenger.clear();
        GroupNameRef.child("listMessage").addChildEventListener(new ChildEventListener() {
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

        SendMessageButton = (FitButton) findViewById(R.id.send_message_button);
        SendFilesButton = findViewById(R.id.send_files_btn);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

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
        String message = userMessageInput.getText().toString().trim();
        String messageKey = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message) || message.equals("")) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {

            GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("from", currentUserID);
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("type", "text");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            fileUri = data.getData();

            // lấy ra tên file pdf và file doc
            String uriString = fileUri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getApplicationContext().getContentResolver().query(fileUri,
                            null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            String finalDisplayName = displayName;

            // nếu file gửi không phải image
            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef = "Groups/" + currentGroupName;

                DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups")
                        .child(currentGroupName).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                loadingBar.dismiss();
                                String downloadUrl = uri.toString();

                                GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messagePushID);

                                HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("from", currentUserID);
                                messageInfoMap.put("name", finalDisplayName);
                                messageInfoMap.put("message", downloadUrl);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("type", checker);
                                GroupMessageKeyRef.updateChildren(messageInfoMap);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });
            } else if (checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Groups/" + currentGroupName;

                DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups")
                        .child(currentGroupName).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            loadingBar.dismiss();

                            GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messagePushID);

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("from", currentUserID);
                            messageInfoMap.put("name", fileUri.getLastPathSegment());
                            messageInfoMap.put("message", myUrl);
                            messageInfoMap.put("date", currentDate);
                            messageInfoMap.put("time", currentTime);
                            messageInfoMap.put("type", checker);
                            GroupMessageKeyRef.updateChildren(messageInfoMap);

                        }


                    }
                });


            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }

        }
    }

//    private void SendLocation(){
//
//        String messageKey = GroupNameRef.push().getKey();
//
//        GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messageKey);
//
//        HashMap<String, Object> messageInfoMap = new HashMap<>();
//        messageInfoMap.put("from", currentUserID);
//        messageInfoMap.put("name", currentUserName);
//        messageInfoMap.put("message", "ShareLocation");
//        messageInfoMap.put("date", currentDate);
//        messageInfoMap.put("time", currentTime);
//        messageInfoMap.put("type", "text");
//        GroupMessageKeyRef.updateChildren(messageInfoMap);
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void SendLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String uri = "geo:" + latitude + ","
                            + longitude + "?q=" + latitude
                            + "," + longitude;

                    String messageKey = GroupNameRef.push().getKey();

                    GroupMessageKeyRef = GroupNameRef.child("listMessage").child(messageKey);

                    HashMap<String, Object> messageInfoMap = new HashMap<>();
                    messageInfoMap.put("from", currentUserID);
                    messageInfoMap.put("name", currentUserName);
                    messageInfoMap.put("message", uri);
                    messageInfoMap.put("date", currentDate);
                    messageInfoMap.put("time", currentTime);
                    messageInfoMap.put("type", "location");
                    GroupMessageKeyRef.updateChildren(messageInfoMap);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000000000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000000000, 0, locationListener);
        }
    }
}
