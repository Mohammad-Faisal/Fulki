package candor.fulki.HOME;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import candor.fulki.GENERAL.FileUtil;
import candor.fulki.R;
import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {


    private static final String TAG = "CreatePostActivity";

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    ArrayList<Uri> postImageUriArrayList = new ArrayList<Uri>();


    private EditText mCaption;
    private EditText mLocation;

    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;
    private byte[] thumb_byte;

    String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //docData.put("listExample", Arrays.asList(1, 2, 3));

    ArrayList<String> imageUrls = new ArrayList<>();
    ArrayList<String> thumbImageUrls = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mCaption = findViewById(R.id.caption);
        mLocation = findViewById(R.id.location);
    }
    

    public void post(View view) throws IOException {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("test").document("one").get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot!=null){
                String p =  documentSnapshot.getData().get("images").toString();
                Log.d(TAG, "onSuccess:     found images from firestore " + p);
                mCaption.setText(p);
            }
        });


        //uploadImages();
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void uploadImages() throws IOException {


        Log.d(TAG, "uploadImages:    called !!");

        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


        ViewGroup holder = findViewById(R.id.post_image_holder);
        for(int i=0; i<holder.getChildCount()-1; i++){
            Uri imageUri = (Uri) holder.getChildAt(i).getTag();

            imageUri  = postImageUriArrayList.get(i);
            
            if(imageUri!=null){
                Log.d(TAG, "uploadImages:    image uri is not null");
            }else{
                Log.d(TAG, "uploadImages: image uri is null !");
            }



            File actualImage = FileUtil.from(CreatePostActivity.this, ((Uri)holder.getChildAt(i).getTag()));
            Bitmap compressedImageFile = new Compressor(CreatePostActivity.this)
                    .setMaxWidth(1024)
                    .setMaxHeight(768)
                    .setQuality(85)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToBitmap(actualImage);


            byte[] imageByte  = getFileDataFromDrawable(compressedImageFile);
            byte[] thumbByte = getFileDataFromDrawable(compressedImageFile);


           /* byte[] imageByte  = CompressImage( imageUri , CreatePostActivity.this , 85);
            byte[] thumbByte = CompressImage( imageUri , CreatePostActivity.this , 30);*/

            final String randomName = random();



            imageFilePath = FirebaseStorage.getInstance().getReference().child("post_images").child(mUserID).child(randomName+".jpg");
            thumbFilePath = FirebaseStorage.getInstance().getReference().child("post_thumb_images").child(mUserID).child(randomName+".jpg");


            UploadTask uploadThumbTask = thumbFilePath.putBytes(thumbByte);
            UploadTask uploadImageTask = thumbFilePath.putBytes(imageByte);

            uploadImageTask.addOnSuccessListener(taskSnapshot -> {
                if(uploadImageTask.isSuccessful()){
                    Uri downloadUrlThumb = taskSnapshot.getDownloadUrl();
                    final String imageUrl  = downloadUrlThumb.toString();

                    uploadThumbTask.addOnSuccessListener(taskSnapshot12 -> {
                        if(uploadThumbTask.isSuccessful()){
                            Uri downloadUrlThumb1 = taskSnapshot12.getDownloadUrl();
                            final String thumbImageUrl  = downloadUrlThumb1.toString();

                            Log.d(TAG, "uploadImages:     main and thumb image uploading is succesful !!");

                            imageUrls.add(imageUrl);
                            thumbImageUrls.add(thumbImageUrl);



                            //firebaseFirestore.collection("test").document("one").set(docData).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess:      images should be now uploaded in as Hashmap !"));


                        }else{
                            Toast.makeText(CreatePostActivity.this, "Thumb Image Upload Failed !", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
                }
            });


        }





        /*//uploading the main image
        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrlImage = taskSnapshot.getDownloadUrl();
                    final String mainImageUrl =  downloadUrlImage.toString();


                    //uploading the thumb image
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        Toast.makeText(CreatePhotoPostActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Thumb  Photo Upload:  " , exception);
                    }).addOnSuccessListener(taskSnapshot1 -> {
                        Uri downloadUrlThumb = taskSnapshot1.getDownloadUrl();
                        final String thumbImageUrl  = downloadUrlThumb.toString();


                        DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document();
                        String postPushId = ref.getId();
                        long timestamp = 1* new Date().getTime();
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        Map<String , Object> postMap = new HashMap<>();



                        postMap.put("user_id" , mUserID);
                        postMap.put("user_name" , mUserName);
                        postMap.put("user_thumb_image" , mUserThumbImage);

                        postMap.put("post_image_url" , mainImageUrl);
                        postMap.put("post_thumb_image_url" , thumbImageUrl);

                        postMap.put("caption" , Caption);
                        postMap.put("time_and_date" , cur_time_and_date);
                        postMap.put("timestamp" ,timestamp );
                        postMap.put("post_push_id" , postPushId);
                        postMap.put("location" , "");

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
                                    addRating(mUserID , 15);
                                    Toast.makeText(CreatePhotoPostActivity.this, "Success !", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(CreatePhotoPostActivity.this , HomeActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }else{
                                    Toast.makeText(CreatePhotoPostActivity.this, "There was an error !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    });
                })
                .addOnFailureListener(exception -> {
                    mProgress.dismiss();
                    Toast.makeText(CreatePhotoPostActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                    Log.w("Main Photo Upload   :  " , exception);
                });*/

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




    private  final byte[] CompressImage(Uri imagetUri , Activity context , int quality){
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
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        final byte[] thumb_byte = baos.toByteArray();
        return thumb_byte;
    }



    public void addImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){
                    Uri mImageUri=data.getData();
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();

                            //from maps

                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                ViewGroup holder;

                                holder = findViewById(R.id.post_image_holder);

                                int dimen = (int) getResources().getDimension(R.dimen.feedback_image_size);

                                ImageView image = new ImageView(this);
                                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(dimen, dimen));
                                image.setMaxHeight(dimen);
                                image.setMaxWidth(dimen);
                                image.setImageBitmap(bitmap);
                                image.setTag(uri);

                                holder.addView(image, holder.getChildCount()-1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            postImageUriArrayList.add(uri);

                            //end maps


                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }
                        Log.v(TAG, "Selected Images" + postImageUriArrayList.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
