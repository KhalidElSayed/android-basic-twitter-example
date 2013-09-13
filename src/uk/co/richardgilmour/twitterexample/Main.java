package uk.co.richardgilmour.twitterexample;

import java.io.IOException;

import com.twitterapime.rest.Credential;
import com.twitterapime.rest.Timeline;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDeviceListener;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener {

	Button get_data_btn;
	private String TAG = "TwitterExample";
	private ProgressDialog dialog;
	private String exampleTweetText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		get_data_btn = (Button) findViewById(R.id.get_twitter);
		get_data_btn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		int source = view.getId();
		if (source == R.id.get_twitter) {
			getTwitterData();
		}
	}

	private void getTwitterData() {
		if (deviceHasConnection()) {
			startTwitterTask();
		} else {
			showShortToast("Connection Error");
		}
	}

	private void startTwitterTask() {
		new AsyncDataTask().execute("AnyTypeOfInputCanBeUsed");
	}

	private void showShortToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void showLogMessage(String msg) {
		Log.d(TAG, msg);
	}

	private boolean deviceHasConnection() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = manager.getActiveNetworkInfo();
		return netinfo != null && netinfo.isConnected();
	}

	private void showDialog() {
		dialog = ProgressDialog.show(this, "Loading Tweets", "Please Wait");
	}

	private void closeDialog() {
		dialog.dismiss();
	}

	private class AsyncDataTask extends AsyncTask<String, Integer, String> {

		private static final int DEFAULT_NUMBER_OF_TWEETS = 20;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Main.this.showLogMessage("PRE-EXECUTE HAS STARTED");
			Main.this.showDialog();
		}

		@Override
		protected String doInBackground(String... params) {
			String result = params[0];
			Token token = new Token(SecretData.TOKEN_ACCESS,
					SecretData.TOKEN_SECRET);
			@SuppressWarnings("deprecation")
			Credential cred = new Credential(SecretData.USERNAME,
					SecretData.CONSUMER_KEY, SecretData.CONSUMER_SECRET, token);
			UserAccountManager manager = UserAccountManager.getInstance(cred);
			try {
				if (manager.verifyCredential()) {
					Timeline timeline = Timeline.getInstance(manager);
					Query query = QueryComposer.count(DEFAULT_NUMBER_OF_TWEETS);
					timeline.startGetUserTweets(query,
							new SearchDeviceListener() {
								int count = 0;

								@Override
								public void tweetFound(Tweet tweet) {
									if (++count == 1) {
										exampleTweetText = getTweetText(tweet);
										showLogMessage(getTweetText(tweet));
									}
									// showLogMessage(tweet.toString());
								}

								private String getTweetText(Tweet tweet) {
									return tweet.getString("TWEET_CONTENT");
								}

								@Override
								public void searchFailed(Throwable fail) {
									showLogMessage(fail.getMessage());
								}

								@Override
								public void searchCompleted() {
									showLogMessage("Tweet search completed");
									Main.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											showShortToast(exampleTweetText);
										}
									});
								}
							});
				} else {
					Log.d(TAG, "details weren't verified");
				}
			} catch (IOException e) {
				e.printStackTrace();
				showLogMessage("IO Error");
			} catch (LimitExceededException e) {
				e.printStackTrace();
				showLogMessage("API Limit Reached");
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Main.this.closeDialog();
			showLogMessage("AsyncTask has completed");
		}
	}
}