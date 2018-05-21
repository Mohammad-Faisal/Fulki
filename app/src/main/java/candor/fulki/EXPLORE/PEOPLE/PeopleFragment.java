package candor.fulki.EXPLORE.PEOPLE;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.HOME.HomeActivity;
import candor.fulki.HOME.Posts;
import candor.fulki.PROFILE.ListPeopleAdapter;
import candor.fulki.PROFILE.ShowPleopleListActivity;
import candor.fulki.PROFILE.UserBasic;
import candor.fulki.R;


public class PeopleFragment extends Fragment {


    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    //containers
    private RecyclerView mPeopleList;
    private final List<UserBasic> userList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private ListPeopleAdapter mPeopleAdapter;
    String mUserID;


    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_people, container, false);


        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(mUserID!=null){
            mPeopleAdapter = new ListPeopleAdapter(userList , getContext() , getActivity());
            mPeopleList = view.findViewById(R.id.fragment_peopole_recycler);
            mLinearLayout = new LinearLayoutManager(getContext());
            mPeopleList.hasFixedSize();
            mPeopleList.setLayoutManager(mLinearLayout);
            mPeopleList.setAdapter(mPeopleAdapter);

            loadFirstData();
        }

        return view;
    }


    private void loadFirstData(){

        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("ratings")
                .orderBy("rating" , Query.Direction.DESCENDING)
                .limit(50);
        nextQuery.addSnapshotListener(getActivity() , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){
                //lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                //isFirstPageLoaded = true;
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        UserBasic basic = new UserBasic();
                        basic.setmUserID(doc.getDocument().getString("user_id"));
                        basic.setmUserName(doc.getDocument().getString("user_name"));
                        basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                        userList.add(basic);
                        mPeopleAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }


}
