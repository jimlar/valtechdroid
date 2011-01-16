package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
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
            for (Employee employee : employees) {
                Log.d(LOG_TAG, "Found employee: " + employee.getEmail());


                //TODO: batch these operations
                String phone = employee.getPhone();
                if (phone == null) {
                    Log.d(LOG_TAG, "No phone number for employee, skipped sync");

                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contacts.People.NAME, employee.getFirstName() + " " + employee.getLastName());
                    values.put(Contacts.People.PRIMARY_EMAIL_ID, employee.getEmail());
                    Uri uri = context.getContentResolver().insert(Contacts.People.CONTENT_URI, values);

                    Uri phoneUri = Uri.withAppendedPath(uri, Contacts.People.Phones.CONTENT_DIRECTORY);

                    Log.d(LOG_TAG, "Phone: " + phone);
                    phone = phone.replaceAll(" ", "");
                    phone = phone.replaceAll("-", "");

                    //TODO: should store the 08 phone also
                    String shortPhone = phone.substring(phone.length() - 4, phone.length());

                    values.clear();
                    values.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_MOBILE);
                    values.put(Contacts.People.Phones.NUMBER, phone);
                    context.getContentResolver().insert(phoneUri, values);

                    values.clear();
                    values.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_OTHER);
                    values.put(Contacts.People.Phones.NUMBER, shortPhone);
                    context.getContentResolver().insert(phoneUri, values);

                    //TODO: store the contact image
                }
            }


        } catch (Exception e) {
            Log.w(LOG_TAG, "Sync failed", e);
        }
    }
}
