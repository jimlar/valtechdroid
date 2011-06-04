package se.valtech.androidsync.sync;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import se.valtech.androidsync.R;
import se.valtech.androidsync.intranet.APIClient;
import se.valtech.androidsync.intranet.APIResponseParser;
import se.valtech.androidsync.storage.ValtechProfile;

public class LoginActivity extends AccountAuthenticatorActivity {
    private static final String LOG_TAG = LoginActivity.class.getName();

	private EditText username;
	private EditText password;
	private Button loginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "create");
		setContentView(R.layout.main);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		loginButton = (Button) findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
                Log.i(LOG_TAG, "click");
				String user = username.getText().toString();
				String password = LoginActivity.this.password.getText().toString();

				if (user.length() > 0 && password.length() > 0) {
					LoginTask t = new LoginTask(LoginActivity.this);
					t.execute(user, password);
				}
			}

		});
	}

	private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private final String LOG_TAG = LoginTask.class.getName();

		Context context;
		ProgressDialog progressDialog;
        private Handler progressMessageHandler;

		LoginTask(Context context) {
            Log.i(LOG_TAG, "create");
			this.context = context;
			loginButton.setEnabled(false);
            progressDialog = ProgressDialog.show(context, "", "Authenticating", true, true);
            progressMessageHandler = new Handler();
		}



		@Override
		public Boolean doInBackground(String... params) {
            Log.i(LOG_TAG, "start");
			String user = params[0];
			String pass = params[1];

			try {
                APIClient client = new APIClient(user, pass, new APIResponseParser());
                if (!client.authenticate()) {
                    updateProgress("Authentication failed");
                    return false;
                }
			} catch (Exception e) {
				e.printStackTrace();
                return false;
			}

            updateProgress("Creating account");
			Account account = new Account(user, ValtechProfile.ACCOUNT_TYPE);
			AccountManager am = AccountManager.get(context);
            Log.i(LOG_TAG, "adding account");
            Bundle bundle = new Bundle();
            bundle.putString("stuff", "yup");
            if (am.addAccountExplicitly(account, pass, bundle)) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, ValtechProfile.ACCOUNT_TYPE);
				result.putString(AccountManager.KEY_AUTHTOKEN, pass);

                //Enable sync by default
                Log.i(LOG_TAG, "enabling sync");
                Account[] accounts = am.getAccountsByType(ValtechProfile.ACCOUNT_TYPE);
                ContentResolver.setIsSyncable(accounts[0], ContactsContract.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(accounts[0], ContactsContract.AUTHORITY, true);

                Log.i(LOG_TAG, "setting auth result");
				setAccountAuthenticatorResult(result);

				return true;
			} else {
                updateProgress("Failed to create account");
				return false;
			}
		}

        private void updateProgress(final String msg) {
            progressMessageHandler.post(new Runnable() {
                public void run() {
                    progressDialog.setMessage(msg);
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { }
        }

        @Override
		public void onPostExecute(Boolean result) {
			loginButton.setEnabled(true);
            progressDialog.dismiss();
			if (result) {
                finish();
            }
		}
	}
}
