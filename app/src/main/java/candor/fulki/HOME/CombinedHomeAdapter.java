package candor.fulki.HOME;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.like.LikeButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CombinedHomeAdapter extends RecyclerView.Adapter<CombinedHomeAdapter.ViewHolder>{

    private static final String TAG = "CombinedHomeAdapter";

    List <CombinedPosts > data;
    Context context;
    Activity activity;

    public CombinedHomeAdapter ( List<CombinedPosts> data , Context context , Activity activity ){
        this.data = data;
        this.context = context;
        this.activity = activity;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_text, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        }
        return new CombinedHomeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CombinedPosts post = data.get(position);

        HashMap<String,String> Hash_file_maps = new HashMap<>();
        Hash_file_maps = post.getPost_thumb_url();
        holder.setImages(Hash_file_maps);
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).getPost_image_url().size() == 0){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{


        public ImageView mPostMoreOptions;
        public TextView  postUserName;
        public TextView  postCaption;
        public TextView  postDateTime;
        public TextView  postLocaiton;
        public TextView  postLikeCount;
        public TextView  postCommentCount;
        public TextView  postShareCount;
        public ImageView postImage;
        public CircleImageView postUserImage;
        public LikeButton postLikeButton;
        public ImageButton postCommentButton;
        public ImageButton postShareButton;
        public LinearLayout postCommentLinear;
        ProgressBar  postProgres;
        SliderLayout postSlider;



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
            postSlider = view.findViewById(R.id.item_post_slider);
        }

        public void setImages(HashMap<String , String> Hash_file_maps){
            for(String name : Hash_file_maps.keySet()){

                Log.d(TAG, "setImages: found a name   "+ name);
                TextSliderView textSliderView = new TextSliderView(context);
                textSliderView
                        .description(Hash_file_maps.get(name))
                        .image(name)
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra",name);
                postSlider.addSlider(textSliderView);
            }
            postSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            postSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            postSlider.setCustomAnimation(new DescriptionAnimation());
            postSlider.setDuration(3000);
            postSlider.addOnPageChangeListener(this);
            postSlider.setCustomIndicator((PagerIndicator) itemView.findViewById(R.id.custom_indicator));
        }
        @Override
        public void onSliderClick(BaseSliderView slider) {

        }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {

        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
