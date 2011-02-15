package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.os.Bundle;
import android.provider.ContactsContract;
import se.jimlar.Debugger;
import se.jimlar.Logger;
import se.jimlar.R;
import se.jimlar.intranet.APIClient;
import se.jimlar.intranet.APIResponseParser;
import se.jimlar.intranet.Employee;
import se.jimlar.storage.*;

import java.util.List;
import java.util.Map;

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

            LOG.debug("Downloading intranet data");
            List<Employee> employees = client.getEmployees().subList(0, 20);

            ContentResolver resolver = context.getContentResolver();

            GroupStorage groupStorage = new GroupStorage(resolver, account);

            LOG.debug("Fetching/creating the sync adapter group");
            Long groupId = groupStorage.getOrCreateGroup();

            ContactsReader reader = new ContactsReader(resolver, account);
            ContactsWriter writer = new ContactsWriter(resolver, account, groupId);

            LOG.debug("Reading stored contacts");
            Map<Long, StoredContact> storedContacts = reader.getStoredContacts();

            LOG.debug("Updating stored contacts");
            writer.updateStoredContacts(storedContacts, employees, syncResult);

            SyncStateManager syncStateManager = new SyncStateManager(resolver, account);
            LOG.debug("Updating statuses");
            StatusManager statusManager = new StatusManager(resolver, syncStateManager);
            statusManager.syncStatuses(employees);

            LOG.debug("Updating stored contact photos");
            PhotoStorage photoStorage = new PhotoStorage(resolver, client, syncStateManager);
            photoStorage.syncPhotos();

//            Debugger.dumpTable(context.getContentResolver(), ContactsContract.StatusUpdates.CONTENT_URI);

            LOG.debug("Sync done");

        } catch (Exception e) {
            LOG.warn("Sync failed", e);
        }
    }
}
