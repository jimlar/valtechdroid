package se.jimlar.intranet;

public class Employee {
    private String firstName;
    private String lastName;
    private String phone;
    private String imageUrl;
    private String email;
    private long userId;

    public Employee(long userId, String firstName, String lastName, String phone, String imageUrl, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
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

    public boolean isDeleted() {
        return false;
    }
}
