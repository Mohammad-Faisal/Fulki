package candor.fulki.HOME;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.EXPLORE.PEOPLE.Ratings;
import candor.fulki.GENERAL.Functions;
import candor.fulki.GENERAL.MainActivity;
import candor.fulki.NOTIFICATION.Notifications;
import candor.fulki.PROFILE.ProfileActivity;
import candor.fulki.PROFILE.ShowPleopleListActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.net.NetworkInfo.State.UNKNOWN;
import static com.nostra13.universalimageloader.core.assist.FailReason.FailType.IO_ERROR;
import static com.nostra13.universalimageloader.core.assist.FailReason.FailType.NETWORK_DENIED;
import static com.nostra13.universalimageloader.core.assist.FailReason.FailType.OUT_OF_MEMORY;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {


    private final static String TAG = "HomeAdapter";

    private List<Posts> data = new ArrayList<>();
    private Context context;


    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private RecyclerView recyclerView;
    private Activity activity;
    public boolean isLiked = false;

    private DatabaseReference mRootRef;
    private String mUserID, mUserImage, mUserName;
    private String mCurrentPosterId;

    public ImageLoader imageLoader;
    public DisplayImageOptions postImageOptions;
    public DisplayImageOptions userImageOptions;


    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    //initialize the inflator by yourself
    private LayoutInflater inflater;
    private ProgressDialog mProgress;

    long currentLikesCount , currentCommentsCount, currentSharesCount;



    public HomeAdapter(RecyclerView recyclerView, List<Posts> data, Activity activity, final Context context) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
        this.data = data;
        this.recyclerView = recyclerView;
        this.mRootRef = FirebaseDatabase.getInstance().getReference();
        this.mRootRef.keepSynced(true);
        this.mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_text, parent, false);
        }else{
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).getPost_image_url().equals("default")){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        //holder.setIsRecyclable(false);

        final Posts post = data.get(position);

        final String mCurrentPosterId = post.getUser_id();  //whose post is this
        final String postPushID = post.getPost_push_id();
        final String userThumbImage = post.getUser_thumb_image();
        final String postImage = post.getPost_image_url();
        final String userName = post.getUser_name();
        final String caption = post.getCaption();
        currentLikesCount  = post.getLike_cnt();
        currentCommentsCount = post.getComment_cnt();
        currentSharesCount = post.getShare_cnt();
        final String timeDate = post.getTime_and_date();


        Log.d(TAG, "onBindViewHolder:      image url found for this  is  "+post.getPost_image_url());
        Log.d(TAG, "onBindViewHolder:      thumb image url found for this  is  "+post.getPost_thumb_image_url());


        holder.postProgres.setVisibility(View.GONE);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(post.getPost_image_url(), holder.postImage);

        /*if(post.getPost_image_url().equals("default")){
            holder.postImage.setVisibility(View.GONE);
            holder.postProgres.setVisibility(View.GONE);
        }else{


        }*/


        holder.setPostCaption(caption);
        holder.setPostDateTime(timeDate);
        holder.setUserImage(userThumbImage , holder.postUserImage);
        //holder.setUserImage(postImage , holder.postImage);
        holder.setPostUserName(userName);
        holder.setPostLikeCount(currentLikesCount);
        holder.setPostUserName(userName);
        holder.setPostCommentCount(currentCommentsCount);




        //holder.setImagePicasso(post.getImage_url() , context ,  holder.postImage );
        //holder.setUserImage(post.getPost_image_url(), holder.postImage);


        //setting if it is shared or not
        /*String location = post.getLocation();
        if(location.equals("")){
            holder.setPostLocaiton("");
            holder.postLocaiton.setVisibility(View.GONE);
        }else{
            holder.postLocaiton.setVisibility(View.VISIBLE);
            holder.setPostLocaiton("shared by "+location);
        }*/




        //setting user name and user image onclick listener
        holder.postUserImage.setOnClickListener(v -> {

            holder.addRating(mUserID , 1);
            holder.addRating(post.getUser_id() , 1);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mCurrentPosterId);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());

        });

        //setting user name and user image onclick listener
        holder.postUserName.setOnClickListener(v -> {
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mCurrentPosterId);

            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");

            holder.addRating(mUserID , 1);
            holder.addRating(post.getUser_id() , 1);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });


        //setting comment count
        FirebaseFirestore.getInstance().collection("comments/" + postPushID + "/comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    long count = documentSnapshots.size();
                    //long cnt = Integer.toString(count);

                    firebaseFirestore.collection("posts").document(postPushID).update("comment_cnt" , count);
                    if (count == 1) {
                        holder.setPostCommentCount(count);
                    } else {
                        holder.setPostCommentCount(count);
                    }
                } else {
                    holder.setPostCommentCount(0);
                }
            }
        });

        //setting share count
        /*FirebaseFirestore.getInstance().collection("shares/" + postPushID + "/shares").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                int count = documentSnapshots.size();
                firebaseFirestore.collection("posts").document(postPushID).update("share_cnt" , count);
                String cnt = Integer.toString(count);
                if (count == 1) {
                    holder.postShareCount.setText(cnt + " share");
                } else {
                    holder.postShareCount.setText(cnt + " shares");
                }
            } else {
                holder.postShareCount.setText("0 share");
            }
        });*/


        //setting the current state of like button
        FirebaseFirestore.getInstance().collection("likes/" + postPushID + "/likes").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        holder.postLikeButton.setLiked(true);
                    } else {
                        holder.postLikeButton.setLiked(false);
                    }
                } else {
                    Log.w(TAG, "onComplete: ", task.getException());
                    holder.postLikeButton.setLiked(false);
                }
            }
        });


        //handling the like onclick listener
        holder.postLikeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                //building like
                holder.postLikeButton.setLiked(true);
                Functions f = new Functions();

                //building like
                String time_stamp = String.valueOf(new Date().getTime());
                WriteBatch writeBatch  = firebaseFirestore.batch();
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mCurrentPosterId+"/notificatinos").document();
                String likeNotificatoinPushID = ref.getId();

                Likes mLikes = new Likes(mUserID , MainActivity.mUserName , MainActivity.mUserThumbImage ,likeNotificatoinPushID , time_stamp);
                Notifications pushNoti = new Notifications( "like" ,mUserID , post.getUser_id() , postPushID ,likeNotificatoinPushID , time_stamp,"n"  );


                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+post.getUser_id()+"/notificatinos").document(likeNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+postPushID+"/notifications").document(likeNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + postPushID + "/likes").document(mUserID); //.set(mLikes);
                writeBatch.set(postLikeRef, mLikes);

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "liked:   like is successful");
                    holder.addRating(mUserID , 3);
                    holder.addRating(post.getUser_id() , 1);
                    holder.addLike(postPushID,1);
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "liked:   like is not succesful");
                });

                //as the user liked this post so i should give him some points
            }
            @Override
            public void unLiked(LikeButton likeButton) {
                holder.postLikeButton.setLiked(false);
                firebaseFirestore.collection("likes/" + postPushID + "/likes").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){

                                String likeNotificatoinPushID = task.getResult().getString("notificationID");
                                WriteBatch writeBatch  = firebaseFirestore.batch();

                                if(likeNotificatoinPushID != null){
                                    DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+post.getUser_id()+"/notificatinos").document(likeNotificatoinPushID);
                                    writeBatch.delete(notificatinoRef);
                                    DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+postPushID+"/notifications").document(likeNotificatoinPushID);
                                    writeBatch.delete(postNotificatinoRef);
                                }
                                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + postPushID + "/likes").document(mUserID); //.set(mLikes);
                                writeBatch.delete(postLikeRef);

                                writeBatch.commit().addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "liked:   unlike is successful");

                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "liked:   unlike is not succesful");
                                });

                            }
                        }
                    }
                });
                holder.addRating(mUserID , -3);
                holder.addRating(post.getUser_id() , -1);
                holder.addLike(postPushID,-1);
            }
        });

        holder.postLikeCount.setOnClickListener(v -> {
            Intent showPeopleIntent = new Intent(context , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "likes");
            showPeopleIntent.putExtra("user_id" ,postPushID );
            holder.addRating(mUserID , 2);
            context.startActivity(showPeopleIntent);
        });

        //handling the comment onclick listener
        holder.postCommentLinear.setOnClickListener(v -> {

           // Dialog commentDialog = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
            Dialog commentDialog = new Dialog(context, android.R.style.ThemeOverlay_Material_ActionBar);
            commentDialog.getWindow().getAttributes().windowAnimations = R.anim.fui_slide_in_right;
            commentDialog.setContentView(R.layout.comment_pop_up_dialog);

            //containers
            final RecyclerView mCommentList;
            final List<Comments> commentList = new ArrayList<>();
            LinearLayoutManager mLinearLayout;


            //--------- SETTING THE COMMENT ADAPTERS --//
            final PostCommentAdapter mPostCommentAdapter = new PostCommentAdapter(commentList, context , activity);
            mCommentList = commentDialog.findViewById(R.id.comment_list);
            mLinearLayout = new LinearLayoutManager(context);
            mCommentList.hasFixedSize();
            mCommentList.setLayoutManager(mLinearLayout);
            mCommentList.setAdapter(mPostCommentAdapter);


            //-------------LOADING COMMENTS------------//
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("comments/"+ postPushID+"/comments").orderBy("time_stamp" , Query.Direction.DESCENDING);
            nextQuery.addSnapshotListener((documentSnapshots, e) -> {
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Comments singleComment = doc.getDocument().toObject(Comments.class);
                        commentList.add(singleComment);
                        mPostCommentAdapter.notifyDataSetChanged();
                    }
                }
            });




            //comment onclick functionality
            final TextView commentBox = commentDialog.findViewById(R.id.comment_write);
            ImageButton commentPost = commentDialog.findViewById(R.id.comment_post);
            commentPost.setOnClickListener(view -> {

                String time_stamp = String.valueOf(new Date().getTime());


                DocumentReference notiRef = FirebaseFirestore.getInstance().collection("notifications/"+mCurrentPosterId+"/notificatinos").document();
                String commentNotificatoinPushID = notiRef.getId();

                DocumentReference commentRef = FirebaseFirestore.getInstance().collection("comments/"+ postPushID +"/comments").document();
                String commentID = commentRef.getId();

                Notifications pushNoti = new Notifications( "comment" ,mUserID , post.getUser_id(), postPushID,commentNotificatoinPushID , time_stamp,"n"  );
                Comments  comment =  new Comments(commentBox.getText().toString() , mUserID ,commentID, postPushID, commentNotificatoinPushID  , time_stamp);
                commentBox.setText("");


                WriteBatch writeBatch  = firebaseFirestore.batch();

                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+post.getUser_id()+"/notificatinos").document(commentNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+postPushID+"/notifications").document(commentNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                writeBatch.set(commentRef, comment);

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "liked:   like is successful");
                    holder.addRating(mUserID , 5);
                    holder.addRating(post.getUser_id() , 2);

                }).addOnFailureListener(e -> {
                    Log.d(TAG, "liked:   like is not succesful");
                });
            });
            commentDialog.show();
        });

        //more options functionality
        holder.mPostMoreOptions.setOnClickListener(v -> {

            if(mUserID.equals(mCurrentPosterId)){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Do you want to Delete this post?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                                mProgress = new ProgressDialog(context);
                                mProgress.setTitle("Deleting Post.......");
                                mProgress.setMessage("please wait while we delete your post");
                                mProgress.setCanceledOnTouchOutside(false);
                                mProgress.show();

                                // post theke delete korte hobe
                                //notification gula delete korte hobe
                                //share gula delete korte hobe
                                //comment gula delete korte hobe
                                //comment like gula delete korte hobe
                                //like gula delete korte hobe


                                firebaseFirestore.collection("posts").document(postPushID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mProgress.dismiss();
                                            notifyDataSetChanged();
                                            Log.d(TAG, "onComplete: post is deleted !!");
                                        }else{
                                            Log.d(TAG, "onComplete: likes are not deleted !!!!");
                                        }
                                    }
                                });


                                /*firebaseFirestore.collection("comments").document(postPushID).delete();
                                firebaseFirestore.collection("comment_likes").document(postPushID).delete();
                                firebaseFirestore.collection("shares").document(postPushID).delete();*/



                                /*firebaseFirestore.collection("images").document(mUserID).collection("posts").document(postPushID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                     if(task.isSuccessful()){
                                         if(task.getResult().exists()){
                                             final String imagePath = task.getResult().get("imagePath").toString();
                                             final String thumbImagePath = task.getResult().get("imageThumbPath").toString();
                                             // Create a storage reference from our app

                                             final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                             StorageReference imageRef = storageRef.child(imagePath);
                                             imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void aVoid) {
                                                     StorageReference imageRef = storageRef.child(thumbImagePath);
                                                     imageRef.delete();
                                                     firebaseFirestore.collection("images").document(mUserID).collection("posts").document(postPushID).delete();
                                                 }
                                             }).addOnFailureListener(new OnFailureListener() {
                                                 @Override
                                                 public void onFailure(@NonNull Exception exception) {
                                                     // Uh-oh, an error occurred!
                                                 }
                                             });
                                         }
                                     }else{
                                         Log.d(TAG, "onComplete: some error occured");
                                     }
                                    }
                                });*/



                                /*Query nextQuery = firebaseFirestore.collection("posts/"+postPushID+"/notificatinos");
                                nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                                            if(doc.getType() == DocumentChange.Type.REMOVED){
                                                Notifications singleNotifications = doc.getDocument().toObject(Notifications.class);
                                                String notiID = singleNotifications.getNotification_id();
                                                String userID = singleNotifications.getNotification_to();
                                                firebaseFirestore.collection("notifications/"+userID+"/notificatinos").document(notiID).delete();
                                            }else{
                                                Log.d(TAG, "onEvent: notification type is not added");
                                            }
                                        }
                                    }
                                });*/


                                /*firebaseFirestore.collection("posts").document(postPushID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mProgress.dismiss();
                                        }
                                    }
                                });*/



                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Do you want to Report this post?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        //post share functionality
        /*holder.postShareButton.setOnClickListener(v -> {
            //setting user details
            FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String mCurrentUserName = task.getResult().getString("name");
                        Map<String , Object> postMap = new HashMap<>();
                        postMap.put("user_id" , mUserID);
                        postMap.put("image_url" , post.getImage_url());
                        postMap.put("thumb_image_url" , post.getThumb_image_url());
                        postMap.put("caption" , post.getCaption());
                        postMap.put("time_and_date" , post.getTime_and_date());
                        postMap.put("timestamp" ,post.getTimestamp() );
                        postMap.put("post_push_id" , post.getPost_push_id());
                        postMap.put("location" , mCurrentUserName);


                        String time_stamp = String.valueOf(new Date().getTime());
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mCurrentPosterId+"/notificatinos").document();
                        String shareNotificatoinPushID = ref.getId();

                        Notifications pushNoti = new Notifications( "share" ,mUserID , post.getUser_id(), postPushID ,shareNotificatoinPushID , time_stamp,"n"  );
                        Shares  share =  new Shares(mUserID , shareNotificatoinPushID);

                        firebaseFirestore.collection("notifications/"+post.getUser_id()+"/notificatinos").document(shareNotificatoinPushID).set(pushNoti);
                        firebaseFirestore.collection("shares/"+postPushID+"/shares").add(share);
                        firebaseFirestore.collection("posts/"+postPushID+"/notifications").document(shareNotificatoinPushID).set(pushNoti);


                        //we are posting the shared post as a new post but for like comment count we didnt change the postPushId of the post
                        firebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                Toast.makeText(context, "Success !", Toast.LENGTH_SHORT).show();
                                holder.addRating(mUserID , 5);
                                holder.addRating(post.getUser_id() , 5);
                            }else{
                                Toast.makeText(context, "Success !", Toast.LENGTH_SHORT).show();
                            }
                        });



                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().toString());
                    }
                }
            });
        });*/

        holder.postImage.setOnClickListener(v -> {
            Intent showPostIntent = new Intent(context  , ShowPostActivity.class);
            showPostIntent.putExtra("postID" , postPushID);

            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_image) ,"post_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair3 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");


            holder.addRating(post.getUser_id() , 1);

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 , pair3);
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ImageView mPostMoreOptions;
        public TextView postUserName;
        public TextView postCaption;
        public TextView postDateTime;
        public TextView postLocaiton;
        public TextView postLikeCount;
        public TextView postCommentCount;
        public TextView postShareCount;
        public ImageView postImage;
        public CircleImageView postUserImage;
        public LikeButton postLikeButton;
        public ImageButton postCommentButton;
        public ImageButton postShareButton;
        private LinearLayout postCommentLinear;
        ProgressBar postProgres;

        public ViewHolder(View view) {
            super(view);
            postUserName = view.findViewById(R.id.post_user_name);
            postCaption = view.findViewById(R.id.post_caption);
            postDateTime = view.findViewById(R.id.post_time_date);
            postLocaiton = view.findViewById(R.id.post_location);
            postImage = view.findViewById(R.id.post_image);
            postUserImage = view.findViewById(R.id.post_user_single_imagee);
            postLikeButton = view.findViewById(R.id.post_like_button);
            postCommentButton = view.findViewById(R.id.post_comment_button);
            postLikeCount = view.findViewById(R.id.post_like_number);
            postCommentCount = view.findViewById(R.id.post_comment_number);
            mPostMoreOptions = view.findViewById(R.id.post_more_options);
            postShareButton = view.findViewById(R.id.post_share_button);
            postShareCount = view.findViewById(R.id.post_share_cnt);
            postCommentLinear = view.findViewById(R.id.item_post_comment_linear);
            postProgres = view.findViewById(R.id.item_post_progress);
        }


        private Task<Void> addRating( String mUserID  , int factor) {

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

        /*private Task<Void> addLike( String mPostID , int factor) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "addLike:   function calledd !!!!");
            final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts")
                    .document(mPostID);

            return firebaseFirestore.runTransaction(transaction -> {
                Posts post = transaction.get(postRef)
                        .toObject(Posts.class);
                long curLikes = post.getLike_cnt();
                long curComments = post.getComment_cnt();
                long curShares = post.getShare_cnt();
                long nextLike = curLikes + factor;
                currentLikesCount = nextLike;
                long nextComment = curComments + factor;
                long nextShare = curShares + factor;
                HashMap< String ,  Object > updateMap = new HashMap<>();
                updateMap.put("like_cnt" , nextLike);
                Log.d(TAG, "after the incrementValue: current likes to be set "+nextLike);
                transaction.update(postRef, updateMap);
                return null;
            });
        }*/

        //increase the like count by one
        private void addLike( String mPostID , int factor) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "addLike:   function calledd !!!!");
            final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts")
                    .document(mPostID);

             firebaseFirestore.runTransaction(transaction -> {
                Posts post = transaction.get(postRef)
                        .toObject(Posts.class);
                long curLikes = post.getLike_cnt();
                long curComments = post.getComment_cnt();
                long curShares = post.getShare_cnt();

                long nextLike = curLikes + factor;
                 Log.d(TAG, "addLike:     like number is  "+nextLike);
                long nextComment = curComments + factor;
                long nextShare = curShares + factor;
                HashMap< String ,  Object > updateMap = new HashMap<>();
                updateMap.put("like_cnt" , nextLike);
                transaction.update(postRef , updateMap);
                return nextLike;
            }).addOnSuccessListener(new OnSuccessListener<Long>() {
                 @Override
                 public void onSuccess(Long aLong) {
                     setPostLikeCount(aLong);
                 }
             });
        }


        private void addComment( String mPostID , int factor) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "addLike:   function calledd !!!!");
            final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts")
                    .document(mPostID);

            firebaseFirestore.runTransaction(transaction -> {
                Posts post = transaction.get(postRef)
                        .toObject(Posts.class);
                long curComments = post.getComment_cnt();
                long nextComment = curComments + factor;

                Log.d(TAG, "addLike:     comment cnt now  is  "+nextComment);
                HashMap< String ,  Object > updateMap = new HashMap<>();
                updateMap.put("comment_cnt" , nextComment);
                transaction.update(postRef , updateMap);
                return nextComment;
            }).addOnSuccessListener(new OnSuccessListener<Long>() {
                @Override
                public void onSuccess(Long aLong) {
                    setPostLikeCount(aLong);
                }
            });
        }



        public void setPostCaption(String Caption) {
            postCaption.setText(Caption);
        }

        public void setPostUserName(String userName) {
            mUserName = userName;
            postUserName.setText(userName);
        }

        public void setPostDateTime(String dateTime) {
            postDateTime.setText(dateTime);
        }

        public void setPostLikeCount(long currentLikesCount) {
            if(currentLikesCount>1){
                postLikeCount.setText(""+currentLikesCount+" likes");
            }else{
                postLikeCount.setText(""+currentLikesCount+" like");
            }
        }

        public void setPostCommentCount(long currentCommentsCount) {
            if(currentCommentsCount>1){
                postCommentCount.setText(""+currentCommentsCount+" comments");
            }else{
                postCommentCount.setText(""+currentCommentsCount+" comment");
            }
        }

        public void setPostLocaiton(String location) {
            postLocaiton.setText(location);
        }

        public void setUserImage(String image_url , ImageView imageView) {
            mUserImage = image_url;
            if(image_url!=null){
                if(image_url.equals("default")){
                    Log.d(TAG, "setUserImage:    visibility gone but caption is  " + image_url);
                    postImage.setVisibility(View.GONE);
                }else{
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(image_url, imageView);
                    //imageLoader.displayImage(image_url, imageView, userImageOptions);
                }
            }

        }

        public void setImage(final String image_url, int type) {
            if(image_url.equals("default")){
                postImage.setVisibility(View.GONE);
            }else{
                imageLoader.displayImage(image_url, postImage, postImageOptions);
            }
        }

        public void setImagePicasso(final String imageURL , final Context context , ImageView imageView ){
            Log.d(TAG, "setImagePicasso:    found image url  "+imageURL);
            if(imageURL.equals("default")){
                postImage.setVisibility(View.GONE);
            }
            else{
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_camera_icon).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(imageView);
                    }
                });
            }
        }


    }
}
