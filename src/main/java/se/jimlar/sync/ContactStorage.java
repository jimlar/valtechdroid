package se.jimlar.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.*;

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

    public void syncEmployees(List<Employee> employees) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Map<Long, StoredContact> storedContacts = getStoredContacts();

        //Update existing ones
        for (Employee employee : employees) {
            if (storedContacts.containsKey(employee.getUserId())) {
                updateEmployee(employee, batch);
            }
        }

        //Insert missing ones
        for (Employee employee : employees) {
            if (!storedContacts.containsKey(employee.getUserId()) && employee.hasPhone()) {
                insertNewEmployee(employee, batch);
            }
        }

        //Delete removed ones
        for (StoredContact storedContact : storedContacts.values()) {
            if (!storedContact.presentIn(employees)) {
                delete(storedContact, batch);
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
        batch.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                                                             .appendPath(String.valueOf(storedContact.contactId))
                                                             .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                                             .build()).build());
    }

    private void updateEmployee(Employee employee, List<ContentProviderOperation> batch) {
        LOG.debug("Updating employee: " + employee.getEmail());
    }

    private void insertNewEmployee(Employee employee, List<ContentProviderOperation> batch) {
        LOG.debug("Inserting employee: " + employee.getEmail());
        int index = batch.size();

        batch.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                          .withValue(ContactsContract.RawContacts.SOURCE_ID, employee.getUserId())
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
}
