package candor.fulki.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.chat.InboxActivity;
import candor.fulki.explore.ExploreActivity;
import candor.fulki.explore.people.Ratings;
import candor.fulki.general.Constants;
import candor.fulki.general.MainActivity;

import candor.fulki.MapsActivity;
import candor.fulki.notification.NotificationActivity;
import candor.fulki.profile.ProfileActivity;
import candor.fulki.profile.ProfileSettingsActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class HomeActivity extends AppCompatActivity {



    private static final String TAG = "Home Activity";
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;


    public  String mUserID = null;
    public  String mUserName = "";
    public  String mUserThumbImage = "";
    public  String mUserImage = "";


    // home fragments recycler view
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private CombinedHomeAdapter mCombinedHomeAdapter;
    private List< CombinedPosts> combinedPosts;
    LinearLayoutManager mLinearManager;
    private DocumentSnapshot lastVisible = null;
    private boolean isFirstPageLoad = true;
    EditText mCreatePostText;
    private ImageButton mCreatePostImage;



    int scroll_count = 1;


    public static final String Name = "nameKey";


    private void loadDetails(){
        android.content.SharedPreferences sp = getSharedPreferences(candor.fulki.general.Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        mUserID = sp.getString(candor.fulki.general.Constants.Id, null);
        mUserName = sp.getString(candor.fulki.general.Constants.Name, null);
        mUserImage = sp.getString(candor.fulki.general.Constants.Image, null);
        mUserThumbImage = sp.getString(candor.fulki.general.Constants.ThumbImage, null);
    }

    private void initRecycler(){
        combinedPosts = new ArrayList<>();
            recyclerView = findViewById(R.id.home_recycler_view);
            mLinearManager = new LinearLayoutManager(HomeActivity.this);
            recyclerView.setLayoutManager(mLinearManager);
            recyclerView.setNestedScrollingEnabled(false);
            mCombinedHomeAdapter = new CombinedHomeAdapter( combinedPosts,HomeActivity.this, HomeActivity.this);
            mCombinedHomeAdapter.setHasStableIds(true);
            recyclerView.setAdapter(mCombinedHomeAdapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkPermission();
        }

        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("  Gorjon");
            getSupportActionBar().setElevation(1);
        }

        loadDetails();
        initBottomNavigation();
        initRecycler();



        findViewById(R.id.home_floating).setOnClickListener(v -> {
            Intent createPostIntent = new Intent(HomeActivity.this , CreatePostActivity.class);
            startActivity(createPostIntent);
        });

        if (mUserID != null) {

            firebaseFirestore =FirebaseFirestore.getInstance();
            Map< String, Object> device_id = new HashMap<>();
            String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
            device_id.put("user_id" , mUserID);
            device_id.put("device_id" , deviceTokenID);
            firebaseFirestore.collection("device_ids").document(mUserID).set(device_id);



            firebaseFirestore = FirebaseFirestore.getInstance();


            AsyncFirstPostLoading runner = new AsyncFirstPostLoading();
            runner.execute();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        Timber.d("onScrolled:    reached bottom %s", scroll_count++);
                        AsyncMorePostLoading runner = new AsyncMorePostLoading();
                        runner.execute();
                    }
                }
            });
        }
    }


    @SuppressLint("StaticFieldLeak")
    public class AsyncFirstPostLoading extends AsyncTask<Void , CombinedPosts , Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            Timber.d("do in background is started !!!!");
            Query nextQuery = firebaseFirestore.collection("posts").orderBy("time_stamp" , Query.Direction.DESCENDING).limit(5);
            nextQuery.addSnapshotListener(HomeActivity.this , (documentSnapshots, e) -> {
                if(documentSnapshots!=null){
                    if(!documentSnapshots.isEmpty()){
                        if(isFirstPageLoad){
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        }
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                CombinedPosts singlePosts = doc.getDocument().toObject(CombinedPosts.class);
                                String uid = singlePosts.getPrimary_user_id();
                                Timber.d("found user id %s", uid);
                                publishProgress(singlePosts);
                            }
                        }
                        isFirstPageLoad = false;
                    }
                }else{
                    Timber.d("onCreate:   document snapshot is null");
                }
            });
            return true;
        }

        @Override
        protected void onProgressUpdate(CombinedPosts... values) {
            Timber.d("progress update is calleddd !!!!!!!!!!");
            combinedPosts.add(values[0]);
            mCombinedHomeAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            Timber.d("progress post execute is calleddd !!!!!!!!!!");
            //super.onPostExecute(s);
            //mCombinedHomeAdapter.notifyDataSetChanged();

        }

    }



    @SuppressLint("StaticFieldLeak")
    public class AsyncMorePostLoading extends AsyncTask<Void , CombinedPosts , Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            Timber.d("do in background is started !!!!");
            Query nextQuery = firebaseFirestore.collection("posts")
                    .orderBy("time_stamp" , Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);
            nextQuery.get().addOnSuccessListener(documentSnapshots -> {
                if(documentSnapshots!=null){
                    if(!documentSnapshots.isEmpty()){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){

                                CombinedPosts singlePosts = doc.getDocument().toObject(CombinedPosts.class);
                                String uid = singlePosts.getPrimary_user_id();
                                Log.d(TAG, "found user id               "+ uid);
                                publishProgress(singlePosts);
                            }
                        }
                    }
                }
            });
            return true;
        }

        @Override
        protected void onProgressUpdate(CombinedPosts... values) {
            Timber.d("progress update is calleddd !!!!!!!!!!");
            combinedPosts.add(values[0]);
            mCombinedHomeAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            Timber.d("progress post execute is calleddd !!!!!!!!!!");
            super.onPostExecute(s);
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
                Intent mainIntent = new Intent(HomeActivity.this, candor.fulki.general.MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                Intent mProfileIntent = new Intent(HomeActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(HomeActivity.this , candor.fulki.search.SearchActivity.class);
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



    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_CHECK_PERMISSION_LOCATION);

        }
    }


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

    private void initBottomNavigation() {
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(false);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);
    }


}
