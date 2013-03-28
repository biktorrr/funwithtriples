package com.example.funwithtriples;


import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.webkit.WebView;


public class MainActivity extends Activity {
	
	private String myID;

	WebView myWebView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		createID();
		
		myWebView = (WebView) findViewById(R.id.myWebView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void onResume(){
		super.onResume();
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.addJavascriptInterface(new JavaScriptInterface(this),
				"Android");
//	myWebView.loadUrl("http://view.jquerymobile.com/demos/");
//	myWebView.loadUrl("http://93.191.131.147/eastapp/hybrid.php");
		

		myWebView.loadUrl("http://eculture.cs.vu.nl:8088/index.html");
		myWebView.loadUrl("file:///android_asset/axiom_list.html");

		
	}

	public class JavaScriptInterface {
			Context mContext;

			/** Instantiate the interface and set the context */
			JavaScriptInterface(Context c) {
				mContext = c;
			}
			
			public void triggerMe(String msg){
				Log.i("SemHybrid","trigger me!"+msg  );
			}

	}

	protected String createID (){
		if (myID == null){
			myID = UUID.randomUUID().toString().substring(0,4);
			return myID;
		}
		else return myID;
	}
	
	protected String getID() {
		return myID;
	}

	protected String register() {
        String responseString;
		try {
			responseString = new TalkToServerTask().execute(new String[]{"welcome" , myID,"some_other"}).get();
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
	
	protected String pair(String friendID) {
        String responseString;
		try {
			responseString = new TalkToServerTask().execute(new String[]{"welcome" , myID, friendID}).get();
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
			String url = "http://eculture.cs.vu.nl:8088/"+params[0]+"?me="+params[1];
			if (params[0] == "pair")
				url+="&with="+params[2];

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
