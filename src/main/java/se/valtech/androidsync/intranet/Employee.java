package se.valtech.androidsync.intranet;

public class Employee {
    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String imageUrl;
    private String email;
    private long userId;
    private String statusMessage;
    private long statusTimeStamp;

    public Employee(long userId, String firstName, String lastName, String mobilePhone, String imageUrl, String email, String statusMessage, long statusTimeStamp) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhone = mobilePhone;
        this.imageUrl = imageUrl;
        this.email = email;
        this.statusMessage = statusMessage;
        this.statusTimeStamp = statusTimeStamp;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getShortPhone() {
        String phone = getMobilePhone().replace(" ", "").replace("-", "");
        return phone.substring(phone.length() - 4, phone.length());
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public long getUserId() {
        return userId;
    }

    public String getWorkPhone() {
        return "+46 8 5622 " + getShortPhone();
    }

    public boolean hasPhone() {
        return getMobilePhone() != null;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public long getStatusTimeStamp() {
        return statusTimeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        if (userId != employee.userId) return false;
        if (email != null ? !email.equals(employee.email) : employee.email != null) return false;
        if (firstName != null ? !firstName.equals(employee.firstName) : employee.firstName != null) return false;
        if (imageUrl != null ? !imageUrl.equals(employee.imageUrl) : employee.imageUrl != null) return false;
        if (lastName != null ? !lastName.equals(employee.lastName) : employee.lastName != null) return false;
        if (mobilePhone != null ? !mobilePhone.equals(employee.mobilePhone) : employee.mobilePhone != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (mobilePhone != null ? mobilePhone.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", email='" + email + '\'' +
                ", userId=" + userId +
                ", statusMessage='" + statusMessage + '\'' +
                ", statusTimeStamp=" + statusTimeStamp +
                '}';
    }

    public boolean hasStatusMessage() {
        return statusMessage != null;
    }

    public String getName() {
        return getFirstName() + " " + getLastName();
    }
}
