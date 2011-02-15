package se.jimlar.storage;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.HashMap;
import java.util.Map;

public class ContactsReader {
    private static final Logger LOG = new Logger(ContactsReader.class);

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
                                    new String[]{ContactsContract.RawContacts.CONTACT_ID,
                                                 ContactsContract.RawContacts.SOURCE_ID,
                                                 ContactsContract.RawContacts.SYNC1},
                                    ContactsContract.Groups.ACCOUNT_NAME + " = ? AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = ?",
                                    new String[]{account.name, account.type},
                                    null);

            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);
                long sourceId = cursor.getLong(1);
                String imageUrl = cursor.getString(2);

                Employee employee = loadStoredEmployee(contactId, sourceId, imageUrl);
                result.put(sourceId, new StoredContact(contactId, employee));
            }

            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Employee loadStoredEmployee(long contactId, long sourceId, String imageUrl) {
        String[] name = loadName(contactId);
        return new Employee(sourceId,
                            name[0],
                            name[1],
                            loadMobilePhone(contactId),
                            imageUrl,
                            loadSingleColumn(contactId, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
                            loadSingleColumn(contactId, ContactsContract.CommonDataKinds.Organization.TITLE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                            null,
                            -1);
    }

    private String[] loadName(long contactId) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                                 ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME},
                                    ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.CONTACT_ID + " = ?",
                                    new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, String.valueOf(contactId)},
                                    null);

            if (cursor.moveToNext()) {
                return new String[]{cursor.getString(0), cursor.getString(1)};
            } else {
                LOG.warn("Found no name for contact " + contactId);
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
            } else {
                LOG.warn("Found no data for column: " + column + ", mime: " + itemMimeType + ", contact: " + contactId);
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
            } else {
                LOG.warn("Found no phone number for contact: " + contactId);
            }
            return null;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
