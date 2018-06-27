package candor.fulki.GENERAL;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import candor.fulki.PROFILE.RegistrationAccount;
import candor.fulki.PROFILE.UserBasic;
import id.zelory.compressor.Compressor;

public class Functions {

    UserBasic userBasic = new UserBasic();

    public Functions() {
    }





    // give image uri and context and return byte array
    public  final byte[] CompressImage(Uri imagetUri , Activity context){
        Bitmap thumb_bitmap = null;
        File thumb_file = new File(imagetUri.getPath());
        try {
            thumb_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(30)
                    .compressToBitmap(thumb_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        final byte[] thumb_byte = baos.toByteArray();
        return thumb_byte;
    }


    public void BringImagePicker(Activity context) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context);
    }

    public long getTimeStamp(){
        long timestamp = 1* new Date().getTime();
        return timestamp;
    }

    public UserBasic getUserBasicData(String mUserID){
        //setting user details

        FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String mUserName = task.getResult().getString("name");
                    String mUserImage = task.getResult().getString("thumb_image");
                    String mUserThumbImage = task.getResult().getString("image");
                    UserBasic mUserBasic = new UserBasic(mUserName , mUserThumbImage , mUserImage);
                    userBasic =  mUserBasic;
                }
            }
        });
        return userBasic;
    }

}
