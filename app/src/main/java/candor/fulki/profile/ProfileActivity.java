package candor.fulki.profile;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.chat.ChatActivity;
import candor.fulki.chat.InboxActivity;
import candor.fulki.explore.ExploreActivity;
import candor.fulki.general.MainActivity;
import candor.fulki.home.CombinedHomeAdapter;
import candor.fulki.home.CombinedPosts;
import candor.fulki.home.HomeActivity;
import candor.fulki.home.HomeAdapter;
import candor.fulki.home.Posts;
import candor.fulki.explore.people.Ratings;
import candor.fulki.MapsActivity;
import candor.fulki.notification.NotificationActivity;
import candor.fulki.notification.Notifications;
import candor.fulki.R;
import candor.fulki.search.SearchActivity;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";

    String mUserID = "";
    String mUserImage;
    String mUserName;
    String mUserThumbImage;




    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private HomeAdapter mProfilePostAdapter;

    private CombinedHomeAdapter mCombinedHomeAdapter;
    private List<CombinedPosts> combinedPosts;

    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoad = false;


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
                finish();
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





        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCurProfileId = getIntent().getStringExtra("userID");
        ownProfile = mCurProfileId.equals(mUserID);


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


        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseFirestore.collection("users").document(mUserID).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                mUserName = documentSnapshot.getString("name");
                mUserImage= documentSnapshot.getString("image");
                mUserThumbImage =documentSnapshot.getString("thumb_image");

            }
        }).addOnFailureListener(e -> {
            mUserImage = MainActivity.mUserImage;
            mUserName = MainActivity.mUserName;
            mUserThumbImage = MainActivity.mUserThumbImage;
        });



        //setting up recycler view
        combinedPosts = new ArrayList<>();
        mCombinedHomeAdapter = new CombinedHomeAdapter(combinedPosts,ProfileActivity.this, ProfileActivity.this);
        mProfilePostsRecycelr.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        mProfilePostsRecycelr.setAdapter(mCombinedHomeAdapter);
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
            chatIntent.putExtra("user_id" , mCurProfileId);
            startActivity(chatIntent);
        });

        final CircleImageView mProfileImageView =findViewById(R.id.profile_image);
        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileImageIntent = new Intent(ProfileActivity.this , ShowProfileImageActivity.class);
                profileImageIntent.putExtra("url", mCurUserImage);
                profileImageIntent.putExtra("name", mCurUserName);
                startActivity(profileImageIntent);
            }

        });
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
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(mCurUserImage, mProfileImageView);
                        getSupportActionBar().setTitle("  "+mCurUserName);
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
            FirebaseFirestore.getInstance().collection("followings/" + mCurProfileId + "/followings").get().addOnCompleteListener(task -> {
                int followings_cnt = task.getResult().size();
                mProfileFollowingsCnt.setText(followings_cnt+" following");
            });
            FirebaseFirestore.getInstance().collection("followers/" + mCurProfileId + "/followers").get().addOnCompleteListener(task -> {
                int followers_cnt = task.getResult().size();
                if(followers_cnt<2)mProfileFollowersCnt.setText(""+followers_cnt+" follower");
                else mProfileFollowersCnt.setText(""+followers_cnt+" followers");
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
                                        if(followNotificatoinPushID!=null){
                                            firebaseFirestore.collection("notifications/"+mCurProfileId+"/notificatinos").document(followNotificatoinPushID).delete();
                                        }
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
            Query nextQuery = firebaseFirestore.collection("posts").orderBy("time_stamp" , Query.Direction.DESCENDING).limit(100).whereEqualTo("primary_user_id", mCurProfileId);
            nextQuery.addSnapshotListener(ProfileActivity.this , (documentSnapshots, e) -> {
                if(documentSnapshots!=null){
                    if(!documentSnapshots.isEmpty()){
                        if(isFirstPageLoad==true){
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        }
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                CombinedPosts singlePosts = doc.getDocument().toObject(CombinedPosts.class);
                                String uid = singlePosts.getPrimary_user_id();
                                Log.d(TAG, "onCreate:  found user id   "+ uid);
                                if(isFirstPageLoad){
                                        combinedPosts.add(singlePosts);
                                }else{
                                    combinedPosts.add(0, singlePosts);
                                }
                                mCombinedHomeAdapter.notifyDataSetChanged();
                            }
                        }
                        isFirstPageLoad = false;
                    }
                }
            });


            //load more posts
            mProfilePostsRecycelr.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        //loadMorePost();
                    }
                }
            });
        }

    }



    //eitay vul ase
    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("posts")
                .orderBy("time_stamp" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .whereEqualTo("primary_user_id" , mCurProfileId)
                .limit(5);
        nextQuery.addSnapshotListener(ProfileActivity.this , (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            CombinedPosts singlePosts = doc.getDocument().toObject(CombinedPosts.class);
                            String uid = singlePosts.getPrimary_user_id();
                            combinedPosts.add(singlePosts);
                            mCombinedHomeAdapter.notifyDataSetChanged();

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
    }
    @Override
    protected void onStop() {
        super.onStop();
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
                Intent mainIntent = new Intent(ProfileActivity.this, candor.fulki.general.MainActivity.class);
                startActivity(mainIntent);
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
