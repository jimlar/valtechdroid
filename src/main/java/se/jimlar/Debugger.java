package se.jimlar;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Debugger {
    private static final Logger LOG = new Logger(Debugger.class);

    public static void dumpContactTables(ContentResolver contentResolver) {
        dumpTable(contentResolver, ContactsContract.Data.CONTENT_URI, null);
        dumpTable(contentResolver, ContactsContract.Groups.CONTENT_URI, null);
        dumpTable(contentResolver, ContactsContract.RawContacts.CONTENT_URI, null);
    }

    public static void dumpTable(ContentResolver contentResolver, Uri contentUri, String selection) {
        LOG.debug(contentUri.toString());
        LOG.debug("-------------------------");
        Cursor cursor = contentResolver.query(contentUri, null, selection, null, null);

        int columns = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            String data = "";
            for (int i = 0; i < columns; i++) {
                data += cursor.getColumnName(i) + ": " + getValueIgnoreErrors(cursor, i) + ", ";
            }
            LOG.debug(data);
        }
        cursor.close();
        LOG.debug("-------------------------");
    }

    private static String getValueIgnoreErrors(Cursor cursor, int i) {
        try {
            return cursor.getString(i);
        } catch (Exception e) {
            return "<unable to print>";
        }
    }
}
