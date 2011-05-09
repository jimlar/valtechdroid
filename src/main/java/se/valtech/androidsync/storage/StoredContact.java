package se.valtech.androidsync.storage;

import se.valtech.androidsync.intranet.Employee;

import java.util.List;

public class StoredContact {
    private final long contactId;
    private final Employee employee;

    public StoredContact(long contactId, Employee employee) {
        this.contactId = contactId;
        this.employee = employee;
    }

    public long getContactId() {
        return contactId;
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
