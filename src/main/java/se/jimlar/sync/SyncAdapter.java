package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.Contacts;
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
        Log.d(LOG_TAG, "User: " + account.name);

        try {
            String authToken = accountManager.blockingGetAuthToken(account, context.getString(R.string.ACCOUNT_TYPE), true);
            Log.d(LOG_TAG, "Pass: " + authToken);
            APIClient client = new APIClient(account.name, authToken, new APIResponseParser());
            List<Employee> employees = client.getEmployees();
            storeEmployees(employees);

        } catch (Exception e) {
            Log.w(LOG_TAG, "Sync failed", e);
        }
    }

    private void storeEmployees(List<Employee> employees) {

        deleteAllContacts();

        int i = 0;
        for (Employee employee : employees) {
            Log.d(LOG_TAG, "Found employee: " + employee.getEmail());

            i++;
            if (i > 5) {
                Log.d(LOG_TAG, "Breaking import");
                break;
            }

            String phone = employee.getMobilePhone();
            if (phone == null) {
                Log.d(LOG_TAG, "No phone number for employee, skipped sync");

            } else {

                Log.d(LOG_TAG, "Phone: " + phone);

                storeEmployee(employee);
            }
        }
    }

    private void storeEmployee(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(Contacts.People.NAME, employee.getFirstName() + " " + employee.getLastName());
        Uri uri = context.getContentResolver().insert(Contacts.People.CONTENT_URI, values);

        Uri phoneUri = Uri.withAppendedPath(uri, Contacts.People.Phones.CONTENT_DIRECTORY);

        values.clear();
        values.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_MOBILE);
        values.put(Contacts.People.Phones.NUMBER, employee.getMobilePhone());
        context.getContentResolver().insert(phoneUri, values);

        values.clear();
        values.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_WORK);
        values.put(Contacts.People.Phones.NUMBER, employee.getWorkPhone());
        context.getContentResolver().insert(phoneUri, values);

        values.clear();
        values.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_OTHER);
        values.put(Contacts.People.Phones.NUMBER, employee.getShortPhone());
        context.getContentResolver().insert(phoneUri, values);

        Uri emailUri = Uri.withAppendedPath(uri, Contacts.People.ContactMethods.CONTENT_DIRECTORY);

        values.clear();
        values.put(Contacts.People.ContactMethods.KIND, Contacts.KIND_EMAIL);
        values.put(Contacts.People.ContactMethods.TYPE, Contacts.People.ContactMethods.TYPE_WORK);
        values.put(Contacts.People.ContactMethods.DATA, employee.getEmail());
        context.getContentResolver().insert(emailUri, values);

        //TODO: store the contact image
    }

    private void deleteAllContacts() {
        String[] projection = new String[] { Contacts.People._ID, Contacts.People.NAME, Contacts.People.NUMBER };
        Cursor cur = context.getContentResolver().query(Contacts.People.CONTENT_URI, projection, null, null,
                                                        Contacts.People.DEFAULT_SORT_ORDER);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        while (cur.moveToNext()) {
            Long id = cur.getLong(cur.getColumnIndex(BaseColumns._ID));
            Uri personUri = Contacts.People.CONTENT_URI;
            personUri = personUri.buildUpon().appendPath(Long.toString(id)).build();
            ops.add(ContentProviderOperation.newDelete(personUri).build());
        }

        try {
            context.getContentResolver().applyBatch(Contacts.AUTHORITY, ops);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (OperationApplicationException e) {
            throw new RuntimeException(e);
        }
    }
}
