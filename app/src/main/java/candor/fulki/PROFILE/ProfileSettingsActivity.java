package candor.fulki.PROFILE;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
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
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    private static final String TAG = "ProfileSettingsActivity";

    // widgets
    private TextView mRegMale , mRegFemale , mRegError , mBirthDate , mRegBio;
    private TextView mCategorya , mCategoryb , mCategoryc ,mCategoryd ,mCategorye;
    private int  mCategoryaa ,  mCategorybb , mCategorycc , mCategorydd  , mCategoryee;
    private Button mRegCamera , mRegSave , mBirthSet;
    private CircleImageView mRegPhoto;
    private ProgressDialog mProgress;
    private EditText mRegName , mRegUserName , mContactNoText,  mOccupationText , mBioText, mEmailText;
    private ImageView mImg_a , mImg_b ;
    private ImageView mImg_c , mImg_d , mImg_e;

    //variables
    private String nameString , userNameString , bioString , genderString , mainImageUrlString, thumbImageUrlString ;
    private String emailString , divisionString , bloodString , birthDateString , proffessionString , contactString;


    private int mGender = 2;
    Uri imageUri = null;

    private String mUserID;

    public ImageLoaderConfiguration config;
    public DisplayImageOptions postImageOptions;
    public ImageLoader imageLoader;

    private boolean photoChanged = false;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);


        initVariables();
        setupSpinner();
        setupImageLoader();;

        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        //firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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


        FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().exists()){


                    nameString = task.getResult().getString("name");
                    userNameString = task.getResult().getString("user_name");
                    mainImageUrlString = task.getResult().getString("image");
                    thumbImageUrlString = task.getResult().getString("thumb_image");
                    genderString = task.getResult().getString("gender");
                    divisionString= task.getResult().getString("division");
                    proffessionString = task.getResult().getString("proffession");
                    contactString = task.getResult().getString("contact_no");
                    birthDateString= task.getResult().getString("birth_date");
                    bloodString = task.getResult().getString("blood_group");
                    bioString = task.getResult().getString("bio");
                    emailString = task.getResult().getString("email");


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
                   // mRegUserName.setInputType(InputType.TYPE_NULL);
                    mContactNoText.setText(contactString);
                    mOccupationText.setText(proffessionString);
                    mBirthDate.setText(birthDateString);
                    mBioText.setText(bioString);

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
            }
        });
        mRegPhoto.setOnClickListener(v -> BringImagePicker());
        mRegCamera.setOnClickListener(v -> checkPermissionStorage());
        mRegSave.setOnClickListener(v -> {
            if(setData()){
                if(imageUri==null){  //no new image
                    upload();
                }else{
                    uploadImage();
                }
            }
        });
    }


    private void uploadImage(){
        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrlImage = taskSnapshot.getDownloadUrl();
                    mainImageUrlString =  downloadUrlImage.toString();
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        Toast.makeText(ProfileSettingsActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Thumb  Photo Upload:  " , exception);
                    }).addOnSuccessListener(taskSnapshot1 -> {
                        Uri downloadUrlThumb = taskSnapshot1.getDownloadUrl();
                        thumbImageUrlString  = downloadUrlThumb.toString();
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
        mProgress.setMessage("please wait while we update your account");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();



        Map< String, Object> userMap = getAllData();

        firebaseFirestore.collection("users").document().set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            public static final String TAG ="Update account process " ;
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    //algolia index creation
                    Client client = new Client( "YWTL46QL1P" , "fcdc55274ed56d6fb92f51c0d0fc46a0" );
                    Index index = client.getIndex("users");
                    List<JSONObject> userList = new ArrayList<>();
                    userList.add(new JSONObject(userMap));
                    index.addObjectsAsync(new JSONArray(userList), null);


                    Intent mainIntent = new Intent(ProfileSettingsActivity.this, MainActivity.class);
                    startActivity(mainIntent);
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

    private boolean setData(){


        MainActivity.mUserName = nameString;
        MainActivity.mUserThumbImage = thumbImageUrlString;
        MainActivity.mUserImage = mainImageUrlString;

        nameString = mRegName.getText().toString();
        userNameString = mRegUserName.getText().toString();
        bioString = mBioText.getText().toString();
        birthDateString = mBirthDate.getText().toString();
        contactString = mContactNoText.getText().toString();
        proffessionString = mOccupationText.getText().toString();
        emailString = mEmailText.getText().toString();
        if(mGender==1){
            genderString = "male";
        }else if(mGender==0){
            genderString = "female";
        }
        if(nameString.equals("")){
            Toast.makeText(this, "Name field can't be empty !", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    Map< String, Object> getAllData() {
        String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
        Map< String, Object> userMap = new HashMap<>();
        userMap.put("name" , nameString);
        userMap.put("user_name" , userNameString);
        userMap.put("bio" , bioString);
        userMap.put("gender" , genderString);
        userMap.put("division", divisionString);
        userMap.put("blood_group", bloodString);
        userMap.put("birth_date" , birthDateString);
        userMap.put("contact_no" , contactString);
        userMap.put("image" , mainImageUrlString);
        userMap.put("thumb_image",thumbImageUrlString);
        userMap.put("email" , emailString);
        userMap.put("timestamp" , String.valueOf(new Date().getTime()));
        userMap.put("district" , "");
        userMap.put("lat" , 0);
        userMap.put("lng" , 0);
        userMap.put("rating" , "");
        userMap.put("user_id" , mUserID);
        userMap.put("device_id" , deviceTokenID);


         Map< String, String> categoryMap = new HashMap<>();
        categoryMap.put("user_name" , nameString);
        categoryMap.put("user_id" , mUserID);
        categoryMap.put("thumb_image" , thumbImageUrlString);
        firebaseFirestore.collection(bloodString).document(mUserID).set(categoryMap);


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

        Map< String, String> categoryMap = new HashMap<>();
        categoryMap.put("user_name" , nameString);
        categoryMap.put("user_id" , mUserID);
        categoryMap.put("thumb_image" , thumbImageUrlString);
        int id = view.getId();
        switch (id) {
            case R.id.settings_category_a:
                if(mCategoryaa==1){
                    mImg_a.setVisibility(View.GONE);
                    mCategorya.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategoryaa=0;
                    mCategoryList.remove("child marrige");
                    firebaseFirestore.collection("child_marrige").document(mUserID).delete();
                }else{
                    mImg_a.setVisibility(View.VISIBLE);
                    mCategorya.setBackgroundResource(R.drawable.textview_selected);
                    mCategoryaa=1;
                    mCategoryList.add("child marrige");
                    firebaseFirestore.collection("child_marrige").document(mUserID).set(categoryMap);
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
                    firebaseFirestore.collection("education").document(mUserID).set(categoryMap);

                }
                break;
            case R.id.settings_category_c:
                if(mCategorycc==1){
                    mImg_c.setVisibility(View.GONE);
                    mCategoryc.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategorycc=0;
                    mCategoryList.remove("women empowerment");
                    firebaseFirestore.collection("women_empowerment").document(mUserID).delete();
                }else{
                    mImg_c.setVisibility(View.VISIBLE);
                    mCategoryc.setBackgroundResource(R.drawable.textview_selected);
                    mCategorycc=1;
                    mCategoryList.add("women empowerment");
                    firebaseFirestore.collection("women_empowerment").document(mUserID).set(categoryMap);
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
                    firebaseFirestore.collection("environment").document(mUserID).set(categoryMap);

                }

                break;
            case R.id.settings_category_e:
                categoryMap.put("user_name" , nameString);
                categoryMap.put("user_id" , mUserID);
                if(mCategoryee==1){
                    mImg_e.setVisibility(View.GONE);
                    mCategorye.setBackgroundResource(R.drawable.textview_not_selected);
                    mCategoryee=0;
                    mCategoryList.remove("child education");
                    firebaseFirestore.collection("child_education").document(mUserID).delete();
                }else{
                    mImg_e.setVisibility(View.VISIBLE);
                    mCategorye.setBackgroundResource(R.drawable.textview_selected);
                    mCategoryee=1;
                    mCategoryList.add("child education");
                    firebaseFirestore.collection("child_education").document(mUserID).set(categoryMap);
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
        mRegCamera = findViewById(R.id.reg_camera);
        mRegSave = findViewById(R.id.reg_save);
        mRegPhoto = findViewById(R.id.reg_photo);
        mRegMale =findViewById(R.id.reg_male);
        mRegFemale =findViewById(R.id.reg_female);
        mRegError = findViewById(R.id.reg_error);
        mContactNoText = findViewById(R.id.settings_phone_no);
        mOccupationText = findViewById(R.id.settings_proffession);
        mBirthDate = findViewById(R.id.settings_birthdate_text);
        mBirthSet = findViewById(R.id.settings_birthdate_change);
        mBioText = findViewById(R.id.reg_bio);
        mEmailText = findViewById(R.id.settings_email);

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
            final String userName  = s.toString();
            int length= userName.length();
            if(length>=6){
                firebaseFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if(e==null){
                            int done = 0;

                            for(DocumentSnapshot doc : documentSnapshots){
                                String user_name = doc.getString("user_name");
                                Log.d("Registration    ;   ","now user name is   "+ user_name);
                                if(userName.equals(user_name)){
                                    mRegError.setText("! user name not available");
                                    mRegError.setTextColor(getResources().getColor(R.color.red_error));
                                    mRegError.setVisibility(View.VISIBLE);
                                    done =1;
                                }
                            }
                            if(done==0){
                                mRegError.setText("user name available :)");
                                mRegError.setTextColor(getResources().getColor(R.color.colorPrimary));
                                mRegError.setVisibility(View.VISIBLE);
                            }
                        }else{

                        }
                    }
                });
            }else{

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



