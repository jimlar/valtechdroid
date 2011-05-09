package se.valtech.storage;

public class SyncState {
    private final long contactId;
    private final long sourceId;
    private final String photoUrl;
    private final PhotoState photoState;
    private long lastStatusUpdate;

    public SyncState(long contactId, long sourceId, String photoUrl, PhotoState photoState, long lastStatusUpdate) {
        this.contactId = contactId;
        this.sourceId = sourceId;
        this.photoUrl = photoUrl;
        this.photoState = photoState;
        this.lastStatusUpdate = lastStatusUpdate;
    }

    public long getContactId() {
        return contactId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public PhotoState getPhotoState() {
        return photoState;
    }

    public long getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public long getSourceId() {
        return sourceId;
    }

    public SyncState newPhotoState(PhotoState newState) {
        return new SyncState(contactId, sourceId, photoUrl, newState, lastStatusUpdate);
    }

    public SyncState newStatusUpdateTimeStamp(long statusTimeStamp) {
        return new SyncState(contactId, sourceId, photoUrl, photoState, statusTimeStamp);
    }
}
