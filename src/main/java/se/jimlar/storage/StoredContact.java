package se.jimlar.storage;

import se.jimlar.intranet.Employee;

import java.util.List;

public class StoredContact {
    private final long contactId;
    private final String imageState;
    private final Employee employee;

    public StoredContact(long contactId, String imageState, Employee employee) {
        this.contactId = contactId;
        this.imageState = imageState;
        this.employee = employee;
    }

    public long getContactId() {
        return contactId;
    }

    public String getImageState() {
        return imageState;
    }

    public Employee getEmployee() {
        return employee;
    }

    public boolean presentIn(List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.getUserId() == this.employee.getUserId()) {
                return true;
            }
        }
        return false;
    }

    public boolean needsUpdate(Employee employee) {
        return !this.employee.equals(employee);
    }
}
