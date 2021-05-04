package com.kdc.chatapp.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kdc.chatapp.Fragments.ChatsFragment;
import com.kdc.chatapp.Fragments.ContactsFragment;
import com.kdc.chatapp.Fragments.GroupsFragment;
import com.kdc.chatapp.Fragments.RequestsFragment;

// class
public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter (FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int i) {
        // i = vị trí của Fragment
        switch (i) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
