package candor.fulki.PROFILE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.CHAT.ChatActivity;
import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.NOTIFICATION.Notifications;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static candor.fulki.GENERAL.MainActivity.mUserName;
import static candor.fulki.GENERAL.MainActivity.mUserThumbImage;

public class ListPeopleAdapter extends RecyclerView.Adapter<ListPeopleAdapter.ListPeopleVIewHolder> {

    private static final String TAG = "ListPeopleAdapter";
    private List<UserBasic> mUserList;
    private FirebaseAuth mAuth;
    Context context;
    Activity activity;
    String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    boolean followState = false;
    boolean ownProfile = false;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


    public ListPeopleAdapter(List<UserBasic> mUserList , Context context , Activity activity){
        this.mUserList = mUserList;
        this.context  = context;
        this.activity = activity;
    }

    @Override
    public ListPeopleVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_person, parent , false);
        return new ListPeopleVIewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListPeopleVIewHolder holder, int position) {

        UserBasic c = mUserList.get(position);
        String mListUserID = c.getmUserID();
        String mListUserName = c.getmUserName();
        String mListUserThumbImage = c.getmUserThumbImage();

        Log.d(TAG, "onBindViewHolder:  user id : " + mListUserID);
        Log.d(TAG, "onBindViewHolder:  user name : " + mListUserName);
        Log.d(TAG, "onBindViewHolder:  user thumb image : " + mListUserThumbImage);

        if(mUserID == mListUserID)ownProfile = true;


        firebaseFirestore.collection("ratings").document(mListUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(e!=null){
                    Log.d(TAG, "onEvent: some error occured whgile getching rating data");
                }else{
                    if(documentSnapshot.exists()){
                        String rating = documentSnapshot.get("rating").toString();
                        holder.userRatingText.setText("rating  "+rating);
                    }
                }
            }
        });



        holder.setImage(mListUserThumbImage , context , R.drawable.ic_blank_profile);
        holder.setName(mListUserName);
        holder.setFollowBtn( mListUserID);


        holder.msgBtn.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 1);
            Intent chatIntent = new Intent(activity , ChatActivity.class);
            chatIntent.putExtra("user_id" , mListUserID);
            context.startActivity(chatIntent);
        });

        holder.userNameText.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 2);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mListUserID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });
        holder.userImage.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 2);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mListUserID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });


        holder.followBtn.setOnClickListener(v -> {
            if(!followState){   //currently not following after click i will follow this person


                holder.addRating(mUserID , 15);
                holder.addRating(mListUserID , 5);

                followState = true;
                holder.followBtn.setBackgroundResource(R.drawable.follow_checked);


                //  --------- GETTING THE DATE AND TIME ----------//
                Calendar c1 = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c1.getTime());

                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mListUserID+"/notificatinos").document();
                String followNotificatoinPushID = ref.getId();
                Notifications pushNoti = new Notifications( "follow" ,mUserID , mListUserID , mListUserID ,followNotificatoinPushID , time_stamp,"n"  );
                firebaseFirestore.collection("notifications/"+mListUserID+"/notificatinos").document(followNotificatoinPushID).set(pushNoti);


                //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWING THIS ID ------//
                Map<String, String> followingData = new HashMap<>();
                followingData.put("id" , mListUserID);
                followingData.put("date" , formattedDate);
                followingData.put("notificationID", followNotificatoinPushID);
                followingData.put("name" , mListUserName);
                followingData.put("thumb_image" , mListUserThumbImage);
                followingData.put("timestamp" , time_stamp);

                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mListUserID).set(followingData);

                //------- SETTING THE INFORMATION THAT NOW I AM FA FOLLOWER OF THIS ID ------//
                Map<String, String> followerData = new HashMap<>();
                followerData.put("id" , mUserID);
                followerData.put("date" , formattedDate);
                followerData.put("notificationID", followNotificatoinPushID);
                followerData.put("name" , mUserName);
                followerData.put("thumb_image" ,mUserThumbImage);
                followerData.put("timestamp" , time_stamp);

                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).set(followerData);



            }else{  //currently following this person after clickhin i will not fololw


                holder.addRating(mUserID , -15);
                holder.addRating(mListUserID , -5);
                followState = false;
                holder.followBtn.setBackgroundResource(R.drawable.user_followings);

                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                String  followNotificatoinPushID = task.getResult().getString("notificationID");
                                if(followNotificatoinPushID!=null){
                                    firebaseFirestore.collection("notifications/"+mListUserID+"/notificatinos").document(followNotificatoinPushID).delete();
                                }
                                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mListUserID).delete();
                                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).delete();
                            }
                        }
                    }
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ListPeopleVIewHolder extends RecyclerView.ViewHolder {

        public TextView userNameText;
        public CircleImageView userImage;
        public android.widget.ImageButton followBtn;
        public android.widget.ImageButton msgBtn;
        public TextView userRatingText;

        public ListPeopleVIewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.item_list_person_name);
            userImage =itemView.findViewById(R.id.item_list_person_image);
            followBtn = itemView.findViewById(R.id.item_list_person_followbtn);
            msgBtn = itemView.findViewById(R.id.item_list_person_msgwbtn);
            userRatingText = itemView.findViewById(R.id.item_list_person_rating);
        }

        public void setImage(final String imageURL , final Context context , int drawable_id ){
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_blank_profile).into(userImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(userImage);
                    }
                });
        }
        public void setName(final String name){
            userNameText.setText(name);
        }
        public void setFollowBtn(String mListUserID){
            Log.d(TAG, "setFollowBtn:     "+mUserID + "    "+mListUserID);
            if(mUserID.equals(mListUserID)){
                followBtn.setVisibility(View.GONE);
                msgBtn.setVisibility(View.GONE);
            }else{
                FirebaseFirestore.getInstance().collection("followings/" + mUserID + "/followings").document(mListUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                followState = true;
                                followBtn.setBackgroundResource(R.drawable.follow_checked);
                            } else{
                                followState = false;
                                followBtn.setBackgroundResource(R.drawable.user_followings);
                            }
                        } else {
                        }
                    }
                });
            }
        }


        private Task<Void> addRating(String mUserID  , int factor) {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "addRating:   function calledd !!!!");
            final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                    .document(mUserID);
            return firebaseFirestore.runTransaction(transaction -> {

                Ratings ratings = transaction.get(ratingRef)
                        .toObject(Ratings.class);
                long curRating = ratings.getRating();
                long nextRating = curRating + factor;

                ratings.setRating(nextRating);
                transaction.set(ratingRef, ratings);
                return null;
            });
        }


    }
}
