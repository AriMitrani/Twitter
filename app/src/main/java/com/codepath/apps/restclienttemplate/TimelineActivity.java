package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

@Parcel
public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    //public AdapterView.OnItemClickListener replyListener;

    public static String getMaxID() {
        return maxID;
    }

    public static String maxID;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);
        //find recyclerview
        rvTweets = findViewById(R.id.rvObj);

        //init list of tweets
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets, new ClickListener(){
            @Override public void onPositionClicked(int position, String button) {
                //Log.e(TAG, "Position: " + position + ", Button: " +button);
                ///Toast.makeText(this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
                if(button == "reply"){
                    Log.e(TAG, "Replying to: " + tweets.get(position).user.screenName);
                    showReplyDialog("@" + tweets.get(position).user.screenName + " ");
                }
                else if(button == "retweet"){
                    Log.e(TAG, "Rtweeting: " + tweets.get(position).id);
                    if (tweets.get(position).rtStatus == "false"){
                        client.rtTweet(tweets.get(position).id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.e(TAG, "Rtweeted");
                                tweets.get(position).rtStatus = "true";
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "Failed to RT");
                            }
                        });
                    } else {
                        client.UndoRtTweet(tweets.get(position).id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.e(TAG, "UNRtweeted");
                                tweets.get(position).rtStatus = "false";
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "Failed to UNRT");
                            }
                        });
                    }
                }
                else if(button == "like"){
                    Log.e(TAG, "Liking post by : " + tweets.get(position).user.screenName);
                    if (tweets.get(position).likeStatus == "false"){
                        client.likeTweet(tweets.get(position).id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.e(TAG, "Liked");
                                tweets.get(position).likeStatus = "true";
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "Failed to Like");
                            }
                        });
                    } else {
                        client.unlikeTweet(tweets.get(position).id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.e(TAG, "UNLiked");
                                tweets.get(position).likeStatus = "false";
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "Failed to UNLike");
                            }
                        });
                    }
                }
                else if(button == "row"){
                    Log.e(TAG, "Opening expanded view: " + tweets.get(position).user.screenName);
                }
            }

        });
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //set up recyclerview using adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                populateMore();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);


        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateRefresh();
            }
        });
        populateHomeTimeline();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*if(item.getItemId() == R.id.composeButton){
            Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
            return true;
        }*/
        if(item.getItemId() == R.id.menuLogout){
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            Log.i("button", "Button clicked");
            TwitterApp.getRestClient(this).clearAccessToken();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
            startActivity(i);
            return true;
        }
        if(item.getItemId() == R.id.testMenu){
            //Toast.makeText(this, "Composing...", Toast.LENGTH_SHORT).show();
            //showEditDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray; //making the json we get back into an array
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    maxID = tweets.get(tweets.size()-1).id;
                    tweets.remove(tweets.size()-1);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.i(TAG, "JSON Exception!", e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    private void populateRefresh() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray; //making the json we get back into an array
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    maxID = tweets.get(tweets.size()-1).id;
                    tweets.remove(tweets.size()-1);
                    adapter.notifyDataSetChanged();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.i(TAG, "JSON Exception!", e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    private void populateMore() {
        client.getMoreTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray; //making the json we get back into an array
                try {
                    //adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    maxID = tweets.get(tweets.size()-1).id;
                    tweets.remove(tweets.size()-1);
                    adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.i(TAG, "JSON Exception!", e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    public void composeButton(View view){
        //Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
        //Intent intent = new Intent(this, ComposeActivity.class);
        //startActivityForResult(intent, REQUEST_CODE);
        //Toast.makeText(this, "Composing...", Toast.LENGTH_SHORT).show();
        showEditDialog();
    }

   public void replyToTweet(){
        showReplyDialog("Testname");
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            //get data from the old intent, compose. Tweet is the data
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //update recyclerview
            tweets.add(0, tweet); //adding this new tweet to the tweets array at the top
            adapter.notifyItemInserted(0); //puts it into the actual view of pos
            maxID = tweets.get(tweets.size()-1).id;
            rvTweets.smoothScrollToPosition(0); //scrolls to top
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    public void pushTweet(@Nullable Parcelable data){
        //get data from the old intent, compose. Tweet is the data
        Tweet tweet = Parcels.unwrap(data);
        //update recyclerview
        tweets.add(0, tweet); //adding this new tweet to the tweets array at the top
        adapter.notifyItemInserted(0); //puts it into the actual view of pos
        maxID = tweets.get(tweets.size()-1).id;
        rvTweets.smoothScrollToPosition(0); //scrolls to top
    }

    public void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        MessageFragment messageFragment = MessageFragment.newInstance("Tweeting");
        Log.i(TAG, "Not tagging");
        messageFragment.show(fm, "fragment_edit_name");
    }

    public void showReplyDialog(String userName) {
        FragmentManager fm = getSupportFragmentManager();
        MessageFragment messageFragment = MessageFragment.newInstance("Tweeting", userName);
        Log.i(TAG, "Tagging " + userName);
        messageFragment.show(fm, "fragment_edit_name");
    }


    public interface ClickListener {
        void onPositionClicked(int position, String button);

    }

}