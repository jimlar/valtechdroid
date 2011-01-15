package se.jimlar.sync;

import java.util.ArrayList;
import java.util.HashMap;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;
import se.jimlar.R;

public class ContactsSyncAdapterService extends Service {
	private static SyncAdapter syncAdapter;

    @Override
	public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
	}

	private SyncAdapter getSyncAdapter() {
		if (syncAdapter == null) {
            syncAdapter = new SyncAdapter(this);
        }
		return syncAdapter;
	}
}
