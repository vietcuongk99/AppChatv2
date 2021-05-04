package com.kdc.chatapp.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kdc.chatapp.Model.Contacts;
import com.kdc.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeleteUserOutGroup extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView listMember;
    private DatabaseReference UserRef, RootRef, MemberCurrentGroup;
    private String groupName;

    private FirebaseAuth mAuth;

    private String CurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user_out_group);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        RootRef = FirebaseDatabase.getInstance().getReference();
        groupName = getIntent().getExtras().get("groupName").toString();
        MemberCurrentGroup = RootRef.child("Groups").child(groupName).child("members");

        listMember = (RecyclerView) findViewById(R.id.listMember);
        listMember.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mToolbar = (Toolbar) findViewById(R.id.delete);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Delete Members");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMembers();
    }

    private void loadMembers() {
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(MemberCurrentGroup, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindMemberViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindMemberViewHolder>(options) {
            @NonNull
            @Override
            public FindMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_mem_radio_layout, parent, false);
                FindMemberViewHolder viewHolder = new FindMemberViewHolder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull FindMemberViewHolder holder, int position, @NonNull Contacts model) {
                String key = getRef(position).getKey();

                UserRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage).into(holder.profileImage);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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
        };

        listMember.setAdapter(adapter);
        adapter.startListening();
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
            deleteAction();
        }

        if (item.getItemId() == R.id.cancel) {
            onBackPressed();
        }

        return true;
    }

    private void deleteAction() {
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();

        RootRef.child("Groups").child(groupName).child("membersCache").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(CurrentUserId)) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        RootRef.child("Groups").child(groupName).child("members").child(snapshot.getKey().toString()).removeValue();
                    }

                    RootRef.child("Groups").child(groupName).child("members").orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                for (DataSnapshot supportItem: dataSnapshot.getChildren()) {
                                    String futureUID = supportItem.getKey();
                                    Map updatePosition = new HashMap();
                                    updatePosition.put("position", "admin");
                                    RootRef.child("Groups").child(groupName).child("members").child(futureUID).updateChildren(updatePosition);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    onBackPressed();

                    Intent settingsIntent = new Intent(DeleteUserOutGroup.this, MainActivity.class);;
                    startActivity(settingsIntent);

                }
                else {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        RootRef.child("Groups").child(groupName).child("members").child(snapshot.getKey().toString()).removeValue();
                    }
                    onBackPressed();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class FindMemberViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        CircleImageView profileImage;
        CheckBox checkBox;

        public FindMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            checkBox = itemView.findViewById(R.id.checkbox_delete);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RootRef.child("Groups").child(groupName).child("membersCache").removeValue();
    }
}
