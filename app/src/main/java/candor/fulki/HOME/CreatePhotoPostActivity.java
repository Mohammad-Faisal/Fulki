package candor.fulki.HOME;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.PROFILE.RegistrationAccount;
import candor.fulki.R;
import id.zelory.compressor.Compressor;

import static candor.fulki.GENERAL.MainActivity.mUserName;

public class CreatePhotoPostActivity extends AppCompatActivity {


    private static final String TAG = "CreatePhotoPostActivity" ;
    //Variables
    Uri imageUri;

    ImageView mPhotoPostImage;
    EditText mPhotoPostCaption;
    Button mPhotoChange , mPhotoUpload;
    private ProgressDialog mProgress;


    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;
    private byte[] thumb_byte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_photo_post);





        //widgets
        mPhotoPostImage = findViewById(R.id.create_photo_post_image);
        mPhotoPostCaption = findViewById(R.id.photo_post_caption);
        mPhotoChange = findViewById(R.id.photo_post_change_pic);
        mPhotoUpload = findViewById(R.id.photo_post_upload);



        //getting the image uri
        Intent intent = getIntent();
        String image_path= intent.getStringExtra("imageUri");
        imageUri = Uri.parse(image_path);
        mPhotoPostImage.setImageURI(imageUri);
        thumb_byte = CompressImage(imageUri , this );



        /*//Toolbar
        Toolbar photoToolbar = findViewById(R.id.toolbar_photo_post);
        setSupportActionBar(photoToolbar);
        getSupportActionBar().setTitle("Photo Upload");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        mPhotoChange.setOnClickListener(v -> BringImagePicker());

        mPhotoUpload.setOnClickListener(v -> post());
    }


    public static String random() {
        int MAX_LENGTH = 100;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mPhotoPostImage.setImageURI(imageUri);
                Functions functions = new Functions();
                thumb_byte = CompressImage(imageUri , this);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(10, 9)
                .setMinCropResultSize(512 , 512)
                .start(CreatePhotoPostActivity.this);
    }


    // give image uri and context and return byte array
    private  final byte[] CompressImage(Uri imagetUri , Activity context){
        Bitmap thumb_bitmap = null;
        File thumb_file = new File(imagetUri.getPath());
        try {
            thumb_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50)
                    .compressToBitmap(thumb_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        final byte[] thumb_byte = baos.toByteArray();
        return thumb_byte;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                post();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void post(){
        final String Caption = mPhotoPostCaption.getText().toString();


        mProgress = new ProgressDialog(CreatePhotoPostActivity.this);
        mProgress.setTitle("Uploading Image.......");
        mProgress.setMessage("please wait while we upload your post");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");
        final String cur_time_and_date = sdf.format(c.getTime());


        final String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String randomName = random();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



        //creating filepath for uploading the image
        imageFilePath = FirebaseStorage.getInstance().getReference().child("post_images").child(mUserID).child(randomName+".jpg");
        thumbFilePath = FirebaseStorage.getInstance().getReference().child("post_thumb_images").child(mUserID).child(randomName+".jpg");



        //uploading the main image
        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrlImage = taskSnapshot.getDownloadUrl();
                    final String mainImageUrl =  downloadUrlImage.toString();


                    //uploading the thumb image
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        Toast.makeText(CreatePhotoPostActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Thumb  Photo Upload:  " , exception);
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrlThumb = taskSnapshot.getDownloadUrl();
                            final String thumbImageUrl  = downloadUrlThumb.toString();


                            DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document();
                            String postPushId = ref.getId();

                            long timestamp = 1* new Date().getTime();

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();


                            Map<String , Object> postMap = new HashMap<>();


                            postMap.put("user_id" , mUserID);
                            postMap.put("user_name" , mUserName);
                            postMap.put("image_url" , mainImageUrl);
                            postMap.put("thumb_image_url" , thumbImageUrl);
                            postMap.put("caption" , Caption);
                            postMap.put("time_and_date" , cur_time_and_date);
                            postMap.put("timestamp" ,timestamp );
                            postMap.put("post_push_id" , postPushId);
                            postMap.put("location" , "default");
                            postMap.put("like_cnt" , 0);
                            postMap.put("comment_cnt" ,0);
                            postMap.put("share_cnt" ,0);

                            //setting the path to file so that later we can delete this post
                            PostFiles postFiles = new PostFiles("post_images/"+mUserID+"/"+randomName+".jpg" ,"post_thumb_images/"+mUserID+"/"+randomName+".jpg", postPushId);
                            firebaseFirestore.collection("images").document(mUserID).collection("posts").document(postPushId).set(postFiles);
                            firebaseFirestore.collection("posts").document(postPushId).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mProgress.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(CreatePhotoPostActivity.this, "Success !", Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(CreatePhotoPostActivity.this , MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }else{
                                        Toast.makeText(CreatePhotoPostActivity.this, "There was an error !", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mProgress.dismiss();
                        Toast.makeText(CreatePhotoPostActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Main Photo Upload   :  " , exception);
                    }
                });

    }



}
