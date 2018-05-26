package candor.fulki.HOME;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.CHAT.InboxActivity;
import candor.fulki.EXPLORE.ExploreActivity;
import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.GENERAL.SearchActivity;

import candor.fulki.MapsActivity;
import candor.fulki.NOTIFICATION.NotificationActivity;
import candor.fulki.PROFILE.ProfileActivity;
import candor.fulki.PROFILE.ProfileSettingsActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;


public class HomeActivity extends AppCompatActivity {



    private static final String TAG = "Home Activity";
    private static final int RC_SIGN_IN = 1;
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;
    private static final String SAVED_LAYOUT_MANAGER = "home recycler";


    public  String mUserID = null;
    public  String mUserName = "";
    public  String mUserThumbImage = "";
    public  String mUserImage = "";


    // home fragments recycler view
    private RecyclerView recyclerView;
    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mHomeAdapter;
    LinearLayoutManager mLinearManager;
    private DocumentSnapshot lastVisible = null;
    private boolean isFirstPageLoad = true;
    private ProgressDialog mProgress;
    EditText mCreatePostText;
    private ImageButton mCreatePostImage;
    private boolean TextPost = false;




    int scroll_count = 1;

    //----------- FIREBASE -----/



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(HomeActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(HomeActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(HomeActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(HomeActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(HomeActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                return true;
        }
        return false;
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkPermission();
        }


        Log.d(TAG, "onCreate:   called !!!!!!!!!!!");


        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("  YMoves");
        }



        mCreatePostText = findViewById(R.id.home_create_post);
        mCreatePostText.addTextChangedListener(filterTextWatcher);
        mCreatePostImage = findViewById(R.id.create_post_image);
        final CircleImageView imageView = findViewById(R.id.home_circle_image);



        //------------- BOTTOM NAVIGATION HANDLING ------//
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(false);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);



        getSupportActionBar().setTitle("   YMoves");
        getSupportActionBar().setElevation(1);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        if (mUserID != null) {



            firebaseFirestore =FirebaseFirestore.getInstance();
            Map< String, Object> device_id = new HashMap<>();
            String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
            device_id.put("user_id" , mUserID);
            device_id.put("device_id" , deviceTokenID);
            firebaseFirestore.collection("device_ids").document(mUserID).set(device_id);


            firebaseFirestore.collection("users").document(mUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        mUserName = documentSnapshot.getString("name");
                        mUserImage= documentSnapshot.getString("image");
                        mUserThumbImage =documentSnapshot.getString("thumb_image");
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(mUserThumbImage, imageView);
                    }
                }
            }).addOnFailureListener(e -> {
                mUserImage = MainActivity.mUserImage;
                mUserName = MainActivity.mUserName;
                mUserThumbImage = MainActivity.mUserThumbImage;
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(mUserThumbImage, imageView);
            });



            posts = new ArrayList<>();
            recyclerView = findViewById(R.id.home_recycler_view);
            mLinearManager = new LinearLayoutManager(HomeActivity.this);
            recyclerView.setLayoutManager(mLinearManager);
            recyclerView.setNestedScrollingEnabled(false);
            mHomeAdapter = new HomeAdapter(recyclerView, posts,HomeActivity.this, HomeActivity.this);
            recyclerView.setAdapter(mHomeAdapter);


            mCreatePostImage.setOnClickListener(v -> {
                if(TextPost){
                    if(isDataAvailable()){
                        uploadTextPost();
                    }else{
                        Toast.makeText(this, "Please enable your data to upload your post", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    BringImagePicker();
                }
            });

            firebaseFirestore = FirebaseFirestore.getInstance();

            loadFirstPosts();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        Log.d(TAG, "onScrolled:      reached bottom "+scroll_count++);
                        loadMorePost();
                    }
                }
            });
        }
    }

    public void uploadTextPost(){

        mProgress = new ProgressDialog(HomeActivity.this);
        mProgress.setTitle("Posting.......");
        mProgress.setMessage("please wait while we upload your post");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document();
        String postPushId = ref.getId();


        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");
        final String cur_time_and_date = sdf.format(c.getTime());
        long timestamp = 1* new Date().getTime();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        String Caption = mCreatePostText.getText().toString();

        Map<String , Object> postMap = new HashMap<>();



        postMap.put("user_id" , mUserID);
        postMap.put("user_name" , mUserName);
        postMap.put("user_thumb_image" , mUserThumbImage);
        postMap.put("post_image_url" , "default");
        postMap.put("post_thumb_image_url" , "default");
        postMap.put("caption" , Caption);
        postMap.put("time_and_date" , cur_time_and_date);
        postMap.put("timestamp" ,timestamp );
        postMap.put("post_push_id" , postPushId);
        postMap.put("location" , "");
        postMap.put("like_cnt" , 0);
        postMap.put("comment_cnt" ,0);
        postMap.put("share_cnt" ,0);





        firebaseFirestore.collection("posts").document(postPushId).set(postMap).addOnCompleteListener(task1 -> {
            mProgress.dismiss();
            if(task1.isSuccessful()){
                addRating(mUserID , 10);
                mProgress.dismiss();
                mCreatePostText.setText("");
            }else{
                mProgress.dismiss();
            }
        });
    }

    public void loadFirstPosts(){

        Query nextQuery = firebaseFirestore.collection("posts").orderBy("timestamp" , Query.Direction.DESCENDING).limit(10);
        nextQuery.addSnapshotListener(HomeActivity.this , (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
                            String uid = singlePosts.getUser_id();

                            Log.d(TAG, "onCreate:  found user id   "+ uid);
                            Log.d(TAG, "loadFirstPosts:  image for this user is  "+singlePosts.getPost_image_url());
                            if(isFirstPageLoad){
                                posts.add(singlePosts);
                            }else{
                                posts.add(0, singlePosts);
                            }
                            mHomeAdapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageLoad = false;
                }
            }else{
                Log.d(TAG, "onCreate:   document snapshot is null");
            }
        });
    }


    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("posts")
                .orderBy("timestamp" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(10);
        nextQuery.get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Log.d(TAG, "loadMorePost:    found some more data  !!!!!");
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
                            String uid = singlePosts.getUser_id();
                            posts.add(singlePosts);
                        }
                        mHomeAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        /*nextQuery.addSnapshotListener(HomeActivity.this , (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                }
            }
        });*/
    }

    private Task<Void> addRating( String mUserID  , int factor) {

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

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String userName  = s.toString();
            int length= userName.length();
            if(length>0){
                mCreatePostImage.setBackgroundResource(R.drawable.ic_send_icon);
                mCreatePostText.setMovementMethod(ScrollingMovementMethod.getInstance());

                TextPost = true;
            }else{
                mCreatePostImage.setBackgroundResource(R.drawable.ic_camera_icon);
                TextPost = false;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Intent CreatePostIntent = new Intent(HomeActivity.this , CreatePhotoPostActivity.class);
                CreatePostIntent.putExtra("imageUri" , imageUri.toString());
                startActivity(CreatePostIntent);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
                Intent mainIntent = new Intent(HomeActivity.this, candor.fulki.GENERAL.MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                Intent mProfileIntent = new Intent(HomeActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(HomeActivity.this , SearchActivity.class);

                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(HomeActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(10, 8)
                .setMinCropResultSize(512 , 512)
                .start(HomeActivity.this);
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_CHECK_PERMISSION_LOCATION);

        }
    }

    private boolean isDataAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
