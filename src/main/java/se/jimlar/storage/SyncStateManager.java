package se.jimlar.storage;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;

import java.util.ArrayList;
import java.util.List;

public class SyncStateManager {
    private static final Logger LOG = new Logger(SyncStateManager.class);

    private final ContentResolver resolver;
    private final Account account;

    public SyncStateManager(ContentResolver resolver, Account account) {
        this.resolver = resolver;
        this.account = account;
    }

    public List<SyncState> getSyncStates() {

        ArrayList<SyncState> result = new ArrayList<SyncState>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                    new String[]{ContactsContract.RawContacts.CONTACT_ID,
                                                 ContactsContract.RawContacts.SYNC1,
                                                 ContactsContract.RawContacts.SYNC2},
                                    ContactsContract.Groups.ACCOUNT_NAME + " = ? AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                    new String[]{account.name, account.type},
                                    null);

            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);
                String imageUrl = cursor.getString(1);
                String imageState = cursor.getString(2);

                result.add(new SyncState(contactId, imageUrl, imageState));
            }

            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public void saveSyncState(SyncState syncState) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.SYNC1, syncState.getImageUrl());
        values.put(ContactsContract.RawContacts.SYNC2, syncState.getState());
        resolver.update(ContactsContract.RawContacts.CONTENT_URI,  values, ContactsContract.RawContacts.CONTACT_ID  + "=" + syncState.getContactId(), null);
    }
}
