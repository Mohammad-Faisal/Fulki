package candor.fulki.EXPLORE.EVENTS;

import android.app.Activity;
import android.content.Context;
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
        String thumb_image_url = events.getThumb_image_url();
        String eventID = events.getEvent_push_id();

        holder.eventDate.setText(date);
        holder.eventLocation.setText(location);
        holder.eventTitle.setText(title);
        holder.eventPeopleCnt.setText(cnt);
        holder.setImage(thumb_image_url , context);


        holder.eventCard.setOnClickListener(v -> {
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        });



    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {

        CardView eventCard;
        ImageView eventImage;
        TextView eventTitle , eventDate , eventLocation, eventPeopleCnt;



        public EventsViewHolder(View itemView) {
            super(itemView);

            eventCard = itemView.findViewById(R.id.item_event_card);
            eventImage = itemView.findViewById(R.id.item_event_image);
            eventDate = itemView.findViewById(R.id.item_event_date);
            eventLocation = itemView.findViewById(R.id.item_event_locaiton);
            eventPeopleCnt = itemView.findViewById(R.id.item_event_people_cnt);
            eventTitle = itemView.findViewById(R.id.item_event_title);
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
