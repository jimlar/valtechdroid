package se.valtech.androidsync.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class SyncStateManager {
    private final ContentResolver resolver;

    public SyncStateManager(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public List<SyncState> getSyncStates() {

        ArrayList<SyncState> result = new ArrayList<SyncState>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                    new String[]{ContactsContract.RawContacts._ID,
                                                 ContactsContract.RawContacts.SOURCE_ID,
                                                 ContactsContract.RawContacts.SYNC1,
                                                 ContactsContract.RawContacts.SYNC2,
                                                 ContactsContract.RawContacts.SYNC3},
                                    ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                    new String[]{ValtechProfile.ACCOUNT_TYPE},
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
        resolver.update(ContactsContract.RawContacts.CONTENT_URI, values, ContactsContract.RawContacts._ID + "=" + syncState.getContactId(), null);
    }
}
