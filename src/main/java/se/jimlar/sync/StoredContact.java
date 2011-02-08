package se.jimlar.sync;

public class StoredContact {
    private final long contactId;
    private final String sourceId;

    public StoredContact(long contactId, String sourceId, String imageUrl) {
        this.contactId = contactId;
        this.sourceId = sourceId;
    }
}
