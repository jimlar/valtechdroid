package se.jimlar.storage;

public class ImageState {
    private final long contactId;
    private final String imageUrl;
    private final String state;

    public ImageState(long contactId, String imageUrl, String state) {
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
}
