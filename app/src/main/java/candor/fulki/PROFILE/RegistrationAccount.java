package candor.fulki.PROFILE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationAccount extends AppCompatActivity {


    // widgets
    private TextView mRegMale , mRegFemale , mRegError;
    private Button mRegCamera , mRegSave;
    private CircleImageView mRegPhoto;
    private ProgressDialog mProgress;
    private EditText mRegName , mRegUserName;

    //variables
    private int mGender = 2;
    Uri imageUri;
    private String mainImageUrl = "";
    private String  thumbImageUrl = "";
    private String mUserID;
    private int successPhotoUpload = 0;

    //firebase
   private StorageReference mStorage;
   private FirebaseFirestore firebaseFirestore;
   private StorageReference imageFilePath;
   private StorageReference thumbFilePath;
   private byte[] thumb_byte;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_account);


        // setting toolbar staff
        /*Toolbar mSettingsToolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(mSettingsToolbar);*/
        getSupportActionBar().setTitle("Create Your Account ");


        //widgets
        mRegName = findViewById(R.id.reg_name);
        mRegUserName = findViewById(R.id.reg_user_name);
        mRegCamera = findViewById(R.id.reg_camera);
        mRegSave = findViewById(R.id.reg_save);
        mRegPhoto = findViewById(R.id.reg_photo);
        mRegMale =findViewById(R.id.reg_male);
        mRegFemale =findViewById(R.id.reg_female);
        mRegError = findViewById(R.id.reg_error);
        mRegUserName.addTextChangedListener(filterTextWatcher);





        //firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRegMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGender = 1; //male
                mRegMale.setBackgroundResource(R.drawable.textview_selected);
                mRegFemale.setBackgroundResource(R.drawable.textview_not_selected);
            }
        });

        mRegFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGender = 0 ;//female
                mRegMale.setBackgroundResource(R.drawable.textview_not_selected);
                mRegFemale.setBackgroundResource(R.drawable.textview_selected);
            }
        });



        mRegPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions functions = new Functions();
                functions.BringImagePicker(RegistrationAccount.this);
            }
        });

        mRegCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
            }
        });


        mRegSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //now saving the data to firestore
                String name = mRegName.getText().toString();
                String userName = mRegUserName.getText().toString();
                String gender = "others";




                if(imageUri==null){
                    Toast.makeText(RegistrationAccount.this, "please select and image", Toast.LENGTH_SHORT).show();
                }else if(name==null){
                    Toast.makeText(RegistrationAccount.this, "please give us your name", Toast.LENGTH_SHORT).show();
                }else if(userName==null) {
                    Toast.makeText(RegistrationAccount.this, "please give us your user name", Toast.LENGTH_SHORT).show();
                }else if(mGender==2){
                    Toast.makeText(RegistrationAccount.this, "Please Select your gender", Toast.LENGTH_SHORT).show();
                }else if(userName.length()<6){
                    mRegUserName.setError("username not specified !");
                    Toast.makeText(RegistrationAccount.this, "User Name must be atleast 6 charactersr", Toast.LENGTH_SHORT).show();
                } else{



                    mProgress = new ProgressDialog(RegistrationAccount.this);
                    mProgress.setTitle("Saving Data.......");
                    mProgress.setMessage("please wait while we create your account");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();


                    //uploading the main image
                    imageFilePath.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                Uri downloadUrlImage = taskSnapshot.getDownloadUrl();
                                mainImageUrl =  downloadUrlImage.toString();


                                //uploading the thumb image
                                UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                                uploadThumbTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                                        Log.w("Thumb  Photo Upload:  " , exception);
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                        Uri downloadUrlThumb = taskSnapshot.getDownloadUrl();
                                        thumbImageUrl  = downloadUrlThumb.toString();




                                        //now saving the data to firestore
                                        String name1 = mRegName.getText().toString();
                                        String userName1 = mRegUserName.getText().toString();
                                        String gender1 = "others";


                                        if(mGender==1){
                                            gender1 = "male";
                                        }else if(mGender==0){
                                            gender1 = "female";
                                        }


                                        Map < String, String> userMap = new HashMap<>();

                                        userMap.put("name" , name1);
                                        userMap.put("user_name" , userName1);
                                        userMap.put("gender" , gender1);
                                        userMap.put("image" , mainImageUrl);
                                        userMap.put("thumb_image",thumbImageUrl);


                                        userMap.put("bio" , "");
                                        userMap.put("division", "Select One");
                                        userMap.put("blood_group", "Select One");
                                        userMap.put("birth_date" , "");
                                        userMap.put("contact_no" , "");
                                        userMap.put("email" , "");
                                        userMap.put("timestamp" , "");
                                        userMap.put("district" , "");
                                        userMap.put("lat" , "");
                                        userMap.put("lng" , "");
                                        userMap.put("rating" , "0");
                                        userMap.put("user_id" , mUserID);


                                        firebaseFirestore.collection("users").document(mUserID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public static final String TAG ="registration process " ;

                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    Intent mainIntent = new Intent(RegistrationAccount.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    mProgress.dismiss();
                                                    finish();

                                                }else{
                                                    mProgress.dismiss();
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onComplete: "+ error);
                                                }
                                            }
                                        });
                                    }
                                });


                            })
                            .addOnFailureListener(exception -> {
                                mProgress.dismiss();
                                Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                                Log.w("Main Photo Upload   :  " , exception);
                            });

                }
            }
        });
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




    public void checkPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(RegistrationAccount.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RegistrationAccount.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(RegistrationAccount.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {
                Functions functions = new Functions();
                functions.BringImagePicker(RegistrationAccount.this);
            }
        }
        else{
            Functions functions = new Functions();
            functions.BringImagePicker(RegistrationAccount.this);
        }
    }
}
