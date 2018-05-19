package candor.fulki.HOME;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.GENERAL.MainActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private RecyclerView recyclerView;
    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mHomeAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoad = true;
    private ProgressDialog mProgress;
    EditText mCreatePostText;
    private ImageButton mCreatePostImage;
    private boolean TextPost = false;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.fragment_home, container, false);





        mCreatePostText = view.findViewById(R.id.home_create_post);
        mCreatePostText.addTextChangedListener(filterTextWatcher);
        mCreatePostImage = view.findViewById(R.id.create_post_image);
        final CircleImageView imageView = view.findViewById(R.id.home_circle_image);



        //isFirstLoaded variable ta use kora hoise jeno onno kono user notun kono post dile eikhane kono genjam srishti na hoy jemon dhora jak onno kono user jodi hothat post dey tahole problem hobe


        if(FirebaseAuth.getInstance().getCurrentUser() !=null){


            //setting up the recyclerview
            posts = new ArrayList<>();
            recyclerView = view.findViewById(R.id.home_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setNestedScrollingEnabled(false);
            mHomeAdapter = new HomeAdapter(recyclerView, posts,getActivity(), getContext());
            recyclerView.setAdapter(mHomeAdapter);



            //setting users image beside the post textview
            final String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users").document(mUserID).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String thumbImage  =  task.getResult().get("thumb_image").toString();

                        ImageLoader imageLoader;
                        DisplayImageOptions userImageOptions;
                        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).build();
                        userImageOptions = new DisplayImageOptions.Builder()
                                .showImageOnLoading(R.drawable.ic_blank_profile)
                                .showImageForEmptyUri(R.drawable.ic_blank_profile)
                                .showImageOnFail(R.drawable.ic_blank_profile)
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .considerExifParams(true)
                                .build();
                        imageLoader = ImageLoader.getInstance();
                        imageLoader.init(config);
                        imageLoader.displayImage(thumbImage, imageView, userImageOptions);
                    }
                }
            });



            mCreatePostImage.setOnClickListener(v -> {

                if(TextPost){
                    mProgress = new ProgressDialog(getContext());
                    mProgress.setTitle("Posting.......");
                    mProgress.setMessage("please wait while we upload your post");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();


                    //onclick
                    DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document();
                    String postPushId = ref.getId();


                    //time and date
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");
                    final String cur_time_and_date = sdf.format(c.getTime());
                    //timestamp
                    long timestamp = 1* new Date().getTime();
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();

                    String Caption = mCreatePostText.getText().toString();

                    Map<String , Object> postMap = new HashMap<>();
                    postMap.put("user_id" , mUserID);
                    postMap.put("image_url" , "default");
                    postMap.put("thumb_image_url" , "default");
                    postMap.put("caption" , Caption);
                    postMap.put("time_and_date" , cur_time_and_date);
                    postMap.put("timestamp" ,timestamp );
                    postMap.put("post_push_id" , postPushId);
                    postMap.put("location" , "default");


                    firebaseFirestore.collection("posts").document(postPushId).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgress.dismiss();
                            if(task.isSuccessful()){
                                mProgress.dismiss();
                                mCreatePostText.setText("");
                            }else{
                                mProgress.dismiss();
                            }
                        }
                    });

                }else{
                    BringImagePicker();
                }
                });


            //loading the data for the first time
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("posts").orderBy("timestamp" , Query.Direction.DESCENDING).limit(3);
            nextQuery.addSnapshotListener(getActivity() , new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
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
            });


            //load more posts
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        loadMorePost();
                    }
                }
            });
        }


        return view;
    }


    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("posts")
                .orderBy("timestamp" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity() , new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
                            posts.add(singlePosts);
                            mHomeAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Intent CreatePostIntent = new Intent(getContext() , CreatePhotoPostActivity.class);
                CreatePostIntent.putExtra("imageUri" , imageUri.toString());
                startActivity(CreatePostIntent);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(10, 8)
                .setMinCropResultSize(512 , 512)
                .start(getActivity());
    }

}
