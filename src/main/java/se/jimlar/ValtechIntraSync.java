package se.jimlar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class ValtechIntraSync extends Activity {
    private SharedPreferences preferences;
    private static final String USERNAME_SETTING = "username";
    private static final String PASSWORD_SETTING = "password";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        preferences = getSharedPreferences("valtechdroid", MODE_PRIVATE);
        String savedUsername = preferences.getString(USERNAME_SETTING, "");
        String savedPassword = preferences.getString(PASSWORD_SETTING, "");

        getUsername().setText(savedUsername);
        getPassword().setText(savedPassword);

        Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                List<Employee> employeeSet = new APIClient(getUsername().getText().toString(),
                                                           getPassword().getText().toString(),
                                                           new APIResponseParser()).getEmployees();
                getStatusLabel().setText(employeeSet.size() + " employes found");
            }
        });


        /* Contacts */
        System.out.println("Querying phone book");
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                System.out.println("id = " + id);
                System.out.println("name = " + name);

                /* Phone */
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                  null,
                                                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                                  new String[]{id}, null);
                    while (phoneCursor.moveToNext()) {
                        System.out.println("number: " + phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        System.out.println("type: " + phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
                    }
                    phoneCursor.close();

                }

                /* Emails */
                Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                while (emailCur.moveToNext()) {
                    String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                    System.out.println("email = " + email);
                    System.out.println("emailType = " + emailType);
                }
                emailCur.close();

                /*  Notes */
                String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] noteWhereParams = new String[]{id,
                                                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                if (noteCur.moveToFirst()) {
                    String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                    System.out.println("note = " + note);
                }
                noteCur.close();

                /* Address */
                String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] addrWhereParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
                while (addrCur.moveToNext()) {
                    String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                    String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                    String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                    String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                    String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                    String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                    String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

                    System.out.println("poBox = " + poBox);
                    System.out.println("street = " + street);
                    System.out.println("city = " + city);
                    System.out.println("state = " + state);
                    System.out.println("postalCode = " + postalCode);
                    System.out.println("country = " + country);
                    System.out.println("type = " + type);
                }
                addrCur.close();

                /* IM */
                String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] imWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
                Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);
                if (imCur.moveToFirst()) {
                    String imName = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
                    String imType = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
                    System.out.println("imType = " + imType);
                    System.out.println("imName = " + imName);
                }
                imCur.close();

                /* Organization */
                String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] orgWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
                Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
                if (orgCur.moveToFirst()) {
                    String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                    String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                    System.out.println("orgName = " + orgName);
                    System.out.println("title = " + title);
                }
                orgCur.close();
            }
        }
    }

    private TextView getStatusLabel() {
        return (TextView) findViewById(R.id.status);
    }

    private EditText getPassword() {
        return (EditText) findViewById(R.id.passwordentry);
    }

    private EditText getUsername() {
        return (EditText) findViewById(R.id.userentry);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(USERNAME_SETTING, getUsername().getText().toString());
        ed.putString(PASSWORD_SETTING, getPassword().getText().toString());
        ed.commit();
    }
}
