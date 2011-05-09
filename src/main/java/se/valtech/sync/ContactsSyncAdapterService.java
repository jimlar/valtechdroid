package se.valtech.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
