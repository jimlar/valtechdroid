package se.jimlar.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.APIClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

public class PhotoStorage {
    private static final Logger LOG = new Logger(PhotoStorage.class);

    private final APIClient client;
    private final ContentResolver resolver;

    public PhotoStorage(ContentResolver resolver, APIClient client) {
        this.client = client;
        this.resolver = resolver;
    }

    public void syncPhotos(Collection<StoredContact> storedContacts) {

        //
        // Seems like most people use a second (async) pass to download the photos
        // - How to stream it: http://developer.android.com/reference/android/content/ContentResolver.html#openOutputStream(android.net.Uri)
        // - Batch example: http://efreedom.com/Question/1-3234386/Android-Batch-Insert-Contact-Photo
        // - Store progress/state in the sync metadata: http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Photo.html
        //
        //
        for (StoredContact storedContact : storedContacts) {
            try {
                if ("not_downloaded".equals(storedContact.imageState)) {
                    downloadAndInsertImage(storedContact);
                    markImageDownloaded(storedContact);
                }

            } catch (Exception e) {
                LOG.warn("Could not insert photo", e);
            }
        }
    }

    private void downloadAndInsertImage(StoredContact storedContact) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.download(storedContact.imageUrl, out);

        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, storedContact.contactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, out.toByteArray());

        resolver.insert(ContactsContract.Data.CONTENT_URI, values);
    }

    private void markImageDownloaded(StoredContact storedContact) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.SYNC2, "downloaded");
        resolver.update(ContactsContract.RawContacts.CONTENT_URI,  values, ContactsContract.RawContacts._ID  + "=" + storedContact.contactId, null);
    }
}
