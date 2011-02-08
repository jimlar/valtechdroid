package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.R;
import se.jimlar.intranet.APIClient;
import se.jimlar.intranet.APIResponseParser;
import se.jimlar.intranet.Employee;

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

            deleteAllContactsAndGroups();

            ContentResolver resolver = context.getContentResolver();

            Long groupId = new GroupStorage(resolver, account).getOrCreateGroup();

            ContactStorage contactStorage = new ContactStorage(resolver, account, groupId);
            contactStorage.syncEmployees(employees);

            new PhotoStorage(resolver, account, client).syncPhotos();

//        dumpTable(ContactsContract.Data.CONTENT_URI, null);
//        dumpTable(ContactsContract.Groups.CONTENT_URI, null);
//        dumpTable(ContactsContract.RawContacts.CONTENT_URI, null);

            LOG.debug("Sync done");

        } catch (Exception e) {
            LOG.warn("Sync failed", e);
        }
    }

    private void deleteAllContactsAndGroups() {
        LOG.warn("Removing all existing contacts and groups");
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
