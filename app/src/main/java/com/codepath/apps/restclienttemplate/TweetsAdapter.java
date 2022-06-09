package com.codepath.apps.restclienttemplate;
//this is populating data into the rows of the recyclerview

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcel;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Parcel
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.myViewHolder>{
    //what is this line doing? why do we need to extend it? it helps auto populate the methods

    Context context;
    List<Tweet> tweets;
    private TimelineActivity.ClickListener listener;
    public static final String tag = "Adapter";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    //public TimelineActivity tl = new TimelineActivity();
    String TAG = "timestamp";

    public TweetsAdapter(Context context, List<Tweet> tweets, TimelineActivity.ClickListener listener){
        this.context = context;
        this.tweets = tweets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //this inflates each item, using the item tweet xml file
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new myViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        //get the data and bind it to the viewholder
        Tweet tweet = tweets.get(position); //getting the tweet at the passed position
        holder.bind(tweet); //we need to define bind in order to populate it
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfilePic;
        TextView tvName;
        TextView tvTweet;
        TextView tvUser;
        ImageView ivMedia;
        TextView timeStamp;
        TimelineActivity tl = new TimelineActivity();
        private Button replyButton;
        private Button retweetButton;
        private CheckBox likeButton;
        private WeakReference<TimelineActivity.ClickListener> listenerRef;

        public myViewHolder(final View itemView, TimelineActivity.ClickListener listener) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUsername);
            tvTweet = itemView.findViewById(R.id.tvTweet);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            replyButton = itemView.findViewById(R.id.replyButton);
            retweetButton = itemView.findViewById(R.id.retweetButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            listenerRef = new WeakReference<>(listener);
            //replyButton = (Button) itemView.findViewById(R.id.swipeContainer);
            itemView.setOnClickListener(this);
            replyButton.setOnClickListener(this);
            retweetButton.setOnClickListener(this);
            likeButton.setOnClickListener(this);

        }

        public void bind(Tweet tweet) {
            tvName.setText(tweet.user.name);
            tvTweet.setText(tweet.body);
            tvUser.setText("@" + tweet.user.screenName);
            timeStamp.setText("• " +getRelativeTimeAgo(tweet.jsonDate));
            if(tweet.rtStatus == "true"){
                retweetButton.setText("a");
            }
            else{
                retweetButton.setText("x");
            }
            Glide.with(context).load(tweet.user.imageUrl).into(ivProfilePic);
            if(tweet.hasMedia){
                ivMedia.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.mediaUrl).into(ivMedia);
                Log.e(tag, "Image showing. URL: "+ tweet.mediaUrl);
            }
            else{
                ivMedia.setVisibility(View.GONE);
                Log.e(tag, "Image gone. URL: "+ tweet.mediaUrl);
            }


            //Log.e(tag, "URL: "+ tweet.mediaUrl);
        }

        @Override
        public void onClick(View v) {
            String button = "";
            if (v.getId() == replyButton.getId()) {
                //Toast.makeText(v.getContext(), "Reply", Toast.LENGTH_SHORT).show();
                button = "reply";
            }
            else if (v.getId() == retweetButton.getId()) {
                //Toast.makeText(v.getContext(), "Retweet", Toast.LENGTH_SHORT).show();
                button = "retweet";
            }
            else if (v.getId() == likeButton.getId()) {
                //Toast.makeText(v.getContext(), "Like", Toast.LENGTH_SHORT).show();
                button = "like";
            }
            else {
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
                button = "row";
            }

            listenerRef.get().onPositionClicked(getAdapterPosition(), button);
        }
    }



    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        //tweets.remove(tweets.size()-1);
        notifyDataSetChanged();
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();
            Date d = new Date(now);
            //long unixTimestamp = Instant.now().getEpochSecond();
            Log.i(TAG, "Post time: " + time);
            Log.i(TAG, "Post time: " + rawJsonDate);
            Log.i(TAG, "Current time: " + now/1000);
            Log.i(TAG, "Current time: " + d);

            final long diff = now - time;
            Log.i(TAG, "Diff: " + diff);
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}
