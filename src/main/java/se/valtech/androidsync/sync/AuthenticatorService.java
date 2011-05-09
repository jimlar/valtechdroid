package se.valtech.androidsync.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
	private static Authenticator authenticator;

	@Override
	public IBinder onBind(Intent intent) {
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            return getAuthenticator().getIBinder();
        }
		return null;
	}

	private Authenticator getAuthenticator() {
		if (authenticator == null) {
            authenticator = new Authenticator(this);
        }
		return authenticator;
	}
}
