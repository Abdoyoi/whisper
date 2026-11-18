package model;

import java.util.Objects;

/**
 * Represents a contact in the Whispr application.
 */
public class Contact {
    private String name;
    private String phoneNumber;
    private String profilePicturePath;

    /**
     * Constructor for Contact.
     *
     * @param name              the name of the contact
     * @param phoneNumber       the phone number of the contact
     * @param profilePicturePath the path to the contact's profile picture
     */
    public Contact(String name, String phoneNumber, String profilePicturePath) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePicturePath = profilePicturePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePicturePath='" + profilePicturePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return Objects.equals(phoneNumber, contact.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}

