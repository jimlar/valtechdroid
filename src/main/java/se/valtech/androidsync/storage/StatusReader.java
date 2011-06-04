package se.valtech.androidsync.storage;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusReader {
    private final ContentResolver contentResolver;

    public StatusReader(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
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
                                           new String[] { "se.valtech.androidsync" },
                                           ContactsContract.StatusUpdates.STATUS_TIMESTAMP + " DESC");

            while (cursor.moveToNext()) {
                statuses.add(new Status(cursor.getString(0), cursor.getString(1), new Date(cursor.getLong(2))));
                if (cursor.getPosition() >= num - 1) {
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

    public static class Status {
        public final String handle;
        public final String status;
        public final Date when;

        public Status(String handle, String status, Date when) {
            this.handle = handle;
            this.status = status;
            this.when = when;
        }

        @Override
        public String toString() {
            return handle + ": " + status;
        }
    }
}
