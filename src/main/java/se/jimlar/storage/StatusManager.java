package se.jimlar.storage;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusManager {
    private static final Logger LOG = new Logger(StatusManager.class);
    private final ContentResolver resolver;

    public StatusManager(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public void syncStatuses(Map<Long, StoredContact> storedContacts, List<Employee> employees) {

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (Employee employee : employees) {
            StoredContact storedContact = storedContacts.get(employee.getUserId());

            /* Insert status */
            batch.add(ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI)
                              .withValues(statusValues(storedContact.getContactId(), employee))
                              .build());
        }

        try {
            LOG.debug("Committing batch");
            resolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception e) {
            LOG.warn("Could not insert photo", e);
        }
    }

    private ContentValues statusValues(long contactId, Employee employee) {
        ContentValues values = new ContentValues();

//        values.put(ContactsContract.StatusUpdates.DATA_ID, profileId); //A _ID of a data row containing the "profile"
        values.put(ContactsContract.StatusUpdates.STATUS, employee.getStatusMessage());
        values.put(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, employee.getStatusTimeStamp());
        values.put(ContactsContract.StatusUpdates.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM);
        values.put(ContactsContract.StatusUpdates.CUSTOM_PROTOCOL, "ValtechIntranet");
        values.put(ContactsContract.StatusUpdates.IM_ACCOUNT, employee.getEmail());
        values.put(ContactsContract.StatusUpdates.IM_HANDLE, employee.getUserId());
//        values.put(StatusUpdates.STATUS_RES_PACKAGE, context.getPackageName());
//        values.put(StatusUpdates.STATUS_ICON, R.drawable.icon);
//        values.put(StatusUpdates.STATUS_LABEL, R.string.label);

        return values;
    }

}
