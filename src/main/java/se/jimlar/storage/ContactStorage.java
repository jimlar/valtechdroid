package se.jimlar.storage;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SyncResult;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactStorage {
    private static final Logger LOG = new Logger(ContactStorage.class);

    private final ContentResolver resolver;
    private final Account account;
    private final Long groupId;

    public ContactStorage(ContentResolver resolver, Account account, Long groupId) {
        this.resolver = resolver;
        this.account = account;
        this.groupId = groupId;
    }

    public void syncEmployees(List<Employee> employees, SyncResult syncResult) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Map<Long, StoredContact> storedContacts = getStoredContacts();

        //Update existing ones
        for (Employee employee : employees) {
            if (storedContacts.containsKey(employee.getUserId())) {
                updateEmployee(employee, storedContacts.get(employee.getUserId()), batch);
                syncResult.stats.numUpdates++;
                syncResult.stats.numEntries++;
            }
        }

        //Insert missing ones
        for (Employee employee : employees) {
            if (!storedContacts.containsKey(employee.getUserId()) && employee.hasPhone()) {
                insertNewEmployee(employee, batch);
                syncResult.stats.numInserts++;
                syncResult.stats.numEntries++;
            }
        }

        //Delete removed ones
        for (StoredContact storedContact : storedContacts.values()) {
            if (!storedContact.presentIn(employees)) {
                delete(storedContact, batch);
                syncResult.stats.numDeletes++;
                syncResult.stats.numEntries++;
            }
        }

        try {
            LOG.debug("Committing batch");
            resolver.applyBatch(ContactsContract.AUTHORITY, batch);

        } catch (Exception e) {
            LOG.error("Exception encountered while running sync batch", e);
        }
    }

    public Map<Long, StoredContact> getStoredContacts() {
        Map<Long, StoredContact> result = new HashMap<Long, StoredContact>();

        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                    new String[]{ContactsContract.RawContacts._ID,
                                                 ContactsContract.RawContacts.SOURCE_ID,
                                                 ContactsContract.RawContacts.SYNC1,
                                                 ContactsContract.RawContacts.SYNC2},
                                    ContactsContract.Groups.ACCOUNT_NAME + " = ? AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                    new String[]{account.name, account.type},
                                    null);

            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);
                long sourceId = cursor.getLong(1);
                String imageUrl = cursor.getString(2);
                String imageState = cursor.getString(3);
                result.put(sourceId, new StoredContact(contactId, sourceId, imageUrl, imageState));
            }

            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void delete(StoredContact storedContact, List<ContentProviderOperation> batch) {
        LOG.debug("Deleting stored contact: " + storedContact.contactId);
        batch.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI.buildUpon()
                                                             .appendPath(String.valueOf(storedContact.contactId))
                                                             .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                                             .build()).build());
        batch.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                                                             .appendPath(String.valueOf(storedContact.contactId))
                                                             .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                                             .build()).build());
    }

    private void updateEmployee(Employee employee, StoredContact storedContact, List<ContentProviderOperation> batch) {
        LOG.debug("Updating employee: " + employee.getEmail());

        /* Insert contact data */
        batch.add(buildDataRemove(storedContact.contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(storedContact.contactId, nameValues(employee)));
        batch.add(buildDataRemove(storedContact.contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(storedContact.contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, employee.getMobilePhone())));
        batch.add(buildDataInsert(storedContact.contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, employee.getShortPhone())));
        batch.add(buildDataInsert(storedContact.contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, employee.getWorkPhone())));
        batch.add(buildDataRemove(storedContact.contactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(storedContact.contactId, emailValues(employee)));
    }

    private void insertNewEmployee(Employee employee, List<ContentProviderOperation> batch) {
        LOG.debug("Inserting employee: " + employee.getEmail());
        int index = batch.size();

        /* Insert raw contact entry */
        batch.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                          .withValue(ContactsContract.RawContacts.SOURCE_ID, employee.getUserId())
                          .withValue(ContactsContract.RawContacts.SYNC1, employee.getImageUrl())
                          .withValue(ContactsContract.RawContacts.SYNC2, "not_downloaded")
                          .build());

        /* Insert contact data */
        batch.add(buildDataInsertWithBackReference(index, nameValues(employee)));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, employee.getMobilePhone())));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, employee.getShortPhone())));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, employee.getWorkPhone())));
        batch.add(buildDataInsertWithBackReference(index, emailValues(employee)));

        /* Add to the group */
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                          .build());
    }

    private ContentValues nameValues(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, employee.getFirstName() + " " + employee.getLastName());
        return values;
    }

    private ContentValues phoneValues(int phoneType, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType);
        return values;
    }

    private ContentValues emailValues(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Email.DATA, employee.getEmail());
        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        return values;
    }

    private ContentProviderOperation buildDataInsertWithBackReference(int contactInsertIndex, ContentValues values) {
        return ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactInsertIndex)
                .withValues(values)
                .build();
    }

    private ContentProviderOperation buildDataInsert(long contactId, ContentValues values) {
        return ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                .withValues(values)
                .build();
    }

    private ContentProviderOperation buildDataRemove(long contactId, String mimeType) {
        return ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                               new String[]{String.valueOf(contactId), mimeType})
                .build();
    }
}
