package se.valtech.androidsync.storage;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.valtech.androidsync.intranet.Employee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusReader {
    private final ContentResolver contentResolver;
    private final ContactsReader contactsReader;

    public StatusReader(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.contactsReader = new ContactsReader(contentResolver);
    }

    public List<Status> getLatestStatuses(int num) {
        List<Status> statuses = new ArrayList<Status>();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ContactsContract.StatusUpdates.CONTENT_URI,
                                           new String[]{ContactsContract.StatusUpdates.DATA_ID,
                                                        ContactsContract.StatusUpdates.STATUS,
                                                        ContactsContract.StatusUpdates.STATUS_TIMESTAMP},
                                           ContactsContract.StatusUpdates.STATUS_RES_PACKAGE + " = ?",
                                           new String[]{"se.valtech.androidsync"},
                                           ContactsContract.StatusUpdates.STATUS_TIMESTAMP + " DESC");

            while (cursor.moveToNext()) {
                long dataId = cursor.getLong(0);
                Employee employee = lookupEmployee(dataId);
                String status = cursor.getString(1);
                Date date = new Date(cursor.getLong(2));
                if (employee != null && status != null && !"".equals(status.trim())) {
                    statuses.add(new Status(employee.getName(), status, date));
                }
                if (statuses.size() >= num) {
                    break;
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return statuses;
    }

    private Employee lookupEmployee(long dataId) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                                           new String[]{ContactsContract.Data.RAW_CONTACT_ID},
                                           ContactsContract.Data._ID + "=?",
                                           new String[]{String.valueOf(dataId)},
                                           null);
            if (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(0);
                return contactsReader.getStoredEmployee(rawContactId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    public static class Status implements Serializable {
        public final String employeeName;
        public final String text;
        public final Date when;

        public Status(String employeeName, String text, Date when) {
            this.employeeName = employeeName;
            this.text = text;
            this.when = when;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
