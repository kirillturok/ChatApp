package by.turok.lab_4.entity;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class User implements Serializable {

    private String id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String birthday;
    private String image;
    private long availability;
    private String token;

    public User() {
    }

    public User(String id, String email, String phone, String firstName, String lastName, String birthday, String image, long availability, String token) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.image = image;
        this.availability = availability;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAvailability() {
        return availability;
    }

    public void setAvailability(long availability) {
        this.availability = availability;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return availability == user.availability && Objects.equals(id, user.id) && Objects.equals(email, user.email) && Objects.equals(phone, user.phone) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(birthday, user.birthday) && Objects.equals(image, user.image) && Objects.equals(token, user.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, phone, firstName, lastName, birthday, image, availability, token);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                ", image='" + image + '\'' +
                ", availability=" + availability +
                ", token='" + token + '\'' +
                '}';
    }

    public static class UserNameComparator implements Comparator<User> {

        @Override
        public int compare(User user1, User user2) {
            String name1 = user1.getFirstName() + " " + user1.getLastName();
            String name2 = user2.getFirstName() + " " + user2.getLastName();
            return name1.compareToIgnoreCase(name2);
        }
    }
}
