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
import de.hdodenhof.circleimageview.CircleImageView;

public class ShowPostActivity extends AppCompatActivity {


    private static final String TAG = "ShowPostActivity";
    private String mPostID , mUserID , mPostOwnerId;
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

    public CircleImageView postUserImage , mShowPostOwnImage;
    public LikeButton postLikeButton;
    public ImageButton postCommentButton;
    public ImageButton postShareButton;

    public String mPostOwnerID;
    String mAnimation;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);

        //collapsing toolbar setting

        /*Toolbar toolbar = findViewById(R.id.show_post_collapsing_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = findViewById(R.id.show_post_collapsing_toolbar_layout);
        dynamicToolbarColor();
        toolbarTextAppernce();*/



        //Image loader initialization for offline feature
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ShowPostActivity.this)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();


        postImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_camera_icon)
                .showImageForEmptyUri(R.drawable.ic_camera_icon)
                .showImageOnFail(R.drawable.ic_camera_icon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        //image loader end


        postUserName =findViewById(R.id.post_user_name);
        postCaption = findViewById(R.id.post_caption);
        postDateTime =findViewById(R.id.post_time_date);
        postLocaiton = findViewById(R.id.post_location);
        postUserImage = findViewById(R.id.post_user_single_imagee);
        postLikeButton = findViewById(R.id.post_like_button);
        postLikeCount = findViewById(R.id.post_like_number);
        postCommentCount = findViewById(R.id.show_post_comment_count);

        mShowPostOwnImage = findViewById(R.id.show_post_own_image);
        imageLoader.displayImage(MainActivity.mUserImage, mShowPostOwnImage, postImageOptions);



        postLikeCount.setOnClickListener(v -> {
            addRating(mUserID , 1);
            Intent showPeopleIntent = new Intent(ShowPostActivity.this , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "likes");
            showPeopleIntent.putExtra("user_id" ,mPostID );
            startActivity(showPeopleIntent);
        });
        mPostID = getIntent().getStringExtra("postID");
        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final ImageView postImageView = findViewById(R.id.show_post_collapsing_image);
        firebaseFirestore.collection("posts").document(mPostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public static final String TAG = "SHow Post";

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        Posts post = task.getResult()
                                .toObject(Posts.class);
                        String postImage = post.getPost_image_url();
                        String postCaptionText = post.getCaption();
                        String postTime = post.getTime_and_date();
                        String postOwnerID = post.getUser_id();
                        String postOwnerThumbImage = post.getUser_thumb_image();
                        String postOwnerName = post.getUser_name();
                        long likeCount = post.getLike_cnt();
                        long commentCount = post.getComment_cnt();


                        if(postImage.equals("default")){
                            postImageView.setVisibility(View.GONE);
                            //toolbar.setVisibility(View.GONE);
                            //collapsingToolbarLayout.setVisibility(View.GONE);
                        }

                        setLikeCount(likeCount);
                        setCommentCount(commentCount);

                        //collapsingToolbarLayout.setTitle(postOwnerName);
                        postUserName.setText(postOwnerName);
                        imageLoader.displayImage(postOwnerThumbImage, postUserImage, postImageOptions);
                        imageLoader.displayImage(postImage, postImageView, postImageOptions);


                        mPostOwnerID = postOwnerID;
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



        //setting like count
        FirebaseFirestore.getInstance().collection("likes/" + mPostID + "/likes").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                int count = documentSnapshots.size();
                String cnt = Integer.toString(count);
                firebaseFirestore.collection("posts").document(mPostID).update("like_cnt" , count);
                if (count == 1) {
                    postLikeCount.setText(cnt);
                } else {
                    postLikeCount.setText(cnt);
                }

            } else {
                postLikeCount.setText("0");
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

                Likes mLikes = new Likes(mUserID , MainActivity.mUserName , MainActivity.mUserThumbImage ,likeNotificatoinPushID , time_stamp);
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

              /*  firebaseFirestore.collection("notifications/"+mPostOwnerID+"/notificatinos").document(likeNotificatoinPushID).set(pushNoti);
                firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID).set(pushNoti);
                firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID).set(mLikes);*/

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
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Comments singleComment = doc.getDocument().toObject(Comments.class);
                        commentList.add(singleComment);
                        mPostCommentAdapter.notifyDataSetChanged();
                    }
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


/*
            firebaseFirestore.collection("notifications/"+mPostOwnerID+"/notificatinos").document(commentNotificatoinPushID).set(pushNoti);
            firebaseFirestore.collection("comments/"+mPostID+"/comments").add(comment);
            firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(commentNotificatoinPushID).set(pushNoti);*/
        });

    }



    private void dynamicToolbarColor() {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_person_icon);
        Palette.from(bitmap).generate(palette -> {
            //collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
            //collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark));
        });
    }


    private void toolbarTextAppernce() {
        //collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        //collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
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




   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }*/
}


