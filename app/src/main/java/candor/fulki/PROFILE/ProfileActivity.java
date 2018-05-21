package candor.fulki.PROFILE;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.CHAT.ChatActivity;
import candor.fulki.CHAT.InboxActivity;
import candor.fulki.EXPLORE.ExploreActivity;
import candor.fulki.GENERAL.SearchActivity;
import candor.fulki.HOME.HomeActivity;
import candor.fulki.HOME.HomeAdapter;
import candor.fulki.HOME.Posts;
import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.MapsActivity;
import candor.fulki.NOTIFICATION.NotificationActivity;
import candor.fulki.NOTIFICATION.Notifications;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static candor.fulki.GENERAL.MainActivity.mUserName;
import static candor.fulki.GENERAL.MainActivity.mUserThumbImage;

public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";

    String mUserID = "";

    public ImageLoaderConfiguration config;
    public DisplayImageOptions postImageOptions;
    public ImageLoader imageLoader;
    DisplayImageOptions userImageOptions;




    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mProfilePostAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoad = true;


    private Button mProfileFollow;
    public TextView mProfileName , mProfileBio  , mProfileFollowersCnt , mProfileFollowingsCnt;
    public RecyclerView mProfilePostsRecycelr , mProfileBadgesRecycler;
    public String mCurProfileId;  //jar profile e asi amra



    boolean ownProfile = false;
    boolean followState = false;
    int active = 1;


    public String mCurUserImage;
    public String mCurUserName;
    public String mCurUserTHumbImage;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(ProfileActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                //setFragment(mHomeFragment);
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(ProfileActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                //setFragment(mExploreFragment);
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(ProfileActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(ProfileActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();;
                //setFragment(mNotificationFragment);
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(ProfileActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                //setFragment(mProfileFragment);
                return true;
        }
        return false;
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setTitle("  Flare");



        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCurProfileId = getIntent().getStringExtra("userID");
        if(mCurProfileId.equals(mUserID)){
            ownProfile = true;
        }else{
            ownProfile = false;
        }


        //------------- BOTTOM NAVIGATION HANDLING ------//
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(true);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);



        //getting views
        mProfileName  = findViewById(R.id.profile_name);
        mProfileBio  = findViewById(R.id.profile_bio);
        mProfileFollow  = findViewById(R.id.profile_follow_button);
        mProfileFollowersCnt  =findViewById(R.id.profile_followers_cnt);
        mProfileFollowingsCnt  = findViewById(R.id.profile_followings_cnt);
        mProfilePostsRecycelr = findViewById(R.id.profile_posts_recycler);
        mProfileBadgesRecycler = findViewById(R.id.profile_badges_recycler);
        mProfilePostsRecycelr.setNestedScrollingEnabled(false);
        mProfileBadgesRecycler.setNestedScrollingEnabled(false);



        setupImageLoader();

        //setting up recycler view
        posts = new ArrayList<>();
        mProfilePostAdapter = new HomeAdapter(mProfilePostsRecycelr, posts,ProfileActivity.this, ProfileActivity.this);
        mProfilePostsRecycelr.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        mProfilePostsRecycelr.setAdapter(mProfilePostAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();




        android.widget.LinearLayout cardFollowers = findViewById(R.id.profile_followers_linear);
        android.widget.LinearLayout cardFollowings = findViewById(R.id.profile_followings_linear);
        android.widget.LinearLayout sendMessage = findViewById(R.id.profile_send_message_linear);
        TextView mProfileSendMessage = findViewById(R.id.profile_send_message_text);

        if(ownProfile){
            mProfileSendMessage.setText("save a note");
        }else{
            mProfileSendMessage.setText("send message");
        }

        cardFollowers.setOnClickListener(v -> {
            addRating(mUserID , 1);
            Intent followersIntent = new Intent(ProfileActivity.this ,ShowPleopleListActivity.class);
            followersIntent.putExtra("type" , "followers");
            followersIntent.putExtra("user_id" , mCurProfileId);
            startActivity(followersIntent);
        });
        cardFollowings.setOnClickListener(v -> {
            addRating(mUserID,1);
            Intent followersIntent = new Intent(ProfileActivity.this ,ShowPleopleListActivity.class);
            followersIntent.putExtra("type" , "followings");
            followersIntent.putExtra("user_id" , mCurProfileId);
            startActivity(followersIntent);
        });
        sendMessage.setOnClickListener(v -> {
            addRating(mUserID ,1);
            addRating(mCurProfileId , 1);
            Intent chatIntent = new Intent(ProfileActivity.this  , ChatActivity.class);
            chatIntent.putExtra("user_id" , mUserID);
            startActivity(chatIntent);
        });

        final CircleImageView mProfileImageView =findViewById(R.id.profile_image);
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mUser!=null){
            FirebaseFirestore.getInstance().collection("users").document(mCurProfileId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(task.getResult().exists()){
                        mCurUserName = task.getResult().getString("name");
                        mCurUserImage = task.getResult().getString("image");
                        mCurUserTHumbImage = task.getResult().getString("thumb_image");
                        String mUserBio = task.getResult().getString("bio");
                        if(mUserBio!=null){
                            mProfileBio.setText(mUserBio);
                        }
                        mProfileName.setText(mCurUserName);
                        imageLoader.displayImage(mCurUserImage, mProfileImageView, postImageOptions);
                    }
                } else {
                }
            });
            //setting the current state of follow button
            if(!ownProfile){
                FirebaseFirestore.getInstance().collection("followings/" + mUserID + "/followings").document(mCurProfileId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            followState = true;
                            mProfileFollow.setText("following");
                            mProfileFollow.setTextColor(getResources().getColor(R.color.colorPrimaryy));
                            mProfileFollow.setBackgroundColor(getResources().getColor(R.color.White));

                        } else {
                            followState = false;
                            mProfileFollow.setText("follow");
                            mProfileFollow.setTextColor(getResources().getColor(R.color.White));
                            mProfileFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));
                        }
                    } else {
                    }
                });
            }else{
                mProfileFollow.setText("Edit Profile");
            }

            //follower and following count setting
            FirebaseFirestore.getInstance().collection("followings/" + mCurProfileId + "/followings").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    int followings_cnt = task.getResult().size();
                    mProfileFollowingsCnt.setText(followings_cnt+" following");
                }
            });
            FirebaseFirestore.getInstance().collection("followers/" + mCurProfileId + "/followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    int followers_cnt = task.getResult().size();
                    if(followers_cnt<2)mProfileFollowersCnt.setText(""+followers_cnt+" follower");
                    else mProfileFollowersCnt.setText(""+followers_cnt+" followers");
                }
            });



            mProfileFollow.setOnClickListener(view -> {

                if(ownProfile){

                    Intent showPostIntent = new Intent(ProfileActivity.this  , ProfileSettingsActivity.class);
                    Pair< View , String > pair1 = Pair.create(findViewById(R.id.profile_image) ,"profile_image");
                    Pair< View , String > pair2 = Pair.create(findViewById(R.id.profile_follow_button) ,"edit_photo");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this ,pair1 , pair2);
                    startActivity(showPostIntent , optionsCompat.toBundle());

                }else{

                    if(!followState){   //currently not following after click i will follow this person

                        addRating(mUserID , 15);
                        addRating(mCurProfileId , 5);

                        followState = true;
                        mProfileFollow.setText("following");
                        mProfileFollow.setTextColor(getResources().getColor(R.color.colorPrimaryy));
                        mProfileFollow.setBackgroundColor(getResources().getColor(R.color.White));

                        //  --------- GETTING THE DATE AND TIME ----------//
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("MMM  yyyy");
                        String formattedDate = df.format(c.getTime());

                        //building notification
                        String time_stamp = String.valueOf(new Date().getTime());
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mCurProfileId+"/notificatinos").document();
                        String followNotificatoinPushID = ref.getId();
                        Notifications pushNoti = new Notifications( "follow" ,mUserID , mCurProfileId , mCurProfileId ,followNotificatoinPushID , time_stamp,"n"  );
                        firebaseFirestore.collection("notifications/"+mCurProfileId+"/notificatinos").document(followNotificatoinPushID).set(pushNoti);


                        //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWING THIS ID ------//
                        Map<String, String> followingData = new HashMap<>();
                        followingData.put("id" , mCurProfileId);
                        followingData.put("date" , formattedDate);
                        followingData.put("notificationID", followNotificatoinPushID);
                        followingData.put("name" , mCurUserName);
                        followingData.put("thumb_image" , mCurUserTHumbImage);
                        followingData.put("timestamp" , time_stamp);
                        firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mCurProfileId).set(followingData);

                        //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWER OF THIS ID ------//
                        Map<String, String> followerData = new HashMap<>();
                        followerData.put("id" , mUserID);
                        followerData.put("date" , formattedDate);
                        followerData.put("notificationID", followNotificatoinPushID);
                        followerData.put("name" , mUserName);
                        followerData.put("thumb_image" ,mUserThumbImage);
                        followerData.put("timestamp" , time_stamp);
                        firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).set(followerData);

                    }else{  //currently following this person after clickhin i will not fololw

                        addRating(mUserID , 15);
                        addRating(mCurProfileId , 5);

                        followState = false;
                        mProfileFollow.setText("follow");
                        mProfileFollow.setTextColor(getResources().getColor(R.color.White));
                        mProfileFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));


                        firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    if(task.getResult().exists()){
                                        String  followNotificatoinPushID = task.getResult().getString("notificationID");
                                        firebaseFirestore.collection("notifications/"+mCurProfileId+"/notificatinos").document(followNotificatoinPushID).delete();
                                        firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mCurProfileId).delete();
                                        firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).delete();
                                    }
                                }
                            }
                        });
                    }
                }
                });

            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("posts").orderBy("timestamp" , Query.Direction.DESCENDING).limit(3);
            nextQuery.addSnapshotListener(ProfileActivity.this , (documentSnapshots, e) -> {
                if(!documentSnapshots.isEmpty()){
                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
                            String uid = singlePosts.getUser_id();
                            if(isFirstPageLoad){
                                if(uid.equals(mCurProfileId)){
                                    posts.add(singlePosts);
                                }
                            }else{
                                if(uid.equals(mCurProfileId)){
                                    posts.add(0, singlePosts);
                                }
                            }
                            mProfilePostAdapter.notifyDataSetChanged();
                        }
                    }
                }
                isFirstPageLoad = false;
            });


            //load more posts
            mProfilePostsRecycelr.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        loadMorePost();
                    }
                }
            });
        }

    }


    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("posts")
                .orderBy("timestamp" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(ProfileActivity.this , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){
                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Posts singlePosts = doc.getDocument().toObject(Posts.class);
                        String uid = singlePosts.getUser_id();
                        if(uid.equals(mCurProfileId)){
                            posts.add(singlePosts);
                            mProfilePostAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();

        /*ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ProfileActivity.this).build();
        userImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_blank_profile)
                .showImageForEmptyUri(R.drawable.ic_blank_profile)
                .showImageOnFail(R.drawable.ic_blank_profile)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
*/
        //mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //imageLoader.destroy();
        //mAuth.removeAuthStateListener(mAuthStateListener);
    }


    private void setupImageLoader(){
        //Image loader initialization for offline feature
        config = new ImageLoaderConfiguration.Builder(ProfileActivity.this)
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

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.action_edit_profile:
                Intent mProfileIntent = new Intent(ProfileActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(ProfileActivity.this , SearchActivity.class);

                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(ProfileActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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



}
