package candor.fulki.PROFILE;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.HOME.HomeAdapter;
import candor.fulki.HOME.Posts;
import candor.fulki.HOME.ShowPostActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }


    String mUserID = "";

    public ImageLoaderConfiguration config;
    public DisplayImageOptions postImageOptions;
    public ImageLoader imageLoader;


    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mProfilePostAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoad = true;


    public TextView mProfileName , mProfileBio , mProfileFollow , mProfileFollowersCnt , mProfileFollowingsCnt;
    public RecyclerView mProfilePostsRecycelr , mProfileBadgesRecycler;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);



        //getting views
        mProfileName  = view.findViewById(R.id.profile_name);
        mProfileBio  = view.findViewById(R.id.profile_bio);
        mProfileFollow  = view.findViewById(R.id.profile_follow_button);
        mProfileFollowersCnt  = view.findViewById(R.id.profile_followers_cnt);
        mProfileFollowingsCnt  = view.findViewById(R.id.profile_followings_cnt);
        mProfilePostsRecycelr = view.findViewById(R.id.profile_posts_recycler);
        mProfileBadgesRecycler = view.findViewById(R.id.profile_badges_recycler);

        mProfilePostsRecycelr.setNestedScrollingEnabled(false);
        mProfileBadgesRecycler.setNestedScrollingEnabled(false);







        setupImageLoader();
        final CircleImageView mProfileImageView = view.findViewById(R.id.profile_image);
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mUser!=null){

            mUserID = mUser.getUid();

            //setting up recycler view
            posts = new ArrayList<>();
            mProfilePostAdapter = new HomeAdapter(mProfilePostsRecycelr, posts,getActivity(), getContext());
            mProfilePostsRecycelr.setLayoutManager(new LinearLayoutManager(getContext()));
            mProfilePostsRecycelr.setAdapter(mProfilePostAdapter);


            //setting user details
            FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if(task.getResult().exists()){
                            String mUserName = task.getResult().getString("name");
                            String mUserImage = task.getResult().getString("image");
                            String mUserBio = task.getResult().getString("bio");
                            if(mUserBio!=null){
                                mProfileBio.setText(mUserBio);
                            }
                            mProfileName.setText(mUserName);
                            imageLoader.displayImage(mUserImage, mProfileImageView, postImageOptions);
                        }
                    } else {
                    }
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
                            String uid = singlePosts.getUser_id();
                            if(isFirstPageLoad){
                                if(uid.equals(mUserID)){
                                    posts.add(singlePosts);
                                }
                            }else{
                                if(uid.equals(mUserID)){
                                    posts.add(0, singlePosts);
                                }
                            }
                            mProfilePostAdapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageLoad = false;
                }
            });

            //load more posts
            mProfilePostsRecycelr.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            mProfilePostAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }


    private void setupImageLoader(){
        //Image loader initialization for offline feature
        config = new ImageLoaderConfiguration.Builder(getContext())
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

}
