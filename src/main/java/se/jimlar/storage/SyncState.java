package se.jimlar.storage;

public class SyncState {
    private final long contactId;
    private final String imageUrl;
    private final String state;

    public SyncState(long contactId, String imageUrl, String state) {
        this.contactId = contactId;
        this.imageUrl = imageUrl;
        this.state = state;
    }

    public long getContactId() {
        return contactId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getState() {
        return state;
    }

    public SyncState imageDownloaded() {
        return new SyncState(contactId, imageUrl, "downloaded");
    }
}
