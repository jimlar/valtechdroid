package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.os.Bundle;
import se.jimlar.Logger;
import se.jimlar.R;
import se.jimlar.intranet.APIClient;
import se.jimlar.intranet.APIResponseParser;
import se.jimlar.intranet.Employee;
import se.jimlar.storage.ContactStorage;
import se.jimlar.storage.GroupStorage;
import se.jimlar.storage.PhotoStorage;

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

            ContentResolver resolver = context.getContentResolver();

            GroupStorage groupStorage = new GroupStorage(resolver, account);
            Long groupId = groupStorage.getOrCreateGroup();

            ContactStorage contactStorage = new ContactStorage(resolver, account, groupId);

// This will delete all contacts
//            contactStorage.syncEmployees(Collections.<Employee>emptyList());

            contactStorage.syncEmployees(employees);

            PhotoStorage photoStorage = new PhotoStorage(resolver, client);
            photoStorage.syncPhotos(contactStorage.getStoredContacts().values());

//            Debugger.dumpContactTables(context.getContentResolver());

            LOG.debug("Sync done");
            syncResult.stats.

        } catch (Exception e) {
            LOG.warn("Sync failed", e);
        }
    }
}
