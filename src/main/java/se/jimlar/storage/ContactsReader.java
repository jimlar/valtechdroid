package se.jimlar.storage;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.intranet.Employee;

import java.util.HashMap;
import java.util.Map;

public class ContactsReader {
    private final ContentResolver resolver;
    private final Account account;

    public ContactsReader(ContentResolver resolver, Account account) {
        this.resolver = resolver;
        this.account = account;
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

                Employee employee = loadStoredEmployee(contactId, sourceId, imageUrl);
                result.put(sourceId, new StoredContact(contactId, imageState, employee));
            }

            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Employee loadStoredEmployee(long contactId, long sourceId, String imageUrl) {
        String[] name = loadMultiColumn(contactId,
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);


        String[] status = loadMultiColumn(contactId,
                                          ContactsContract.StatusUpdates.CONTENT_ITEM_TYPE,
                                          ContactsContract.StatusUpdates.STATUS,
                                          ContactsContract.StatusUpdates.STATUS_TIMESTAMP);

        long statusTimeStamp = parseStatusTimeStamp(status[1]);

        return new Employee(sourceId,
                            name[0],
                            name[1],
                            loadMobilePhone(contactId),
                            imageUrl,
                            loadSingleColumn(contactId, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
                            loadSingleColumn(contactId, ContactsContract.CommonDataKinds.Organization.TITLE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                            status[0],
                            statusTimeStamp);
    }

    private long parseStatusTimeStamp(String status) {
        try {
            return Long.parseLong(status);
        } catch (NumberFormatException e) { }
        return 0;
    }

    private String[] loadMultiColumn(long contactId, String itemType, String... columns) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    columns,
                                    ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.CONTACT_ID + " = ?",
                                    new String[]{itemType, String.valueOf(contactId)},
                                    null);

            if (cursor.moveToNext()) {
                return new String[]{cursor.getString(0), cursor.getString(1)};
            }
            return new String[]{null, null};

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String loadSingleColumn(long contactId, String column, String itemMimeType) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    new String[]{column},
                                    ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.CONTACT_ID + " = ?",
                                    new String[]{itemMimeType, String.valueOf(contactId)},
                                    null);

            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
            return null;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String loadMobilePhone(long contactId) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                                    ContactsContract.Data.MIMETYPE + " = ? AND "
                                            + ContactsContract.Data.CONTACT_ID + " = ? AND "
                                            + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?",
                                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                                 String.valueOf(contactId),
                                                 String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)},
                                    null);

            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
            return null;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
