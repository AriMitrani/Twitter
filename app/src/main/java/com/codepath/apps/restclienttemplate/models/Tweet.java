package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {
    public String body;
    public String createdAt;
    public User user;
    public String mediaUrl;
    public Boolean hasMedia;
    public String jsonDate;
    public String since;
    public static final String tag = "Post";
    public String id;
    public String rtStatus;
    public String likeStatus;

    public Tweet(){} //req by parcel

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        JSONObject entities = jsonObject.getJSONObject("entities");
        Log.e(tag, "JSON Obj: "+ jsonObject);
        //Log.e(tag, "Entities: "+ entities);
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.jsonDate = jsonObject.getString("created_at");
        Log.e(tag, "Date: "+ tweet.jsonDate);
        tweet.id = jsonObject.getString("id");
        tweet.rtStatus = jsonObject.getString("retweeted");
        tweet.likeStatus = jsonObject.getString("favorited");
        if(entities.has("media")){ //if there's an image
            JSONArray media = entities.getJSONArray("media");
            JSONObject trueMedia = media.getJSONObject(0);
            //Log.e(tag, "Media: "+ media);
            tweet.hasMedia = true;
            tweet.mediaUrl = trueMedia.getString("media_url_https");
            Log.e(tag, "String: "+ tweet.mediaUrl);
        }
        else{
            Log.e(tag, "No media"); //
            tweet.hasMedia = false;
            tweet.mediaUrl = "a";
        }
        //Log.e(tag, "Media state: " + media);
        //tweet.mediaUrl = entities.getString("created_at");
        //Log.e(tag, "Created at: "+ tweet.mediaUrl);
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
