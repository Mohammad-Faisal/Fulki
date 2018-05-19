package candor.fulki.NOTIFICATION;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.HOME.Comments;
import candor.fulki.R;



public class NotificationFragment extends Fragment {


    private static final String TAG = "NotificationFragment";

    private List<Notifications> notifications;
    private List<String> notificationIDs;
    private NotificationAdapter mNotificationAdapter;
    private String mUserID;



    public NotificationFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


            //setting RecyclerView
            notifications = new ArrayList<>();
            notificationIDs = new ArrayList<>();
            RecyclerView recyclerView = view.findViewById(R.id.notification_recycler);
            recyclerView.hasFixedSize();
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mNotificationAdapter = new NotificationAdapter(notifications, notificationIDs, getContext() , getActivity());
            recyclerView.setAdapter(mNotificationAdapter);

            FirebaseFirestore firebaseFirestore;
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("notifications/"+mUserID+"/notificatinos").orderBy("time_stamp" , Query.Direction.DESCENDING);
            nextQuery.addSnapshotListener((documentSnapshots, e) -> {
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Notifications singleNotifications = doc.getDocument().toObject(Notifications.class);
                        notifications.add(singleNotifications);
                        mNotificationAdapter.notifyDataSetChanged();
                    }else{
                        Log.d(TAG, "onEvent: notification type is not added");
                    }
                }
            });
        }



        return view;
    }

}
