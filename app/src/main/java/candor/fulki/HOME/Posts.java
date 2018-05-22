package candor.fulki.HOME;

/**
 * Created by Mohammad Faisal on 1/21/2018.
 */

public class Posts {


    private long timestamp;

    private String user_name;
    private String user_id;
    private String user_thumb_image;

    private String post_image_url;
    private String post_thumb_image_url;


    private String time_and_date;
    private String location;  //works as shared by flag
    private String post_push_id;
    private String caption;

    private long like_cnt;
    private long comment_cnt;
    private long share_cnt;


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_thumb_image() {
        return user_thumb_image;
    }

    public void setUser_thumb_image(String user_thumb_image) {
        this.user_thumb_image = user_thumb_image;
    }

    public String getPost_image_url() {
        return post_image_url;
    }

    public void setPost_image_url(String post_image_url) {
        this.post_image_url = post_image_url;
    }

    public String getPost_thumb_image_url() {
        return post_thumb_image_url;
    }

    public void setPost_thumb_image_url(String post_thumb_image_url) {
        this.post_thumb_image_url = post_thumb_image_url;
    }

    public String getTime_and_date() {
        return time_and_date;
    }

    public void setTime_and_date(String time_and_date) {
        this.time_and_date = time_and_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPost_push_id() {
        return post_push_id;
    }

    public void setPost_push_id(String post_push_id) {
        this.post_push_id = post_push_id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getLike_cnt() {
        return like_cnt;
    }

    public void setLike_cnt(long like_cnt) {
        this.like_cnt = like_cnt;
    }

    public long getComment_cnt() {
        return comment_cnt;
    }

    public void setComment_cnt(long comment_cnt) {
        this.comment_cnt = comment_cnt;
    }

    public long getShare_cnt() {
        return share_cnt;
    }

    public void setShare_cnt(long share_cnt) {
        this.share_cnt = share_cnt;
    }

    public Posts() {

    }

    public Posts(long timestamp, String user_name, String user_id, String user_thumb_image, String post_image_url, String post_thumb_image_url, String time_and_date, String location, String post_push_id, String caption, long like_cnt, long comment_cnt, long share_cnt) {

        this.timestamp = timestamp;
        this.user_name = user_name;
        this.user_id = user_id;
        this.user_thumb_image = user_thumb_image;
        this.post_image_url = post_image_url;
        this.post_thumb_image_url = post_thumb_image_url;
        this.time_and_date = time_and_date;
        this.location = location;
        this.post_push_id = post_push_id;
        this.caption = caption;
        this.like_cnt = like_cnt;
        this.comment_cnt = comment_cnt;
        this.share_cnt = share_cnt;
    }
}
