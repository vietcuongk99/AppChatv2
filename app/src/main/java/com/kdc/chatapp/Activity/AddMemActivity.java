package com.kdc.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kdc.chatapp.Model.Contacts;
import com.kdc.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddMemActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList, FriendsListToAdd;
    private DatabaseReference UserRef, RootRef;
    private EditText userName;

    private String groupName;

    private FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter;
    private FirebaseRecyclerAdapter<Contacts, FriendListViewHolder> adapterFriend;

    private FirebaseAuth mAuth;

    private String UserId;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mem);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        RootRef = FirebaseDatabase.getInstance().getReference();
        groupName = getIntent().getExtras().get("groupName").toString();

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.listMember);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        userName = (EditText) findViewById(R.id.friend_name);

        mToolbar = (Toolbar) findViewById(R.id.add_members);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add members");


    }

    @Override
    protected void onStart() {
        super.onStart();
        searchFriend("");
        addMemToList();

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String friendName = standardInputText(userName.getText().toString());
                searchFriend(friendName);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.member_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.add_option);
        View view = menuItem.getActionView();
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

        @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.add_option) {
            RootRef.child("Groups").child(groupName).child("membersCache").push();

            RootRef.child("Groups").child(groupName).child("membersCache").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    HashMap<String, Object> AddUser = new HashMap();

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        AddUser.put(snapshot.getKey(), snapshot.getValue());
                        RootRef.child("Groups").child(groupName).child("members").updateChildren(AddUser);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            onBackPressed();
        }

        if (item.getItemId() == R.id.cancel) {
            onBackPressed();
        }

        return true;
    }

    private String standardInputText(String name) {
        if(name.equals("")) return "";
        name = name.trim();
        name = name.replaceAll("\\s+", " ");
        String temp[] = name.split(" ");
        String nameStandard="";
        for (int i = 0; i < temp.length; i++) {
            nameStandard += String.valueOf(temp[i].charAt(0)).toUpperCase() + temp[i].substring(1);
            if (i < temp.length - 1)
                nameStandard += " ";
        }
        return nameStandard;
    }

    private void searchFriend(String text) {
        FirebaseRecyclerOptions<Contacts> options;
        mAuth = FirebaseAuth.getInstance();
        UserId = mAuth.getCurrentUser().getUid();
        if(!text.equals("")) {
            Query firebaseSearchQuery = UserRef.orderByChild("name").startAt(text).endAt(text + "\uf8ff");

            options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(firebaseSearchQuery, Contacts.class)
                            .setLifecycleOwner(this)
                            .build();
        } else {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(UserRef, Contacts.class)
                    .build();
        }

        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.userName.setText(model.getName());

                if (model.getImage() != null) {
                    Picasso.get().load(model.getImage()).into(holder.profileImage);
                }

                String user_id = getRef(position).getKey();

                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked==true) {
                            RootRef.child("Groups").child(groupName).push();
                            Map DeleteUser = new HashMap();
                            DeleteUser.put("position", "member");
                            RootRef.child("Groups").child(groupName).child("membersCache").child(user_id).updateChildren(DeleteUser);
                        }
                        else RootRef.child("Groups").child(groupName).child("membersCache").child(user_id).removeValue();
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_mem_radio_layout, parent, false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                return viewHolder;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }



    private void addMemToList() {

    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        CircleImageView profileImage;
        CheckBox checkBox;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            checkBox = itemView.findViewById(R.id.checkbox_delete);

        }
    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;

        public FriendListViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_image);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RootRef.child("Groups").child(groupName).child("membersCache").removeValue();
    }
}
