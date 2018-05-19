package candor.fulki.HOME;

public class Likes {

    String uid;
    String notificationID;

    public Likes() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        uid = uid;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public Likes(String uid, String notificationID) {

        this.uid = uid;
        this.notificationID = notificationID;
    }
}
