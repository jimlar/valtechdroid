package se.valtech.androidsync.storage;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;

public class GroupStorage {
    private final ContentResolver resolver;
    private final Account account;

    public GroupStorage(ContentResolver resolver, Account account) {
        this.resolver = resolver;
        this.account = account;
    }

    public Long getOrCreateGroup() {
        Long groupId = getGroupId();

        if (groupId == null) {
            createGroup(account);
            groupId = getGroupId();
        }

        return groupId;
    }

    private void createGroup(Account account) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Groups.TITLE, "Valtech Intranet");
        values.put(ContactsContract.Groups.GROUP_VISIBLE, 1);
        values.put(ContactsContract.Groups.SHOULD_SYNC, 0);

        values.put(ContactsContract.Groups.ACCOUNT_NAME, account.name);
        values.put(ContactsContract.Groups.ACCOUNT_TYPE, ValtechProfile.ACCOUNT_TYPE);

        resolver.insert(ContactsContract.Groups.CONTENT_URI, values);
    }

    private Long getGroupId() {
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Groups.CONTENT_URI,
                                                        new String[]{ContactsContract.Groups._ID},
                                                        ContactsContract.Groups.ACCOUNT_TYPE + " = ? AND " + ContactsContract.Groups.GROUP_VISIBLE + " = 1",
                                                        new String[]{ValtechProfile.ACCOUNT_TYPE},
                                                        null);
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
