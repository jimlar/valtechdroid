package se.jimlar.sync;

import se.jimlar.intranet.Employee;

import java.util.List;

public class StoredContact {
    public final long contactId;
    public final long sourceId;
    public final String imageUrl;
    public final String imageState;

    public StoredContact(long contactId, long sourceId, String imageUrl, String imageState) {
        this.contactId = contactId;
        this.sourceId = sourceId;
        this.imageUrl = imageUrl;
        this.imageState = imageState;
    }

    public boolean presentIn(List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.getUserId() == sourceId) {
                return true;
            }
        }
        return false;
    }
}
