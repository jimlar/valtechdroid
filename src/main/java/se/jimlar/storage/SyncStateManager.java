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
                                                 ContactsContract.RawContacts.SOURCE_ID,
                                                 ContactsContract.RawContacts.SYNC1,
                                                 ContactsContract.RawContacts.SYNC2,
                                                 ContactsContract.RawContacts.SYNC3},
                                    ContactsContract.Groups.ACCOUNT_NAME + " = ? AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                    new String[]{account.name, account.type},
                                    null);

            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);
                long sourceId = cursor.getLong(1);
                String imageUrl = cursor.getString(2);
                String imageState = cursor.getString(3);
                long lastStatusUpdate = cursor.getLong(4);

                result.add(new SyncState(contactId, sourceId, imageUrl, getPhotoState(imageState), lastStatusUpdate));
            }

            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private PhotoState getPhotoState(String imageState) {
        try {
            return PhotoState.valueOf(imageState);
        } catch (IllegalArgumentException e) {
            return PhotoState.NOT_DOWNLOADED;
        }
    }

    public void saveSyncState(SyncState syncState) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.SYNC1, syncState.getPhotoUrl());
        values.put(ContactsContract.RawContacts.SYNC2, syncState.getPhotoState().name());
        values.put(ContactsContract.RawContacts.SYNC3, syncState.getLastStatusUpdate());
        resolver.update(ContactsContract.RawContacts.CONTENT_URI,  values, ContactsContract.RawContacts.CONTACT_ID  + "=" + syncState.getContactId(), null);
    }
}
