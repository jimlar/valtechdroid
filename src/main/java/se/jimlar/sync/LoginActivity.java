package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import se.jimlar.R;

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
		ProgressDialog mDialog;

		LoginTask(Context c) {
            Log.i(LOG_TAG, "create");
			context = c;
			loginButton.setEnabled(false);

			mDialog = ProgressDialog.show(c, "", getString(R.string.authenticating), true, false);
			mDialog.setCancelable(true);
		}

		@Override
		public Boolean doInBackground(String... params) {
            Log.i(LOG_TAG, "start");
			String user = params[0];
			String pass = params[1];

			// Do something internetty
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Bundle result = null;
			Account account = new Account(user, context.getString(R.string.ACCOUNT_TYPE));
			AccountManager am = AccountManager.get(context);
            Log.i(LOG_TAG, "adding account");
            Bundle bundle = new Bundle();
            bundle.putString("stuff", "yup");
            if (am.addAccountExplicitly(account, pass, bundle)) {
				result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
				result.putString(AccountManager.KEY_AUTHTOKEN, pass);
                Log.i(LOG_TAG, "setting auth result");
				setAccountAuthenticatorResult(result);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onPostExecute(Boolean result) {
			loginButton.setEnabled(true);
			mDialog.dismiss();
			if (result)
                finish();
		}
	}
}
