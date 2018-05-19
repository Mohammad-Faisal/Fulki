package candor.fulki.HOME;

/**
 * Created by Mohammad Faisal on 1/21/2018.
 */

public class Posts {


    private long timestamp;
    private String user_name;
    private String user_id;
    private String time_and_date;
    private String image_url;
    private String thumb_image_url;
    private String location;
    private String post_push_id;
    private String caption;
    private int like_cnt;
    private int comment_cnt;
    private int share_cnt;

    public Posts() {
    }

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

    public int getLike_cnt() {
        return like_cnt;
    }

    public void setLike_cnt(int like_cnt) {
        this.like_cnt = like_cnt;
    }

    public int getComment_cnt() {
        return comment_cnt;
    }

    public void setComment_cnt(int comment_cnt) {
        this.comment_cnt = comment_cnt;
    }

    public int getShare_cnt() {
        return share_cnt;
    }

    public void setShare_cnt(int share_cnt) {
        this.share_cnt = share_cnt;
    }

    public Posts(long timestamp, String user_name, String user_id, String time_and_date, String image_url, String thumb_image_url, String location, String post_push_id, String caption, int like_cnt, int comment_cnt, int share_cnt) {
        this.timestamp = timestamp;
        this.user_name = user_name;
        this.user_id = user_id;
        this.time_and_date = time_and_date;
        this.image_url = image_url;
        this.thumb_image_url = thumb_image_url;
        this.location = location;
        this.post_push_id = post_push_id;
        this.caption = caption;
        this.like_cnt = like_cnt;
        this.comment_cnt = comment_cnt;
        this.share_cnt = share_cnt;
    }
}
