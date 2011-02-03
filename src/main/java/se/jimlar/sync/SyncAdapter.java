package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.R;
import se.jimlar.intranet.APIClient;
import se.jimlar.intranet.APIResponseParser;
import se.jimlar.intranet.Employee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final Logger LOG = new Logger(SyncAdapter.class);
    private Context context;
    private AccountManager accountManager;

    public SyncAdapter(Context context) {
        super(context, true);
        this.context = context;
        accountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        LOG.debug("Starting sync");
        try {
            String authToken = accountManager.blockingGetAuthToken(account, context.getString(R.string.ACCOUNT_TYPE), true);
            APIClient client = new APIClient(account.name, authToken, new APIResponseParser());
            List<Employee> employees = client.getEmployees();
            storeEmployees(employees, account, client);

            LOG.debug("Sync done");

        } catch (Exception e) {
            LOG.warn("Sync failed", e);
        }
    }

    private void storeEmployees(List<Employee> employees, Account account, APIClient client) {
        deleteAllContactsAndGroups();

        Long groupId = getOrCreateGroup(account);

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        int i = 0;
        for (Employee employee : employees) {
            LOG.debug("Found employee: " + employee.getEmail());

//            i++;
//            if (i > 10) {
//                LOG.debug("Breaking import");
//                break;
//            }

            String phone = employee.getMobilePhone();
            if (phone == null) {
                LOG.debug("No phone number for employee, skipped sync");

            } else {
                storeEmployee(employee, groupId, account, batch);
            }
        }

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception e) {
            LOG.error("Exception encountered while running sync batch", e);
        }


        insertPhotos(account, client);


//        dumpTable(ContactsContract.Data.CONTENT_URI, null);
//        dumpTable(ContactsContract.Groups.CONTENT_URI, null);
//        dumpTable(ContactsContract.RawContacts.CONTENT_URI, null);
    }

    private void insertPhotos(Account account, APIClient client) {

        //
        // Seems like most people use a second (async) pass to download the photos
        // - How to stream it: http://developer.android.com/reference/android/content/ContentResolver.html#openOutputStream(android.net.Uri)
        // - Batch example: http://efreedom.com/Question/1-3234386/Android-Batch-Insert-Contact-Photo
        // - Store progress/state in the sync metadata: http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Photo.html
        //
        //

        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
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

                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            } catch (Exception e) {
                LOG.warn("Could not insert photo", e);
            }
        }
    }

    private Long getOrCreateGroup(Account account) {
        Long groupId = getGroupId(account);

        if (groupId == null) {
            createGroup(account);
            groupId = getGroupId(account);
        }

        return groupId;
    }

    private void createGroup(Account account) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Groups.TITLE, "Valtech Intranet");
        values.put(ContactsContract.Groups.GROUP_VISIBLE, 1);
        values.put(ContactsContract.Groups.SHOULD_SYNC, 0);

        values.put(ContactsContract.Groups.ACCOUNT_NAME, account.name);
        values.put(ContactsContract.Groups.ACCOUNT_TYPE, account.type);

        context.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, values);
    }

    private Long getGroupId(Account account) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                                                        new String[]{ContactsContract.Groups._ID},
                                                        ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "
                                                                + ContactsContract.Groups.ACCOUNT_TYPE + " = ? AND "
                                                                + ContactsContract.Groups.GROUP_VISIBLE + " = 1",
                                                        new String[]{account.name, account.type},
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

    private void storeEmployee(Employee employee, Long groupId, Account account, ArrayList<ContentProviderOperation> batch) {
        int index = batch.size();

        batch.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                          .withValue(ContactsContract.RawContacts.SYNC1, employee.getImageUrl())
                          .withValue(ContactsContract.RawContacts.SYNC2, "not_downloaded")
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, employee.getFirstName() + " " + employee.getLastName())
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getMobilePhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getShortPhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getWorkPhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Email.DATA, employee.getEmail())
                          .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                          .build());

        /* Add to the group */
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                          .build());
    }

    private void deleteAllContactsAndGroups() {
        context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build(), null, null);
        context.getContentResolver().delete(ContactsContract.Groups.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build(), null, null);
    }

    private void dumpTable(Uri contentUri, String selection) {
        LOG.debug(contentUri.toString());
        LOG.debug("-------------------------");
        Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, null);

        int columns = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            String data = "";
            for (int i = 0; i < columns; i++) {
                data += cursor.getColumnName(i) + ": " + cursor.getString(i) + ", ";
            }
            LOG.debug(data);
        }
        cursor.close();
        LOG.debug("-------------------------");
    }
}
