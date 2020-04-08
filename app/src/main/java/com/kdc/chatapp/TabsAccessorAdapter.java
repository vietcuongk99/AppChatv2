package com.kdc.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    // đặt tiêu đề cho từng Fragment
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";

            case 1:
                return "Group";

            case 2:
                return "Contacts";

            default:
                return null;
        }
    }
}
