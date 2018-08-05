package candor.fulki.EXPLORE.POSTS;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.HOME.CombinedHomeAdapter;
import candor.fulki.HOME.CombinedPosts;
import candor.fulki.HOME.HomeActivity;
import candor.fulki.HOME.HomeAdapter;
import candor.fulki.HOME.Posts;
import candor.fulki.R;

public class PostsFragment extends Fragment {



    private RecyclerView recyclerView;
    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mHomeAdapter;


    //new
    private List<CombinedPosts> combinedPosts;
    private CombinedHomeAdapter mCombinedHomeAdapter;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        /*posts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.fragment_post_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mHomeAdapter = new HomeAdapter(recyclerView, posts,getActivity(), getContext());
        recyclerView.setAdapter(mHomeAdapter);*/




        combinedPosts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.fragment_post_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mCombinedHomeAdapter = new CombinedHomeAdapter(combinedPosts, getContext(), getActivity());
        recyclerView.setAdapter(mCombinedHomeAdapter);



        loadFirstPosts();

        return view;
    }


    public void loadFirstPosts(){
        firebaseFirestore = FirebaseFirestore.getInstance();


        Query nextQuery = firebaseFirestore.collection("posts").orderBy("like_cnt" , Query.Direction.DESCENDING).limit(50);
        nextQuery.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
/*                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }*/
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            //Toast.makeText(this, "found", Toast.LENGTH_SHORT).show();
                            CombinedPosts singlePosts = doc.getDocument().toObject(CombinedPosts.class);
                            String uid = singlePosts.getPrimary_user_id();
                            combinedPosts.add(singlePosts);
                            /*if(isFirstPageLoad){
                                combinedPosts.add(singlePosts);
                            }else{
                                combinedPosts.add(0, singlePosts);
                            }*/
                            mCombinedHomeAdapter.notifyDataSetChanged();
                        }
                    }
                    //isFirstPageLoad = false;
                }
            }else{
               // Log.d(TAG, "onCreate:   document snapshot is null");
            }
        });

    }


}
