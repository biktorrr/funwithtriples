package com.example.funwithtriples;

import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String myID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button button = (Button) findViewById(R.id.updateButton);
		createID();
		String myKnowledge = register();
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView tripleView = (TextView) findViewById(R.id.triplesTextView);
				tripleView.setText(myID);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected String createID (){
		if (myID == null){
			myID = UUID.randomUUID().toString();
			return myID;
		}
		else return myID;
	}
	
	protected String register() {
        String responseString;
		try {
			responseString = new TalkToServerTask().execute(new String[]{myID,"some_other"}).get();
			return responseString;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

class TalkToServerTask extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... params) {
    	HttpClient httpclient = new DefaultHttpClient();
		try {
			String url = "http://10.60.249.202:6000/pair?me="+params[0]+"&with="+params[1];
		    HttpResponse response = httpclient.execute(new HttpGet(url));
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        response.getEntity().writeTo(out);
	        out.close();
	        return out.toString();
		}
		catch (Exception e) {
            this.exception = e;
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(String response) {
        // TODO: check this.exception 
        // TODO: do something with the feed
    }

}