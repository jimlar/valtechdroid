package se.jimlar.storage;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SyncResult;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsWriter {
    private static final Logger LOG = new Logger(ContactsWriter.class);
    private static final int MAX_BATCH_SIZE = 450;

    private final ContentResolver resolver;
    private final Account account;
    private final Long groupId;

    public ContactsWriter(ContentResolver resolver, Account account, Long groupId) {
        this.resolver = resolver;
        this.account = account;
        this.groupId = groupId;
    }

    public void updateStoredContacts(Map<Long, StoredContact> storedContacts, List<Employee> employees, SyncResult syncResult) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        //Update existing ones
        for (Employee employee : employees) {
            StoredContact storedContact = storedContacts.get(employee.getUserId());
            if (storedContact != null && storedContact.needsUpdate(employee)) {
                updateEmployee(employee, storedContact, batch);
                syncResult.stats.numUpdates++;
                syncResult.stats.numEntries++;
                if (batch.size() >= MAX_BATCH_SIZE) {
                    applyBatch(batch);
                }
            }
        }

        //Insert missing ones
        for (Employee employee : employees) {
            if (!storedContacts.containsKey(employee.getUserId()) && employee.hasPhone()) {
                insertNewEmployee(employee, batch);
                syncResult.stats.numInserts++;
                syncResult.stats.numEntries++;
                if (batch.size() >= MAX_BATCH_SIZE) {
                    applyBatch(batch);
                }
            }
        }

        //Delete removed ones
        for (StoredContact storedContact : storedContacts.values()) {
            if (!storedContact.presentIn(employees)) {
                delete(storedContact, batch);
                syncResult.stats.numDeletes++;
                syncResult.stats.numEntries++;
                if (batch.size() >= MAX_BATCH_SIZE) {
                    applyBatch(batch);
                }
            }
        }

        applyBatch(batch);
    }

    private void applyBatch(ArrayList<ContentProviderOperation> batch) {
        try {
            LOG.debug("Committing batch");
            resolver.applyBatch(ContactsContract.AUTHORITY, batch);

        } catch (Exception e) {
            LOG.error("Exception encountered while running sync batch", e);
        }
        batch.clear();
    }

    private void delete(StoredContact storedContact, List<ContentProviderOperation> batch) {
        long contactId = storedContact.getContactId();
        LOG.debug("Deleting stored contact: " + contactId);
        batch.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI.buildUpon()
                                                             .appendPath(String.valueOf(contactId))
                                                             .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                                             .build()).build());
        batch.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                                                             .appendPath(String.valueOf(contactId))
                                                             .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                                             .build()).build());
    }

    private void updateEmployee(Employee employee, StoredContact storedContact, List<ContentProviderOperation> batch) {
        LOG.debug("Updating employee: " + employee.getEmail());

        LOG.debug("Old employee: " + storedContact.getEmployee());
        LOG.debug("New employee: " + employee);

        /* Insert contact data */
        long contactId = storedContact.getContactId();
        batch.add(buildDataRemove(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(contactId, nameValues(employee)));
        batch.add(buildDataRemove(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(contactId, organizationValues(employee)));
        batch.add(buildDataRemove(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, employee.getMobilePhone())));
        batch.add(buildDataInsert(contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, employee.getShortPhone())));
        batch.add(buildDataInsert(contactId, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, employee.getWorkPhone())));
        batch.add(buildDataRemove(contactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE));
        batch.add(buildDataInsert(contactId, emailValues(employee)));
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
                          .withValue(ContactsContract.RawContacts.SYNC2, PhotoState.NOT_DOWNLOADED.name())
                          .build());

        /* Insert contact data */
        batch.add(buildDataInsertWithBackReference(index, nameValues(employee)));
        batch.add(buildDataInsertWithBackReference(index, organizationValues(employee)));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, employee.getMobilePhone())));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, employee.getShortPhone())));
        batch.add(buildDataInsertWithBackReference(index, phoneValues(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, employee.getWorkPhone())));
        batch.add(buildDataInsertWithBackReference(index, emailValues(employee)));

        batch.add(buildDataInsertWithBackReference(index, profileValues(employee)));

        /* Add to the group */
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                          .build());
    }

    private ContentValues profileValues(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ValtechProfile.CONTENT_ITEM_TYPE);
        values.put(ValtechProfile.PROFILE_ID, employee.getUserId());
        return values;
    }

    private ContentValues organizationValues(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Organization.COMPANY, "Valtech AB");
        return values;
    }

    private ContentValues nameValues(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, employee.getFirstName() + " " + employee.getLastName());
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, employee.getFirstName());
        values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, employee.getLastName());
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
