package candor.fulki.EXPLORE.EVENTS;

public class Events {


    private String title;
    private String moderator_id;
    private String time_and_date;
    private String image_url;
    private String thumb_image_url;
    private String location;
    private String event_push_id;
    private String description;
    private String people_cnt;
    private String discussion_cnt;
    private long timestamp;
    private String clap_cnt;
    private String love_cnt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModerator_id() {
        return moderator_id;
    }

    public void setModerator_id(String moderator_id) {
        this.moderator_id = moderator_id;
    }

    public String getTime_and_date() {
        return time_and_date;
    }

    public void setTime_and_date(String time_and_date) {
        this.time_and_date = time_and_date;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_image_url() {
        return thumb_image_url;
    }

    public void setThumb_image_url(String thumb_image_url) {
        this.thumb_image_url = thumb_image_url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEvent_push_id() {
        return event_push_id;
    }

    public void setEvent_push_id(String event_push_id) {
        this.event_push_id = event_push_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPeople_cnt() {
        return people_cnt;
    }

    public void setPeople_cnt(String people_cnt) {
        this.people_cnt = people_cnt;
    }

    public String getDiscussion_cnt() {
        return discussion_cnt;
    }

    public void setDiscussion_cnt(String discussion_cnt) {
        this.discussion_cnt = discussion_cnt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Events() {

    }

    public Events(String title, String moderator_id, String time_and_date, String image_url, String thumb_image_url, String location, String event_push_id, String description, String people_cnt, String discussion_cnt, long timestamp, String clap_cnt, String love_cnt) {
        this.title = title;
        this.moderator_id = moderator_id;
        this.time_and_date = time_and_date;
        this.image_url = image_url;
        this.thumb_image_url = thumb_image_url;
        this.location = location;
        this.event_push_id = event_push_id;
        this.description = description;
        this.people_cnt = people_cnt;
        this.discussion_cnt = discussion_cnt;
        this.timestamp = timestamp;
        this.clap_cnt = clap_cnt;
        this.love_cnt = love_cnt;
    }

    public String getClap_cnt() {

        return clap_cnt;
    }

    public void setClap_cnt(String clap_cnt) {
        this.clap_cnt = clap_cnt;
    }

    public String getLove_cnt() {
        return love_cnt;
    }

    public void setLove_cnt(String love_cnt) {
        this.love_cnt = love_cnt;
    }
}
