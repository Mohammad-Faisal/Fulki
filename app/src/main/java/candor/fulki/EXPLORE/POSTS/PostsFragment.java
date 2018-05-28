package candor.fulki.EXPLORE.POSTS;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.HOME.HomeAdapter;
import candor.fulki.HOME.Posts;
import candor.fulki.R;

public class PostsFragment extends Fragment {



    private RecyclerView recyclerView;
    private List< Posts> posts;
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter mHomeAdapter;


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

        posts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.fragment_post_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mHomeAdapter = new HomeAdapter(recyclerView, posts,getActivity(), getContext());
        recyclerView.setAdapter(mHomeAdapter);

        loadFirstPosts();


        return view;
    }


    public void loadFirstPosts(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("posts").orderBy("like_cnt" , Query.Direction.DESCENDING).limit(50);
        nextQuery.addSnapshotListener(getActivity() , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){
                //isFirstPageLoad = false;
                //lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Posts singlePosts = doc.getDocument().toObject(Posts.class);
                        posts.add(singlePosts);
                        /*if(isFirstPageLoad){
                            posts.add(singlePosts);
                        }else{
                            posts.add(0, singlePosts);
                        }*/
                        mHomeAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }


}
