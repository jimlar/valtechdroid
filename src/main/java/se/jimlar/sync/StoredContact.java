package se.jimlar.sync;

import se.jimlar.intranet.Employee;

import java.util.List;

public class StoredContact {
    public final long contactId;
    public final long sourceId;
    public final String imageUrl;

    public StoredContact(long contactId, long sourceId, String imageUrl) {
        this.contactId = contactId;
        this.sourceId = sourceId;
        this.imageUrl = imageUrl;
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
