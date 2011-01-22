package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import se.jimlar.R;
import se.jimlar.intranet.APIClient;
import se.jimlar.intranet.APIResponseParser;
import se.jimlar.intranet.Employee;

import java.util.ArrayList;
import java.util.List;

class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getName();
    private Context context;
    private AccountManager accountManager;

    public SyncAdapter(Context context) {
        super(context, true);
        this.context = context;
        accountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            String authToken = accountManager.blockingGetAuthToken(account, context.getString(R.string.ACCOUNT_TYPE), true);
            APIClient client = new APIClient(account.name, authToken, new APIResponseParser());
            List<Employee> employees = client.getEmployees();
            storeEmployees(employees);

        } catch (Exception e) {
            Log.w(LOG_TAG, "Sync failed", e);
        }
    }

    private void storeEmployees(List<Employee> employees) {
        deleteAllContactsAndGroups();

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        int i = 0;
        for (Employee employee : employees) {
            Log.d(LOG_TAG, "Found employee: " + employee.getEmail());

            i++;
            if (i > 20) {
                Log.d(LOG_TAG, "Breaking import");
                break;
            }

            String phone = employee.getMobilePhone();
            if (phone == null) {
                Log.d(LOG_TAG, "No phone number for employee, skipped sync");

            } else {
                storeEmployee(employee, batch);
            }
        }

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception encountered while running sync batch: " + e);
        }

    }

    private void storeEmployee(Employee employee, ArrayList<ContentProviderOperation> batch) {
        int index = batch.size();

        batch.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) //This weirdness make the contact visible
                          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null) //This weirdness make the contact visible
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, employee.getFirstName() + " " + employee.getLastName())
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getMobilePhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getShortPhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.getWorkPhone())
                          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                          .build());
        batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, index)
                          .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                          .withValue(ContactsContract.CommonDataKinds.Email.DATA, employee.getEmail())
                          .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                          .build());

        //TODO: store the contact image
        //
        // Seems like most people use a second (async) pass to download the photos
        // - How to stream it: http://developer.android.com/reference/android/content/ContentResolver.html#openOutputStream(android.net.Uri)
        // - Batch example: http://efreedom.com/Question/1-3234386/Android-Batch-Insert-Contact-Photo
        // - Store progress/state in the sync metadata: http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Photo.html
        //
        

        Log.d(LOG_TAG, "Image Url: " + employee.getImageUrl());

    }

    private void deleteAllContactsAndGroups() {
        context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build(), null, null);
        context.getContentResolver().delete(ContactsContract.Groups.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build(), null, null);
    }
}
