package candor.fulki.GENERAL;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import candor.fulki.EXPLORE.ExploreFragment;
import candor.fulki.HOME.CreatePhotoPostActivity;

import candor.fulki.HOME.HomeActivity;
import candor.fulki.HOME.HomeAdapter;
import candor.fulki.HOME.HomeFragment;
import candor.fulki.HOME.Posts;
import candor.fulki.MapsActivity;
import candor.fulki.NOTIFICATION.NotificationActivity;
import candor.fulki.NOTIFICATION.NotificationFragment;
import candor.fulki.PROFILE.ProfileActivity;
import candor.fulki.PROFILE.ProfileFragment;
import candor.fulki.PROFILE.ProfileSettingsActivity;
import candor.fulki.PROFILE.RegistrationAccount;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;


    public static String mUserID = "";
    public static String mUserName = "";
    public static String mUserThumbImage = "";
    public static String mUserImage = "";



    //----------- FIREBASE -----//
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(1);


        mAuth = FirebaseAuth.getInstance();
        if(!isDataAvailable()){
            Toast.makeText(this, "Please enable your data to continue", Toast.LENGTH_SHORT).show();
        }
            mAuthStateListener = firebaseAuth -> {
                final FirebaseUser mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    mUserID = mUser.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("users").document(mUserID);
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                Intent regIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                                startActivity(regIntent);
                                finish();
                            }else{

                                mUserImage = task.getResult().getString("image");
                                mUserName = task.getResult().getString("name");
                                mUserThumbImage = task.getResult().getString("thumb_image");
                                mUserImage = task.getResult().getString("image");

                                Intent homeIntent = new Intent(MainActivity.this , HomeActivity.class);
                                startActivity(homeIntent);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    });
                }else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            };
        }


    private boolean isDataAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){



                FirebaseFirestore db = FirebaseFirestore.getInstance();
                mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference docRef = db.collection("users").document(mUserID);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            Intent regIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                            startActivity(regIntent);
                            finish();
                        }else{

                            Intent homeIntent = new Intent(MainActivity.this , HomeActivity.class);
                            startActivity(homeIntent);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });


            }else{
                Toast.makeText(this, "Failed for some reason please check your internet connnection ", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
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
}
