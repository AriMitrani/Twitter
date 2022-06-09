package com.codepath.apps.restclienttemplate;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcel;
import org.parceler.Parcels;

import okhttp3.Headers;

@Parcel
public class MessageFragment extends DialogFragment {
    private EditText mEditText;
    Button btnTweet;
    TwitterClient client = TwitterApp.getRestClient(getContext());
    String tag = "popup";
    Context context;
    TimelineActivity tl = new TimelineActivity();
    static String taggedUser = "";

    public MessageFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static MessageFragment newInstance(String title) {
        MessageFragment frag = new MessageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public static MessageFragment newInstance(String title, String userName) {
        MessageFragment frag = new MessageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        taggedUser = userName;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditText = (EditText) view.findViewById(R.id.mlCompose);
        mEditText.setText(taggedUser);
        btnTweet = (Button) view.findViewById(R.id.bPublish2);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //tell the thingy to publish the tweet
                Log.e(tag, "Click");
                String content = mEditText.getText().toString();
                if(content.trim().isEmpty()){
                    Toast.makeText(getContext(), "Tweet cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(content.length() > 280){
                    Toast.makeText(getContext(), "Sorry, your tweet is too long!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
                client.publishTweet(content, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.e(tag, "Published tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject); //making it into a tweet obj
                            Log.e(tag, "Published tweet says: " + tweet.body);
                            tl.pushTweet(Parcels.wrap(tweet));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(tag, "Failed to publish tweet", throwable);
                    }
                });
            }
        });
    }

}
