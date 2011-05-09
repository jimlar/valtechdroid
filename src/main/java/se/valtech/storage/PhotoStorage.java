package se.valtech.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;
import se.valtech.Logger;
import se.valtech.intranet.APIClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PhotoStorage {
    private static final Logger LOG = new Logger(PhotoStorage.class);

    private final APIClient client;
    private final ContentResolver resolver;
    private final SyncStateManager syncStateManager;

    public PhotoStorage(ContentResolver resolver, APIClient client, SyncStateManager syncStateManager) {
        this.client = client;
        this.resolver = resolver;
        this.syncStateManager = syncStateManager;
    }

    public void syncPhotos() {
        LOG.debug("Reading photo states");
        for (SyncState syncState : syncStateManager.getSyncStates()) {
            try {
                switch (syncState.getPhotoState()) {
                    case NOT_DOWNLOADED:
                        downloadAndInsertImage(syncState);
                        syncStateManager.saveSyncState(syncState.newPhotoState(PhotoState.DOWNLOADED));
                        break;
                }

            } catch (Exception e) {
                LOG.warn("Could not insert photo", e);
            }
        }
    }

    private void downloadAndInsertImage(SyncState syncState) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.download(syncState.getPhotoUrl(), out);

        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, syncState.getContactId());
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, out.toByteArray());

        resolver.insert(ContactsContract.Data.CONTENT_URI, values);
    }
}
