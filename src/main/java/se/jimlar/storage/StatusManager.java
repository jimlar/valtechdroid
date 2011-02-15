package se.jimlar.storage;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;
import se.jimlar.Logger;
import se.jimlar.intranet.Employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusManager {
    private static final Logger LOG = new Logger(StatusManager.class);
    private final ContentResolver resolver;
    private final SyncStateManager syncStateManager;

    public StatusManager(ContentResolver resolver, SyncStateManager syncStateManager) {
        this.resolver = resolver;
        this.syncStateManager = syncStateManager;
    }

    public void syncStatuses(List<Employee> employees) {

        Map<Long, SyncState> syncStateByUserId = new HashMap<Long, SyncState>();
        for (SyncState state : syncStateManager.getSyncStates()) {
            syncStateByUserId.put(state.getSourceId(), state);
        }

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (Employee employee : employees) {

            SyncState state = syncStateByUserId.get(employee.getUserId());
            if (state.getLastStatusUpdate() != employee.getStatusTimeStamp()) {

                LOG.debug("Inserting new status for " + employee.getEmail());

                long profileId = lookupProfileDataId(employee);
                if (profileId > 0) {
                    batch.add(ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI)
                                      .withValues(statusValues(profileId, employee))
                                      .build());
                    syncStateManager.saveSyncState(state.newStatusUpdateTimeStamp(employee.getStatusTimeStamp()));
                } else {
                    LOG.warn("Found no profile data for " + employee.getEmail());
                }
            } else {
                LOG.debug("Status update not needed for " + employee.getEmail());
            }
        }

        try {
            LOG.debug("Committing batch");
            resolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception e) {
            LOG.warn("Could not insert photo", e);
        }
    }

    private long lookupProfileDataId(Employee employee) {

        Cursor cursor = null;
        try {
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    new String[]{ContactsContract.Data._ID},
                                    ContactsContract.Data.MIMETYPE + "='" + ValtechProfile.CONTENT_ITEM_TYPE + "' AND " + ValtechProfile.PROFILE_ID + "=?",
                                    new String[]{String.valueOf(employee.getUserId())},
                                    null);
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    private ContentValues statusValues(long dataId, Employee employee) {
        ContentValues values = new ContentValues();

        values.put(ContactsContract.StatusUpdates.DATA_ID, dataId);
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
