package se.jimlar.sync;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import se.jimlar.R;

public class LoginActivity extends AccountAuthenticatorActivity {
	private EditText username;
	private EditText password;
	private Button loginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		loginButton = (Button) findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String user = username.getText().toString().trim().toLowerCase();
				String password = LoginActivity.this.password.getText().toString().trim().toLowerCase();

				if (user.length() > 0 && password.length() > 0) {
					LoginTask t = new LoginTask(LoginActivity.this);
					t.execute(user, password);
				}
			}

		});
	}

	private class LoginTask extends AsyncTask<String, Void, Boolean> {
		Context context;
		ProgressDialog mDialog;

		LoginTask(Context c) {
			context = c;
			loginButton.setEnabled(false);

			mDialog = ProgressDialog.show(c, "", getString(R.string.authenticating), true, false);
			mDialog.setCancelable(true);
		}

		@Override
		public Boolean doInBackground(String... params) {
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
			if (am.addAccountExplicitly(account, pass, null)) {
				result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
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
