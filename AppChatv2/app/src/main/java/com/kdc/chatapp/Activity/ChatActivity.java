package com.kdc.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kdc.chatapp.Adapter.StickerAdapter;
import com.kdc.chatapp.Call.CallScreenActivity;
import com.kdc.chatapp.Adapter.MessageAdapter;
import com.kdc.chatapp.Model.Messages;
import com.kdc.chatapp.Model.Sticker;
import com.kdc.chatapp.R;
import com.kdc.chatapp.Call.SinchService;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FitButton SendMessageButton, SendFilesButton, SendStickerButton;
    private EditText MessageInputText;

    private List<Messages> messagesList;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    private ImageButton pCall;
    private ImageButton vCall;

    private ImageButton Location;
    LocationManager locationManager;
    LocationListener locationListener;


    private FitButton close_btn;
    private RelativeLayout chat_activity, choose_sticker, chat_layout;
    private String[] fileList;
    private RecyclerView listSticker;
    private List<Sticker> stickers;
    private StickerAdapter stickerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        messagesList = new ArrayList<>();
        getMessengerList();

        stickers = new ArrayList<>();
        getStickerList();

        InitializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });


        DisplayLastSeen();


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Image",
                                "PDF Files",
                                "Ms Word Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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
        pCall.setOnClickListener(buttonClickListener);
        vCall.setOnClickListener(buttonClickListener);
    }


    private void InitializeControllers() {

        ChatToolBar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = findViewById(R.id.send_message_btn);
        SendFilesButton = findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList, ChatActivity.this);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        pCall = findViewById(R.id.pCall);
        vCall = findViewById(R.id.vCall);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location = findViewById(R.id.Location);
        Location.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                SendLocation();
            }
        });


        chat_activity = findViewById(R.id.chat_layout);
        listSticker = findViewById(R.id.group_divider);
        listSticker.setLayoutManager(new GridLayoutManager(getApplicationContext(), 5));
        listSticker.setHasFixedSize(true);

        chat_layout = findViewById(R.id.chat_layout);
        choose_sticker = findViewById(R.id.choose_sticker);
        close_btn = findViewById(R.id.close_btn);
        SendStickerButton = findViewById(R.id.send_sticker_btn);
        SendStickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int height = size.y;
                int width = size.x;


                top_content.getLayoutParams().height = height/3;

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(group_divider);
                group_divider.getLayoutParams().width = width;
                group_divider.getLayoutParams().height = width/6;

                 */

                choose_sticker.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ABOVE, R.id.choose_sticker);
                params.addRule(RelativeLayout.BELOW, R.id.chat_toolbar);
                userMessagesList.setLayoutParams(params);
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                stickerAdapter = new StickerAdapter(stickers, getApplicationContext(),
                        messageSenderID, messageReceiverID, saveCurrentTime, saveCurrentDate, "chat");
                listSticker.setAdapter(stickerAdapter);
            }
        });

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose_sticker.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ABOVE, R.id.chat_layout);
                params.addRule(RelativeLayout.BELOW, R.id.chat_toolbar);
                userMessagesList.setLayoutParams(params);
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }
        });


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
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", downloadUrl);
                                messageImageBody.put("name", finalDisplayName);
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", messageSenderID);
                                messageImageBody.put("to", messageReceiverID);
                                messageImageBody.put("messageID", messagePushID);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("date", saveCurrentDate);


                                Map messageBodyDetail = new HashMap();
                                messageBodyDetail.put(messageSenderRef + "/listMessage/" + messagePushID, messageImageBody);
                                messageBodyDetail.put(messageReceiverRef + "/listMessage/" + messagePushID, messageImageBody);

                                RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Map messageBodyDetails = new HashMap();
                                            messageBodyDetail.put(messageSenderRef + "/stateUserSee", 1);
                                            messageBodyDetails.put(messageReceiverRef + "/stateUserSee", 0);
                                            RootRef.updateChildren(messageBodyDetails);
                                            loadingBar.dismiss();
                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            }
            else if (checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

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

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/listMessage/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/listMessage/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Map messageBodyDetails = new HashMap();
                                        messageBodyDetails.put(messageSenderRef + "/stateUserSee", 1);
                                        messageBodyDetails.put(messageReceiverRef + "/stateUserSee", 0);
                                        RootRef.updateChildren(messageBodyDetails);
                                        loadingBar.dismiss();
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }


                    }
                });


            }
            else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                Date now = new Date();
                                String SDate1 = date + " " + time;
                                try {
                                    Date date1 = new SimpleDateFormat("dd/MM/yyyy hh:mm a").parse(SDate1);
                                    long x = (now.getTime() - date1.getTime()) / 60000;
                                    if(x<60) {
                                        x+=1;
                                        if(x==1) {
                                            userLastSeen.setText("active " + x + " minute ago");
                                        }
                                        else userLastSeen.setText("active " + x + " minutes ago");
                                    }
                                    else if(x>1440) {
                                        x=x/1440;
                                        if(x==1) {
                                            userLastSeen.setText("active " + x + " day ago");
                                        }
                                        else  userLastSeen.setText("active " + x + " days ago");

                                    }
                                    else {
                                        x=x/60;
                                        if(x==1) {
                                            userLastSeen.setText("active " + x + " hour ago");
                                        }
                                        else  userLastSeen.setText("active " + x + " hours ago");

                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void getMessengerList() {
        messagesList.clear();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("listMessage")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put("stateUserSee", 1);
                        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).updateChildren(messageBodyDetails);
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


    private void SendMessage() {
        String messageText = MessageInputText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText) || messageText.equals("")) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {
            MessageInputText.setText("");
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/listMessage/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/listMessage/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/stateUserSee", 1);
                        messageBodyDetails.put(messageReceiverRef + "/stateUserSee", 0);
                        RootRef.updateChildren(messageBodyDetails);
                    } else {
                        Toast.makeText(ChatActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //to place the call to the entered name
    private void callButtonClicked() {
        String userName = messageReceiverID;
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUser(userName);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }

    private void callVideoButtonClicked() {
        String userName = messageReceiverID;
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUserVideo(userName);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pCall:
                    callButtonClicked();
                    break;

                case R.id.vCall:
                    callVideoButtonClicked();
                    break;


            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void SendLocation() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
//                    String uri = "geo:" + latitude + ","
//                            + longitude + "?q=" + latitude
//                            + "," + longitude;

                    String msgContent = "{\"latitude\":" + latitude + "," + "\"longitude\":" + longitude + "}";

                    String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                    String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();
                    String messagePushID = userMessageKeyRef.getKey();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", msgContent);
                    messageTextBody.put("type", "location");
                    messageTextBody.put("from", messageSenderID);
                    messageTextBody.put("to", messageReceiverID);
                    messageTextBody.put("messageID", messagePushID);
                    messageTextBody.put("time", saveCurrentTime);
                    messageTextBody.put("date", saveCurrentDate);


                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/listMessage/" + messagePushID, messageTextBody);
                    messageBodyDetails.put(messageReceiverRef + "/listMessage/" + messagePushID, messageTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/stateUserSee", 1);
                                messageBodyDetails.put(messageReceiverRef + "/stateUserSee", 0);
                                RootRef.updateChildren(messageBodyDetails);
                            } else {
                                Toast.makeText(ChatActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                            }
                            MessageInputText.setText("");
                        }
                    });
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

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000000000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000000000, 0, locationListener);
        }

    }


    private void getStickerList() {
        stickers.clear();
        AssetManager mgr = getApplicationContext().getAssets();
        try {
            fileList = mgr.list("sticker");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, "Size: " + fileList.length, Toast.LENGTH_SHORT).show();

        for (int i = 0; i < fileList.length; i++) {
            Sticker sticker = new Sticker(fileList[i], "file:///android_asset/sticker/" + fileList[i]);
            stickers.add(sticker);
        }
    }

    @Override
    public void onBackPressed() {
        if (choose_sticker.getVisibility() == View.VISIBLE) {
            choose_sticker.setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ABOVE, R.id.chat_layout);
            params.addRule(RelativeLayout.BELOW, R.id.chat_toolbar);
            userMessagesList.setLayoutParams(params);
            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
        } else {
            super.onBackPressed();
        }
    }
}
