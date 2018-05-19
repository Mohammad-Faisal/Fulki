package candor.fulki.NOTIFICATION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.GetTimeAgo;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.HOME.ShowPostActivity;
import candor.fulki.PROFILE.ProfileActivity;
import candor.fulki.PROFILE.UserBasic;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mohammad Faisal on 1/30/2018.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notifications> mNotificationList;
    private List<String> mNotificationIDs;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Context context;
    Activity activity;

    public ImageLoader imageLoader;
    public DisplayImageOptions postImageOptions;
    public DisplayImageOptions userImageOptions;
    private String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    // ---- CONSTRUCTOR --//
    public NotificationAdapter(List<Notifications> mNotificationList , List<String> mNotificationIDs, Context context , Activity activity){
        this.mNotificationList = mNotificationList;
        this.context = context;
        this.activity  = activity;
        this.mNotificationIDs = mNotificationIDs;

        //Image loader initialization for offline feature
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();
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
    }


    @Override
    public int getItemViewType(int position) {
        if( mNotificationList.get(position).getType().equals("follow")){
            //ble ble ble
            return 1;
        }
        else{
            return 1;
        }
    }
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == 1){  //follow notification
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent , false);
            return new NotificationAdapter.NotificationViewHolder(v);
        }
        else
        return null;
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.NotificationViewHolder holder, int position) {
        final Notifications notiItem = mNotificationList.get(position);
        final String type = notiItem.getType();


        final String notiID =notiItem.getNotification_id();
        String seen_status = notiItem.getSeen();
        if(seen_status.equals("n")){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.Grey));
        }else{
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
        }

        if(type.equals("follow")){
            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();



            FirebaseFirestore.getInstance().collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String userName = task.getResult().getString("name");
                        String userThumbImage = task.getResult().getString("thumb_image");
                        holder.setImage(userThumbImage , context , R.drawable.ic_blank_profile);

                        //setting time ago for comment
                        GetTimeAgo ob = new GetTimeAgo();
                        long time = Long.parseLong(online);
                        String time_ago = ob.getTimeAgo(time ,context);
                        holder.notificationTime.setText( time_ago);

                        String sourceString =  "<b>" + userName + "<b>" + "  has followed your profile";
                        holder.notificationText.setText(Html.fromHtml(sourceString));
                    }
                }
            });

            holder.notificationImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(context , ProfileActivity.class);
                    profileIntent.putExtra("userID" , userID);
                    context.startActivity(profileIntent);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(context , ProfileActivity.class);
                    profileIntent.putExtra("userID" , userID);
                    context.startActivity(profileIntent);
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
                    mRootRef.child("notifications").child(MainActivity.mUserID).child(notiID).child("seen").setValue("y").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });

                }
            });
        }
        else if (type.equals("comment")  || type.equals("like")  || type.equals("share") || type.equals("comment_like")){



            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();
            final String postID = notiItem.getContent_id();



            FirebaseFirestore.getInstance().collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String userName = task.getResult().getString("name");
                        String userThumbImage = task.getResult().getString("thumb_image");
                        holder.setImage(userThumbImage , context , R.drawable.ic_blank_profile);

                        //setting time ago for comment
                        GetTimeAgo ob = new GetTimeAgo();
                        long time = Long.parseLong(online);
                        String time_ago = ob.getTimeAgo(time ,context);
                        holder.notificationTime.setText( time_ago);


                        //setting comment
                        String sourceString = "default notification";
                        if(type.equals("comment")){
                            sourceString =  "<b>" + userName + "<b>" + "  has Commented on your post";
                        } else if(type.equals("like")) {
                            sourceString =  "<b>" + userName + "<b>" + "  has Liked your post";
                        }else if( type.equals("share")){
                            sourceString =  "<b>" + userName + "<b>" + "  has Shared your post";
                        }else{
                            sourceString =  "<b>" + userName + "<b>" + "  has liked your comment !";
                        }
                        holder.notificationText.setText(Html.fromHtml(sourceString));
                    }
                }
            });

            holder.notificationImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(context , ProfileActivity.class);
                    profileIntent.putExtra("userID" , userID);
                    context.startActivity(profileIntent);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));


                    firebaseFirestore.collection("notifications/"+mUserID+"/notificatinos").document(notiID).update("seen" , "y").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                    Intent showPostIntent = new Intent(context , ShowPostActivity.class);
                    showPostIntent.putExtra("postID" , postID);
                    Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_notification_image) ,"profile_image");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 );
                    context.startActivity(showPostIntent , optionsCompat.toBundle());
                }
            });
        }else if(type.equals("invitation")){  ///add something
            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();
            final String meetingID = notiItem.getContent_id();


            mRootRef.child("users").child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String imageURL = dataSnapshot.child("thumb_image").getValue().toString();
                    final String userName = dataSnapshot.child("name").getValue().toString();
                    String sourceString = "default notification";
                    sourceString =  "<b>" + userName + "<b>" + "  has invited you to a group meeting";
                    holder.notificationText.setText(Html.fromHtml(sourceString));
                    holder.setImage(imageURL , context , R.drawable.ic_blank_profile);
                    ////SETTING TIME AGO OF THE NOTIFICATION -//
                    GetTimeAgo ob = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String time_ago = ob.getTimeAgo(time ,context);
                    holder.notificationTime.setText( time_ago);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

           /* holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
                    mRootRef.child("notifications").child(MainActivity.mUserID).child(notiID).child("seen").setValue("y").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent showPostIntent = new Intent(context , MeetingActivity.class);
                            showPostIntent.putExtra("meetingID" , meetingID);
                            context.startActivity(showPostIntent);
                        }
                    });
                }
            });*/
        }
    }


    @Override
    public int getItemCount() {
        return mNotificationList.size();
    }


    public class NotificationViewHolder  extends RecyclerView.ViewHolder{
        public TextView notificationText;
        public CircleImageView notificationImage;
        public TextView notificationTime;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationImage = itemView.findViewById(R.id.item_notification_image);
            notificationText = itemView.findViewById(R.id.item_notification_details);
            notificationTime = itemView.findViewById(R.id.item_notification_time_ago);
        }

        public void setImage(String imageURL , final Context context , int drawable_id ){
            imageLoader.displayImage(imageURL, notificationImage, userImageOptions);
        }
    }



}