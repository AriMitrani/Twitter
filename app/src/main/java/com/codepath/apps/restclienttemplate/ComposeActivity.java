package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcel;
import org.parceler.Parcels;

import okhttp3.Headers;

@Parcel
public class ComposeActivity extends AppCompatActivity {
    public final String tag = "Compose";
    EditText mlCompose;
    Button btnTweet;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        mlCompose = findViewById(R.id.mlCompose);
        btnTweet = findViewById(R.id.bPublish);

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //tell the thingy to publish the tweet
                String content = mlCompose.getText().toString();
                if(content.trim().isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Tweet cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(content.length() > 280){
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(ComposeActivity.this, content, Toast.LENGTH_SHORT).show();
                client.publishTweet(content, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.e(tag, "Published tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject); //making it into a tweet obj
                            Log.e(tag, "Published tweet says: " + tweet.body);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet)); //sending this to timeline
                            setResult(RESULT_OK, intent);
                            finish();
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