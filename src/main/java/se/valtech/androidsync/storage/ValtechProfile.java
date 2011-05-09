package se.valtech.androidsync.storage;

import android.provider.ContactsContract;

public interface ValtechProfile {

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.valtechdroid.profile";

    public static final String PROFILE_ID = ContactsContract.Data.DATA1;
}
