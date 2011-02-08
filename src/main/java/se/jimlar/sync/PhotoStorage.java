package se.jimlar.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.APIClient;

import java.io.ByteArrayOutputStream;

public class PhotoStorage {
    private static final Logger LOG = new Logger(PhotoStorage.class);

    private final Account account;
    private final APIClient client;
    private final ContentResolver resolver;

    public PhotoStorage(ContentResolver resolver, Account account, APIClient client) {
        this.account = account;
        this.client = client;
        this.resolver = resolver;
    }

    public void syncPhotos() {

        //
        // Seems like most people use a second (async) pass to download the photos
        // - How to stream it: http://developer.android.com/reference/android/content/ContentResolver.html#openOutputStream(android.net.Uri)
        // - Batch example: http://efreedom.com/Question/1-3234386/Android-Batch-Insert-Contact-Photo
        // - Store progress/state in the sync metadata: http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Photo.html
        //
        //

        Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                       new String[]{ContactsContract.RawContacts._ID, ContactsContract.RawContacts.SYNC1},
                                       ContactsContract.Groups.ACCOUNT_NAME + " = ? AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                       new String[]{account.name, account.type},
                                       null);

        while (cursor.moveToNext()) {
            long contactId = cursor.getLong(0);
            String imageUrl = cursor.getString(1);

            //TODO: remove this when the thumbnails are in the API properly
            int i = imageUrl.lastIndexOf('.');
            imageUrl = imageUrl.substring(0, i) + ".thumbnail" + imageUrl.substring(i);

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                client.download(imageUrl, out);

                ContentValues values = new ContentValues();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, contactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, out.toByteArray());

                resolver.insert(ContactsContract.Data.CONTENT_URI, values);

            } catch (Exception e) {
                LOG.warn("Could not insert photo", e);
            }
        }
    }
}
