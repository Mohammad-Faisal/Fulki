package candor.fulki.HOME;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.NOTIFICATION.Notifications;
import candor.fulki.PROFILE.ShowPleopleListActivity;
import candor.fulki.R;
import candor.fulki.UTILITIES.TouchImageView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ShowPostActivity extends AppCompatActivity  implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{


    private static final String TAG = "ShowPostActivity";
    private String mPostID , mUserID , mPostOwnerId;
    private String mUserName , mUserThumbImage , mUserImage;
    /*CollapsingToolbarLayout collapsingToolbarLayout;*/
    public DisplayImageOptions postImageOptions;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



    public ImageView mPostMoreOptions;
    public TextView postUserName;
    public TextView postCaption;
    public TextView postDateTime;
    public TextView postLocaiton;
    public TextView postLikeCount;
    public TextView postCommentCount;
    public SliderLayout postSlider;

    public CircleImageView postUserImage , mShowPostOwnImage;
    public LikeButton postLikeButton;


    public String mPostOwnerID;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);


        postUserName =findViewById(R.id.post_user_name);
        postCaption = findViewById(R.id.post_caption);
        postDateTime =findViewById(R.id.post_time_date);
        postLocaiton = findViewById(R.id.post_location);
        postUserImage = findViewById(R.id.post_user_single_imagee);
        postLikeButton = findViewById(R.id.post_like_button);
        postLikeCount = findViewById(R.id.post_like_number);
        postCommentCount = findViewById(R.id.show_post_comment_count);
        mShowPostOwnImage = findViewById(R.id.show_post_own_image);
        //final ImageView postImageView = findViewById(R.id.show_post_collapsing_image);
        final TouchImageView postImageView = findViewById(R.id.show_post_collapsing_image);
        postSlider = findViewById(R.id.show_post_slider);


        mPostID = getIntent().getStringExtra("postID");
        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(mUserID!=null){
            firebaseFirestore.collection("users").document(mUserID).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    mUserName = documentSnapshot.getString("name");
                    mUserImage= documentSnapshot.getString("image");
                    mUserThumbImage =documentSnapshot.getString("thumb_image");

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(mUserThumbImage, mShowPostOwnImage);

                }
            }).addOnFailureListener(e -> {
                mUserImage = MainActivity.mUserImage;
                mUserName = MainActivity.mUserName;
                mUserThumbImage = MainActivity.mUserThumbImage;

                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(mUserThumbImage, mShowPostOwnImage);
            });
        }


        postLikeCount.setOnClickListener(v -> {
            addRating(mUserID , 1);
            Intent showPeopleIntent = new Intent(ShowPostActivity.this , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "likes");
            showPeopleIntent.putExtra("user_id" ,mPostID );
            startActivity(showPeopleIntent);
        });


        firebaseFirestore.collection("posts").document(mPostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public static final String TAG = "SHow Post";

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        CombinedPosts post = task.getResult()
                                .toObject(CombinedPosts.class);

                        HashMap<String,String> Hash_file_maps;
                        Hash_file_maps = post.getPost_thumb_url();

                        if(Hash_file_maps.size() == 0){
                            postSlider.setVisibility(View.GONE);
                        }else{
                            setImages(Hash_file_maps);
                        }



                        String primary_user_id = post.getPrimary_user_id();
                        String secondary_user_id = post.getSecondary_user_id();

                        String primary_push_id = post.getPrimary_push_id();
                        String secondary_push_id = post.getSecondary_push_id();


                        String timedate = post.getTime_and_date();
                        String location = post.getLocation();
                        String caption = post.getCaption();
                        String type = post.getType();

                        String privacy = post.getPrivacy();

                        long like_cnt = post.getLike_cnt();
                        long comment_cnt = post.getComment_cnt();
                        long share_cnt = post.getShare_cnt();


                        String postCaptionText = post.getCaption();
                        String postTime = post.getTime_and_date();



                        firebaseFirestore.collection("users").document(primary_user_id).get().addOnSuccessListener(documentSnapshot -> {
                            if(documentSnapshot.exists()){
                                String primaryUserName = documentSnapshot.getString("name");
                                String primaryUserThumbImage =documentSnapshot.getString("thumb_image");
                                postUserName.setText(primaryUserName);
                                ImageLoader imageLoader = ImageLoader.getInstance();
                                imageLoader.displayImage(primaryUserThumbImage, postUserImage);

                            }
                        }).addOnFailureListener(e -> {
                            String primaryUserName = "User Name";
                            String primaryUserThumbImage = "default";
                            postUserName.setText(primaryUserName);
                            ImageLoader imageLoader = ImageLoader.getInstance();
                            imageLoader.displayImage(primaryUserThumbImage, postUserImage);
                        });



                        long likeCount = post.getLike_cnt();
                        long commentCount = post.getComment_cnt();



                        setLikeCount(likeCount);
                        setCommentCount(commentCount);


                        mPostOwnerID = primary_user_id;
                        postDateTime.setText(postTime);
                        if(postCaptionText.length() == 0){
                            postCaption.setVisibility(View.GONE);
                        }else{
                            postCaption.setText(postCaptionText);
                        }



                    }else{
                        Log.d(TAG, "onComplete: an error occured while loading the image");
                    }
                }
            }
        });

        //setting the current state of like button
        FirebaseFirestore.getInstance().collection("likes/" + mPostID + "/likes").document(mUserID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    postLikeButton.setLiked(true);
                } else {
                    postLikeButton.setLiked(false);
                }
            } else {
                postLikeButton.setLiked(false);
            }
        });

        //handling the like onclick listener
        postLikeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {



                //building like
                postLikeButton.setLiked(true);
                Functions f = new Functions();

                //building like
                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mPostOwnerID+"/notificatinos").document();
                String likeNotificatoinPushID = ref.getId();

                Likes mLikes = new Likes(mUserID , mUserName , mUserThumbImage ,likeNotificatoinPushID , time_stamp);
                Notifications pushNoti = new Notifications( "like" ,mUserID ,mPostOwnerID , mPostID ,likeNotificatoinPushID , time_stamp,"n"  );


                WriteBatch writeBatch  = firebaseFirestore.batch();

                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+mPostOwnerID+"/notificatinos").document(likeNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID); //.set(mLikes);
                writeBatch.set(postLikeRef, mLikes);

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    addRating(mUserID , 3);
                    addRating( mPostOwnerID , 1);
                    addLike(mPostID , 1);
                    Log.d(TAG, "liked:   like is successful");

                }).addOnFailureListener(e -> {
                    Log.d(TAG, "liked:   like is not succesful");
                });
            }
            @Override
            public void unLiked(LikeButton likeButton) {
               postLikeButton.setLiked(false);
                firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                String likeNotificatoinPushID = task.getResult().getString("notificationID");
                                WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
                                if(likeNotificatoinPushID!=null){
                                    writeBatch.delete(firebaseFirestore.collection("notifications/"+mPostOwnerID+"/notificatinos").document(likeNotificatoinPushID));
                                    writeBatch.delete(firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID));
                                }
                                writeBatch.delete(firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID));
                                writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        addRating(mUserID , -3);
                                        addRating( mPostOwnerID , -1);
                                        addLike(mPostID , -1);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

        //setting comment list
        final RecyclerView mCommentList;
        final List<Comments> commentList = new ArrayList<>();
        LinearLayoutManager mLinearLayout;


        //--------- SETTING THE COMMENT ADAPTERS --//
        final PostCommentAdapter mPostCommentAdapter = new PostCommentAdapter(commentList, this , ShowPostActivity.this);
        mCommentList = findViewById(R.id.show_post_recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mCommentList.hasFixedSize();
        mCommentList.setLayoutManager(mLinearLayout);
        mCommentList.setAdapter(mPostCommentAdapter);


        //-------------LOADING COMMENTS------------//
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("comments/"+mPostID+"/comments").orderBy("time_stamp" , Query.Direction.DESCENDING);
        nextQuery.addSnapshotListener((documentSnapshots, e) -> {
            for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if(doc.getType() == DocumentChange.Type.ADDED){
                    Comments singleComment = doc.getDocument().toObject(Comments.class);
                    commentList.add(singleComment);
                    mPostCommentAdapter.notifyDataSetChanged();
                }
            }
        });


        //posting comments
        final TextView commentBox = findViewById(R.id.comment_write);
        ImageButton commentPost = findViewById(R.id.comment_post);
        commentPost.setOnClickListener(view -> {

            addRating(mUserID , 5);
            addRating( mPostOwnerID , 2);

            String time_stamp = String.valueOf(new Date().getTime());

            DocumentReference notiRef = FirebaseFirestore.getInstance().collection("notifications/"+mPostOwnerID+"/notificatinos").document();
            String commentNotificatoinPushID = notiRef.getId();

            DocumentReference commentRef = FirebaseFirestore.getInstance().collection("comments/"+mPostID+"/comments").document();
            String commentID = commentRef.getId();

            Notifications pushNoti = new Notifications( "comment" ,mUserID ,mPostOwnerID, mPostID ,commentNotificatoinPushID , time_stamp,"n"  );
            Comments  comment =  new Comments(commentBox.getText().toString() , mUserID ,commentID, mPostID  , commentNotificatoinPushID  , time_stamp);
            commentBox.setText("");

            WriteBatch writeBatch  = firebaseFirestore.batch();

            writeBatch.set(notiRef, pushNoti);
            DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(commentNotificatoinPushID);
            writeBatch.set(postNotificatinoRef, pushNoti);

            writeBatch.set(commentRef, comment);

            writeBatch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "liked:   like is successful");

            }).addOnFailureListener(e -> {
                Log.d(TAG, "liked:   like is not succesful");
            });


        });

    }

    private Task<Void> addRating(String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addRating:   function calledd !!!!");
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        return firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }

    private void setLikeCount(long likeCnt){
        if(likeCnt>1){
            postLikeCount.setText(""+likeCnt+" likes");
        }else{
            postLikeCount.setText(""+likeCnt+" like");
        }
    }

    private void setCommentCount(long commentCnt){
        if(commentCnt>1){
            postCommentCount.setText(""+commentCnt+" comments");
        }else{
            postCommentCount.setText(""+commentCnt+" comment");
        }
    }

    private void addLike( String mPostID , int factor) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addLike:   function calledd !!!!");
        final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts")
                .document(mPostID);

        firebaseFirestore.runTransaction(transaction -> {
            Posts post = transaction.get(postRef)
                    .toObject(Posts.class);
            long curLikes = post.getLike_cnt();
            long nextLike = curLikes + factor;

            Log.d(TAG, "addLike:     like number is  "+nextLike);
            HashMap< String ,  Object > updateMap = new HashMap<>();

            updateMap.put("like_cnt" , nextLike);
            transaction.update(postRef , updateMap);

            return nextLike;
        }).addOnSuccessListener(aLong -> {
            setLikeCount(aLong);
        });
    }


    public void setImages(HashMap<String , String> Hash_file_maps) {

        if (Hash_file_maps.size() > 0) {
            for (String name : Hash_file_maps.keySet()) {
                TextSliderView textSliderView = new TextSliderView(ShowPostActivity.this);
                textSliderView
                        .description(Hash_file_maps.get(name))
                        .image(name)
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                        .setOnSliderClickListener(this);
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", name);
                postSlider.addSlider(textSliderView);
            }
            postSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            postSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            postSlider.setCustomAnimation(new DescriptionAnimation());
            postSlider.setDuration(3000);
            postSlider.addOnPageChangeListener(this);
            postSlider.setCustomIndicator((PagerIndicator)findViewById(R.id.custom_indicator));
        } else {
            if(postSlider!=null)postSlider.setVisibility(View.GONE);

        }
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}


