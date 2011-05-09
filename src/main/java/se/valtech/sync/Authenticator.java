package se.valtech.sync;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import se.valtech.Logger;

import java.util.Arrays;

class Authenticator extends AbstractAccountAuthenticator {
    private static final Logger LOG = new Logger(Authenticator.class);
    private Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        Bundle result = new Bundle();
        Intent i = new Intent(context, LoginActivity.class);
        i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        result.putParcelable(AccountManager.KEY_INTENT, i);
        return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        LOG.debug("confirmCredentials");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        LOG.debug("editProperties");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        LOG.debug("getAuthToken, options: " + options);
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        bundle.putString(AccountManager.KEY_AUTHTOKEN, AccountManager.get(context).getPassword(account));
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        LOG.debug("getAuthTokenLabel");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        LOG.debug("hasFeatures: " + Arrays.asList(features));
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        LOG.debug("updateCredentials");
        return null;
    }
}
