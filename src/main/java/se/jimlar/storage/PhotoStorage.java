package se.jimlar.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.APIClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PhotoStorage {
    private static final Logger LOG = new Logger(PhotoStorage.class);

    private final APIClient client;
    private final ContentResolver resolver;
    private final ContactsReader reader;

    public PhotoStorage(ContentResolver resolver, APIClient client, ContactsReader reader) {
        this.client = client;
        this.resolver = resolver;
        this.reader = reader;
    }

    public void syncPhotos() {
        LOG.debug("Reading photo states");
        for (ImageState imageState : reader.getImageStates()) {
            try {
                if ("not_downloaded".equals(imageState.getState())) {
                    downloadAndInsertImage(imageState);
                    markImageDownloaded(imageState);
                }

            } catch (Exception e) {
                LOG.warn("Could not insert photo", e);
            }
        }
    }

    private void downloadAndInsertImage(ImageState imageState) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.download(imageState.getImageUrl(), out);

        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, imageState.getContactId());
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, out.toByteArray());

        resolver.insert(ContactsContract.Data.CONTENT_URI, values);
    }

    private void markImageDownloaded(ImageState imageState) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.SYNC2, "downloaded");
        resolver.update(ContactsContract.RawContacts.CONTENT_URI,  values, ContactsContract.RawContacts.CONTACT_ID  + "=" + imageState.getContactId(), null);
    }
}
