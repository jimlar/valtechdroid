package se.jimlar.intranet;

public class Employee {
    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String imageUrl;
    private String email;
    private long userId;

    public Employee(long userId, String firstName, String lastName, String mobilePhone, String imageUrl, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhone = mobilePhone;
        this.imageUrl = imageUrl;
        this.email = email;
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

    public boolean isDeleted() {
        return false;
    }

    public String getWorkPhone() {
        return "+46 8 5622 " + getShortPhone();
    }
}
