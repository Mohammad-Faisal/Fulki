package candor.fulki.EXPLORE.EVENTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import candor.fulki.CHAT.Meeting.MeetingRooms;
import candor.fulki.CHAT.Meeting.MeetingRoomsAdapter;
import candor.fulki.R;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {


    private List<Events> eventsList;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Context context;
    Activity activity;



    public EventsAdapter(List<Events> eventsList, Context context , Activity activity) {
        this.eventsList = eventsList;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent , false);
        return new EventsAdapter.EventsViewHolder(v);
        }

    @Override
    public void onBindViewHolder(EventsViewHolder holder, int position) {
        Events events = eventsList.get(position);

        String title = events.getTitle();
        String cnt = events.getPeople_cnt();
        String location = events.getLocation();
        String date = events.getTime_and_date();
        String thumb_image_url = events.getImage_url();
        String eventID = events.getEvent_push_id();

        holder.eventDate.setText(date);
        holder.eventLocation.setText(location);
        holder.eventTitle.setText(title);
        holder.setPeopleCnt(eventID);
        holder.setCommentCnt(eventID);
        holder.setImage(thumb_image_url , context);

        holder.eventCard.setOnClickListener(v -> {
            Intent showEventIntent = new Intent( context ,ShowEventActivity.class);
            showEventIntent.putExtra("event_id"  , eventID);
            context.startActivity(showEventIntent);
        });

    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {

        CardView eventCard;
        ImageView eventImage;
        TextView eventTitle , eventDate , eventLocation, eventPeopleCnt , eventCommentCnt;



        public EventsViewHolder(View itemView) {
            super(itemView);

            eventCard = itemView.findViewById(R.id.item_event_card);
            eventImage = itemView.findViewById(R.id.item_event_image);
            eventDate = itemView.findViewById(R.id.item_event_date);
            eventLocation = itemView.findViewById(R.id.item_event_locaiton);
            eventPeopleCnt = itemView.findViewById(R.id.item_event_people_cnt);
            eventTitle = itemView.findViewById(R.id.item_event_title);
            eventCommentCnt = itemView.findViewById(R.id.item_event_comment_cnt);
        }


        public void  setPeopleCnt(String eventID){

            FirebaseFirestore.getInstance().collection("joins/" + eventID + "/joins").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        eventPeopleCnt.setText(""+count+" people");
                    } else {
                        eventPeopleCnt.setText(""+"0"+" people");
                    }
                }
            });

        }


        public void  setCommentCnt(String eventID){

            FirebaseFirestore.getInstance().collection("comments/" + eventID + "/comments").addSnapshotListener((documentSnapshots, e) -> {
                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    eventCommentCnt.setText(""+count+" comments");
                } else {
                    eventCommentCnt.setText(""+"0"+" comment");
                }
            });

        }


        public void setImage(final String imageURL , final Context context ){
            if(imageURL.equals("default")){

            }
            else{
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_camera_icon).into(eventImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(eventImage);
                    }
                });
            }
        }


    }
}
