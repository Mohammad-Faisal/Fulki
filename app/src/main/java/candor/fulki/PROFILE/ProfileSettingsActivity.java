package candor.fulki.PROFILE;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.GENERAL.ValueAdapter;
import candor.fulki.HOME.HomeActivity;
import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    private static final String TAG = "ProfileSettingsActivity";

    // widgets
    private TextView mRegMale , mRegFemale , mRegError , mBirthDate , mRegBio , mDistrict;
    private TextView mCategorya , mCategoryb , mCategoryc ,mCategoryd ,mCategorye;
    private int  mCategoryaa ,  mCategorybb , mCategorycc , mCategorydd  , mCategoryee;
    private Button mRegCamera , mRegSave , mBirthSet;
    private CircleImageView mRegPhoto;
    private ProgressDialog mProgress;
    private EditText mRegName , mRegUserName , mContactNoText,  mOccupationText , mBioText;
    private ImageView mImg_a , mImg_b ;
    private ImageView mImg_c , mImg_d , mImg_e;

    //variables
    private String nameString = "" , userNameString = "" , bioString = "" , genderString = "" , mainImageUrlString = "", thumbImageUrlString  = "";
    private String districtString  ="", divisionString = "" , bloodString = "" , birthDateString = "" , professionString = "" , contactString = "";


    private int mGender = 2;
    Uri imageUri = null;

    private String mUserID;

    public ImageLoaderConfiguration config;
    public DisplayImageOptions postImageOptions;
    public ImageLoader imageLoader;

    //firebase
    private StorageReference mStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;
    private byte[] thumb_byte;

    Spinner mDivisionSpinner;
    Spinner mBloodSpinner;

    private DatePickerDialog.OnDateSetListener mDateListener;
    ArrayList < String > mCategoryList = new ArrayList<>();
    List < String > mDivisionList = new ArrayList<>();
    List < String > mBloodList = new ArrayList<>();


    private ValueAdapter districtValueAdapter;
    AlertDialog districtAlertDialog;
    private ArrayList<String> mStringList;
    private static final String[] districts={
             "Barguna" , "Barisal" , "Bhola",    "Jhalokati",  "Patuakhali", "Pirojpur",
            "Bandarban","Brahmanbaria",   "Chandpur", "Chittagong", "Comilla",    "Cox's Bazar","Feni",     "Khagrachhari","Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka",    "Faridpur" , "Gazipur",  "Gopalganj",  "Kishoreganj","Madaripur",  "Manikganj","Munshiganj",  "Narayanganj","Narsingdi","Rajbari","Shariatpur","Tangail",
            "Bagerhat", "Chuadanga",      "Jessore",  "Jhenaidah",  "Khulna",     "Kushtia",    "Magura",   "Meherpur",    "Narail",     "Satkhira",
            "Jamalpur", "Mymensingh",     "Netrakona","Sherpur",
            "Bogra",    "Chapainawabganj","Joypurhat","Naogaon",    "Natore",     "Pabna",      "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha",      "Kurigram", "Lalmonirhat","Nilphamari", "Panchagarh", "Rangpur",  "Thakurgaon",
            "Habiganj", "Moulvibazar",    "Sunamganj","Sylhet"
    };


    private boolean isFirstTime = true;
    String[] mDivisionArray= {};
    String[] myBloodArray  = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);


        initVariables();
        initButtons();
        initDistrictData();
        setupSpinner();
        setupImageLoader();;



        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(!mUserID.equals(null)){
            mDivisionArray  = getResources().getStringArray(R.array.divisions_array);
            myBloodArray = getResources().getStringArray(R.array.blood_array);
            getDataFromNet();
        }


        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void getDataFromNet(){
        mStorage = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().exists()){

                    isFirstTime = false;  //this user has regestered before
                    mRegUserName.setEnabled(false);
                    Log.d(TAG, "getDataFromNet:  is first time is   "+isFirstTime);

                    nameString = task.getResult().getString("name");
                    userNameString = task.getResult().getString("user_name");
                    bioString = task.getResult().getString("bio");

                    mainImageUrlString = task.getResult().getString("image");
                    thumbImageUrlString = task.getResult().getString("thumb_image");

                    genderString = task.getResult().getString("gender");
                    professionString = task.getResult().getString("profession");

                    divisionString= task.getResult().getString("division");
                    districtString = task.getResult().getString("district");

                    contactString = task.getResult().getString("contact_no");
                    birthDateString= task.getResult().getString("birth_date");
                    bloodString = task.getResult().getString("blood_group");



                    int indx_of_blood = 0;
                    if(!bloodString.equals("Select One")){
                        for(int i = 0;i<9;i++)
                        {
                            if(mBloodList.get(i).equals(bloodString))
                            {
                                indx_of_blood = i;
                                break;
                            }
                        }
                    }
                    int indx_of_division= 0;
                    if(!divisionString.equals("Select One")){
                        for(int i = 0;i<9;i++)
                        {
                            if(mDivisionList.get(i).equals(divisionString))
                            {
                                indx_of_division = i;
                                break;
                            }
                        }
                    }

                    mBloodSpinner.setSelection(indx_of_blood);
                    mDivisionSpinner.setSelection(indx_of_division);
                    mRegName.setText(nameString);
                    mRegUserName.setText(userNameString);
                    mContactNoText.setText(contactString);
                    mBirthDate.setText(birthDateString);
                    mBioText.setText(bioString);
                    mDistrict.setText(districtString);
                    mOccupationText.setText(professionString);

                    if(genderString.equals("male")){
                        mGender = 1;
                        mRegMale.setBackgroundResource(R.drawable.textview_selected);
                        mRegFemale.setBackgroundResource(R.drawable.textview_not_selected);
                    }else{
                        mGender = 0;
                        mRegMale.setBackgroundResource(R.drawable.textview_not_selected);
                        mRegFemale.setBackgroundResource(R.drawable.textview_selected);
                    }

                    imageLoader.displayImage(thumbImageUrlString, mRegPhoto, postImageOptions);
                }
            } else {
                isFirstTime = true;

            }
        });
    }

    public void initButtons(){
        mBirthSet.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: clickedd !!!!!");
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    ProfileSettingsActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    mDateListener,
                    year , month , day
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();;

            mDateListener = (view, year1, month1, dayOfMonth) -> {
                month1 = month1 +1;
                Log.d(TAG, "onDateSet: mm/dd/yyyy   "+ month1 +"/" + dayOfMonth + "/" + year1);
                birthDateString  = dayOfMonth+"/"+month1 + "/" + year1;
                mBirthDate.setText(birthDateString);
            };
        });
        mRegMale.setOnClickListener(v -> {
            mGender = 1; //male
            mRegMale.setBackgroundResource(R.drawable.textview_selected);
            mRegFemale.setBackgroundResource(R.drawable.textview_not_selected);
        });
        mRegFemale.setOnClickListener(v -> {
            mGender = 0 ;//female
            mRegMale.setBackgroundResource(R.drawable.textview_not_selected);
            mRegFemale.setBackgroundResource(R.drawable.textview_selected);
        });
        mRegPhoto.setOnClickListener(v -> BringImagePicker());
        mRegCamera.setOnClickListener(v -> checkPermissionStorage());
        mRegSave.setOnClickListener(v -> {
            if(setData()){
                if(!isDataAvailable()){
                    Toast.makeText(this, "Please connect to internet ", Toast.LENGTH_SHORT).show();
                }else{
                    if(isFirstTime){
                        Log.d(TAG, "initButtons:  trying to upload data .... for the first time ");
                        uploadImage();
                    }else{
                        Log.d(TAG, "initButtons:  trying to upload data .... for not the first time ");
                        if(imageUri==null){
                            Log.d(TAG, "initButtons:     upload function called");
                            upload();
                        }else{
                            Log.d(TAG, "initButtons:    uploadImageFunction called");
                            uploadImage();
                        }
                    }
                }
            }
        });
    }

    private void initDistrictData() {

        mStringList=new ArrayList<String>();

        for(int i=0;i<districts.length;i++){
            mStringList.add(districts[i]);
        }


        mDistrict.setOnClickListener(v -> {

            districtValueAdapter=new ValueAdapter(mStringList,ProfileSettingsActivity.this);
            districtAlertDialog = new AlertDialog.Builder(ProfileSettingsActivity.this).create();
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.custom_district_list, null);
            final EditText editText=convertView.findViewById(R.id.distric_list_search_text);
            final ListView lv =  convertView.findViewById(R.id.distric_list_listview);
            districtAlertDialog.setView(convertView);
            districtAlertDialog.setCancelable(false);

            lv.setAdapter(districtValueAdapter);
            districtAlertDialog.show();
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    districtValueAdapter.getFilter().filter(s);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            lv.setOnItemClickListener((parent, view, position, id) -> {
                mDistrict.setText(lv.getItemAtPosition(position).toString());
                districtString = lv.getItemAtPosition(position).toString();
                Log.d(TAG, "initDistrictData:    found a district  "+lv.getItemAtPosition(position).toString());
                districtAlertDialog.dismiss();
            });
        });
    }

    private void uploadImage(){

        mProgress = new ProgressDialog(ProfileSettingsActivity.this);
        mProgress.setTitle("Saving Data.......");
        mProgress.setMessage("please wait while we upload your image");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrlImage = taskSnapshot.getDownloadUrl();
                    mainImageUrlString =  downloadUrlImage.toString();
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        mProgress.dismiss();
                        Toast.makeText(ProfileSettingsActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Thumb  Photo Upload:  " , exception);
                    }).addOnSuccessListener(taskSnapshot1 -> {

                        Uri downloadUrlThumb = taskSnapshot1.getDownloadUrl();
                        thumbImageUrlString  = downloadUrlThumb.toString();
                        Log.d(TAG, "uploadImage:    is succesfull ");
                        mProgress.dismiss();
                        upload();
                    });
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(ProfileSettingsActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                    Log.w("Main Photo Upload   :  " , exception);
                });
    }

    private void upload(){

        mProgress = new ProgressDialog(ProfileSettingsActivity.this);
        mProgress.setTitle("Saving Data.......");
        mProgress.setMessage("please wait while we Save your settings");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        //blood ar category upload kora lagbe
        Map< String, String> categoryMap = new HashMap<>();

        if(!isFirstTime)firebaseFirestore.collection("categories").document(mUserID).delete();

        categoryMap.put("user_name" , nameString);
        categoryMap.put("user_id" , mUserID);
        categoryMap.put("thumb_image" , thumbImageUrlString);

        for(int i=0;i<myBloodArray.length;i++){
            String blood = myBloodArray[i];
            if(blood.equals(bloodString)){
                firebaseFirestore.collection(bloodString).document(mUserID).set(categoryMap);
            }else{
                firebaseFirestore.collection(bloodString).document(mUserID).delete();
            }
        }

        Map< String, String> odvut = new HashMap<>();
        for(int i=0;i<mCategoryList.size();i++){
            String category = mCategoryList.get(i);
            firebaseFirestore.collection(category).document(mUserID).set(categoryMap);
            odvut.put(category , category);
        }
        //category set holo
        firebaseFirestore.collection("categories").document(mUserID).set(odvut);
        if(isFirstTime){


            Map< String, Object> userMap = getAllData();
            Client client = new Client( "YWTL46QL1P" , "fcdc55274ed56d6fb92f51c0d0fc46a0" );
            Index index = client.getIndex("users");
            List<JSONObject> userList = new ArrayList<>();
            userList.add(new JSONObject(userMap));
            index.addObjectsAsync(new JSONArray(userList), null);


            //set rating
            Map< String, Object> rating = new HashMap<>();
            rating.put("rating" , 0);
            rating.put("user_name" , nameString);
            rating.put("user_id" , mUserID);
            rating.put("thumb_image" , thumbImageUrlString);

            //time stamp
            Map< String, Object> time_stamp = new HashMap<>();
            time_stamp.put("user_id" , mUserID);
            time_stamp.put("time_stamp" ,  new Date().getTime());

            // device token id
            Map< String, Object> device_id = new HashMap<>();
            String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
            device_id.put("user_id" , mUserID);
            device_id.put("device_id" , deviceTokenID);

            //user names
            Map< String, Object> user_name = new HashMap<>();
            user_name.put("user_id" , mUserID);
            user_name.put("user_name" , userNameString);



            WriteBatch writeBatch  = firebaseFirestore.batch();

            DocumentReference ratingRef = firebaseFirestore.collection("ratings").document(mUserID);
            DocumentReference timestampRef = firebaseFirestore.collection("time_stamps").document(mUserID);
            DocumentReference deviceRef = firebaseFirestore.collection("device_ids").document(mUserID);
            DocumentReference userNameRef = firebaseFirestore.collection("user_names").document(mUserID);
            DocumentReference maleRef = firebaseFirestore.collection("males").document(mUserID);
            DocumentReference femaleRef = firebaseFirestore.collection("females").document(mUserID);
            DocumentReference userRef = firebaseFirestore.collection("users").document(mUserID);

            writeBatch.set(userRef , userMap);
            writeBatch.set(ratingRef , rating);
            writeBatch.set(timestampRef , time_stamp);
            writeBatch.set(deviceRef , device_id);
            writeBatch.set(userNameRef , user_name);


            if(mGender == 0){
                Map< String, Object> female = new HashMap<>();
                female.put("user_id" , mUserID);
                writeBatch.set(femaleRef , female);
            }else{
                Map< String, Object> male = new HashMap<>();
                male.put("user_id" , mUserID);
                writeBatch.set(maleRef , male);
            }



            writeBatch.commit().addOnSuccessListener(aVoid -> {

                Intent homeIntent = new Intent(ProfileSettingsActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                mProgress.dismiss();
                finish();
                Log.d(TAG, "first time data upload is successful");

            }).addOnFailureListener(e -> {
                mProgress.dismiss();
                Log.d(TAG, "  aditional data upload not succesful");
            });

        }else{
            Map< String, Object> userMap = getAllData();
            firebaseFirestore.collection("users").document(mUserID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                public static final String TAG ="Update account process " ;
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        addRating(mUserID , 8);
                        Intent homeIntent = new Intent(ProfileSettingsActivity.this, HomeActivity.class);
                        startActivity(homeIntent);
                        mProgress.dismiss();
                        finish();
                    }else{
                        mProgress.dismiss();
                        String error = task.getException().getMessage();
                        Toast.makeText(ProfileSettingsActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: "+ error);
                    }
                }
            });
        }

    }


    private boolean isDataAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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

    private boolean setData(){

        nameString = mRegName.getText().toString();
        userNameString = mRegUserName.getText().toString();
        bioString = mBioText.getText().toString();
        birthDateString = mBirthDate.getText().toString();
        contactString = mContactNoText.getText().toString();
        professionString = mOccupationText.getText().toString();

        MainActivity.mUserName = nameString;
        MainActivity.mUserThumbImage = thumbImageUrlString;
        MainActivity.mUserImage = mainImageUrlString;

        if(mGender==1){
            genderString = "male";
        }else if(mGender==0){
            genderString = "female";
        }


        if(isFirstTime && imageUri == null){
            Toast.makeText(this, "Please take a moment to select your profile image ", Toast.LENGTH_SHORT).show();
            return false;
        } else if(nameString.equals("")){
            Toast.makeText(this, "Name field can't be empty !", Toast.LENGTH_SHORT).show();
            return false;
        }else if(userNameString.equals("")){
            Toast.makeText(this, "User name cant be empty", Toast.LENGTH_SHORT).show();
            return false;
        }else if(districtString.equals("")){
            Toast.makeText(this, "Please select your district ", Toast.LENGTH_SHORT).show();
            return false;
        }else if(mGender==2){
            Toast.makeText(this, "Please specify if you are male or female ", Toast.LENGTH_SHORT).show();
            return false;
        }else if(userNameString.length()<6) {
            mRegUserName.setError("username not specified !");
            Toast.makeText(ProfileSettingsActivity.this, "User Name must be atleast 6 charactersr", Toast.LENGTH_SHORT).show();
            return false;
        }else if(bloodString.equals("Select One")){
            Toast.makeText(this, "Please select your blood group", Toast.LENGTH_SHORT).show();
            return  false;
        } else{
            return true;
        }
    }

    Map< String, Object> getAllData() {

        Map< String, Object> userMap = new HashMap<>();

        userMap.put("name" , nameString);
        userMap.put("user_name" , userNameString);
        userMap.put("user_id" , mUserID);
        userMap.put("bio" , bioString);
        userMap.put("gender" , genderString);
        userMap.put("profession" , professionString);

        userMap.put("division", divisionString);
        userMap.put("district" , districtString);

        userMap.put("blood_group", bloodString);
        userMap.put("birth_date" , birthDateString);
        userMap.put("contact_no" , contactString);

        userMap.put("image" , mainImageUrlString);
        userMap.put("thumb_image",thumbImageUrlString);


        Log.d(TAG, "getAllData:    now the data set is like   " + userMap.toString());
        return userMap;
    }

    private void setupImageLoader(){
        config = new ImageLoaderConfiguration.Builder(ProfileSettingsActivity.this)
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

    public void categoryOnClick(View view){



        int id = view.getId();
        switch (id) {
            case R.id.settings_category_a:
                if(mCategoryaa==1){
                    mImg_a.setVisibility(View.GONE);
                    mCategorya.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategoryaa=0;
                    mCategoryList.remove("child_marrige");
                    firebaseFirestore.collection("child_marrige").document(mUserID).delete();
                }else{
                    mImg_a.setVisibility(View.VISIBLE);
                    mCategorya.setBackgroundResource(R.drawable.textview_selected);
                    mCategoryaa=1;
                    mCategoryList.add("child_marrige");
                }
                break;
            case R.id.settings_category_b:
                if(mCategorybb==1){
                    mImg_b.setVisibility(View.GONE);
                    mCategoryb.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategorybb=0;
                    mCategoryList.remove(new String("education"));
                    firebaseFirestore.collection("education").document(mUserID).delete();
                }else{
                    mImg_b.setVisibility(View.VISIBLE);
                    mCategoryb.setBackgroundResource(R.drawable.textview_selected);
                    mCategorybb=1;
                    mCategoryList.add(new String("education"));
                }
                break;
            case R.id.settings_category_c:
                if(mCategorycc==1){
                    mImg_c.setVisibility(View.GONE);
                    mCategoryc.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategorycc=0;
                    mCategoryList.remove("women_empowerment");
                    firebaseFirestore.collection("women_empowerment").document(mUserID).delete();
                }else{
                    mImg_c.setVisibility(View.VISIBLE);
                    mCategoryc.setBackgroundResource(R.drawable.textview_selected);
                    mCategorycc=1;
                    mCategoryList.add("women_empowerment");
                }

                break;
            case R.id.settings_category_d:
                if(mCategorydd==1){
                    mImg_d.setVisibility(View.GONE);
                    mCategoryd.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategorydd=0;
                    mCategoryList.remove("environment");
                    firebaseFirestore.collection("environment").document(mUserID).delete();

                }else{
                    mImg_d.setVisibility(View.VISIBLE);
                    mCategoryd.setBackgroundResource(R.drawable.textview_selected);
                    mCategorydd=1;
                    mCategoryList.add("environment");
                }
                break;
            case R.id.settings_category_e:
                if(mCategoryee==1){
                    mImg_e.setVisibility(View.GONE);
                    mCategorye.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategoryee=0;
                    mCategoryList.remove("violence");
                    firebaseFirestore.collection("violence").document(mUserID).delete();
                }else{
                    mImg_e.setVisibility(View.VISIBLE);
                    mCategorye.setBackgroundResource(R.drawable.textview_selected);
                    mCategoryee=1;
                    mCategoryList.add("violence");
                }
                break;
            default:
                int p;
        }

    }

    private void setupSpinner(){


        String[] myResArray = getResources().getStringArray(R.array.divisions_array);
        mDivisionList = Arrays.asList(myResArray);

        myResArray = getResources().getStringArray(R.array.blood_array);
        mBloodList = Arrays.asList(myResArray);


        //adapter for spinner we changed the view by using layout given bellow
        mDivisionSpinner =  findViewById(R.id.settings_division_spinner);
        ArrayAdapter<CharSequence> mDivisionAdapter = ArrayAdapter.createFromResource(this,
                R.array.divisions_array, R.layout.spinner_layout);
        mDivisionAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mDivisionSpinner.setAdapter(mDivisionAdapter);
        mDivisionSpinner.setAdapter(mDivisionAdapter);
        mDivisionSpinner.setOnItemSelectedListener(this);


        mBloodSpinner = findViewById(R.id.settings_blood_spinner);
        ArrayAdapter<CharSequence> mBloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_array, R.layout.spinner_layout);
        mBloodAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mBloodSpinner.setAdapter(mBloodAdapter);
        mBloodSpinner.setAdapter(mBloodAdapter);
        mBloodSpinner.setOnItemSelectedListener(this);
    }

    private void initVariables() {






        //widgets
        mRegName = findViewById(R.id.reg_name);
        mRegUserName = findViewById(R.id.reg_user_name);
        mRegUserName.addTextChangedListener(filterTextWatcher);
        mRegCamera = findViewById(R.id.reg_camera);
        mRegSave = findViewById(R.id.reg_save);
        mRegPhoto = findViewById(R.id.reg_photo);
        mRegMale =findViewById(R.id.reg_male);
        mRegFemale =findViewById(R.id.reg_female);
        mRegError = findViewById(R.id.reg_error);
        mOccupationText = findViewById(R.id.settings_proffession);
        mBirthDate = findViewById(R.id.settings_birthdate_text);
        mBirthSet = findViewById(R.id.settings_birthdate_change);
        mBioText = findViewById(R.id.reg_bio);
        mContactNoText = findViewById(R.id.settings_phone_no);
        mDistrict =findViewById(R.id.settings_district);




        mCategoryaa = mCategorybb = mCategorycc = mCategorydd = mCategoryee = 0;
        mCategorya = findViewById(R.id.settings_category_a);
        mCategoryb = findViewById(R.id.settings_category_b);
        mCategoryc = findViewById(R.id.settings_category_c);
        mCategoryd = findViewById(R.id.settings_category_d);
        mCategorye = findViewById(R.id.settings_category_e);

        mImg_a = findViewById(R.id.image_tik_a);
        mImg_b = findViewById(R.id.image_tik_b);
        mImg_c = findViewById(R.id.image_tik_c);
        mImg_d = findViewById(R.id.image_tik_d);
        mImg_e = findViewById(R.id.image_tik_e);


    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(isFirstTime){

                //mRegError.setVisibility(View.VISIBLE);
                final String userName  = s.toString();
                int length= userName.length();
                if(length>=6){
                    firebaseFirestore.collection("user_names").addSnapshotListener((documentSnapshots, e) -> {
                        if(e==null){
                            int done = 0;
                            if(!documentSnapshots.isEmpty()){
                                for(DocumentSnapshot doc : documentSnapshots){
                                    String user_name = doc.getString("user_name");
                                    Log.d("Registration    ;   ","now user name is   "+ user_name);
                                    if(userName.equals(user_name)){
                                        mRegError.setText("! user name not available");
                                        mRegError.setTextColor(getResources().getColor(R.color.red_error));
                                        mRegError.setVisibility(View.VISIBLE);
                                        done =1;
                                        break;
                                    }
                                }
                            }
                            if(done==0){
                                mRegError.setText("user name available :)");
                                mRegError.setTextColor(getResources().getColor(R.color.colorPrimary));
                                mRegError.setVisibility(View.VISIBLE);
                            }
                        }else{

                        }
                    });
                }else{

                }
            }else{
                mRegError.setText("you cant change your user id");
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                mRegPhoto.setImageURI(imageUri);
                Functions functions = new Functions();
                thumb_byte = functions.CompressImage(imageUri , this);


                //creating filepath for uploading the image
                imageFilePath = mStorage.child("users").child(mUserID).child("Profile").child("profile_images").child(mUserID+".jpg");
                thumbFilePath = mStorage.child("users").child(mUserID).child("Profile").child("thumb_images").child(mUserID+".jpg");


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.w("Registration  :  " , error);
            }
        }
    }


    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(ProfileSettingsActivity.this);
    }


    public void checkPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(ProfileSettingsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ProfileSettingsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(ProfileSettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {
                BringImagePicker();
            }
        }
        else{
            BringImagePicker();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mDatabaseReference.child("online").setValue("true");//offline or online
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);//offline or online
    }


    //spinner item

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        switch(parent.getId()){
            case R.id.settings_division_spinner:
                divisionString =  mDivisionSpinner.getSelectedItem().toString();
                break;
            case R.id.settings_blood_spinner:
                bloodString= mBloodSpinner.getSelectedItem().toString();
                break;
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }



}



