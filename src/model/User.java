package model;

public class User {
    private String userId;
    private String name;
    private String phoneNumber;
    private String profilePicturePath;

    public User(String userId, String name, String phoneNumber, String profilePicturePath) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePicturePath = profilePicturePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePicturePath='" + profilePicturePath + '\'' +
                '}';
    }
}

