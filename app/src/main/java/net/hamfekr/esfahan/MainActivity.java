package net.hamfekr.esfahan;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onesignal.OneSignal;
import com.onesignal.OneSignal.NotificationOpenedHandler;

import net.hamfekr.esfahan.model.EventInfo;
import net.hamfekr.esfahan.parser.EventInfoJsonParser;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    TextView tvTitle;
    TextView tvDateTime;
    TextView tvAddress;
    TextView tvDetails;
    ProgressBar mainProgressBar;
    LinearLayout mainView;
    LinearLayout failedView;

    EventInfo eventInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OneSignal.init(this, "", "", new NotificationOpenedHandler() {
            @Override
            public void notificationOpened(String s, JSONObject jsonObject, boolean b) {
            }
        });

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        mainProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        mainView = (LinearLayout) findViewById(R.id.mainView);
        failedView = (LinearLayout) findViewById(R.id.failedView);

        LoadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_map:
                showOnMap();
                break;
            case R.id.action_calendar:
                addEvent();
                break;
            case R.id.action_register:
                registerEvent();
                break;
            case R.id.action_share:
                shareEvent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OneSignal.onPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OneSignal.onResumed();
    }

    private void LoadData() {
        if (isOnline()) {
            requestData("");
        } else {
            showFailedView(getString(R.string.internet_fail));
        }
    }

    private void showFailedView(String msg) {
        mainProgressBar.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        failedView.setVisibility(View.VISIBLE);

        TextView tvFail = (TextView) findViewById(R.id.tvFail);
        tvFail.setText(msg);
    }

    private void updateInfoDisplay() {
        tvTitle.setText(eventInfo.getTitle());
        tvAddress.setText(eventInfo.getAddress());
        tvDateTime.setText(eventInfo.getDate() + "\n" + eventInfo.getTime());
        tvDetails.setText(eventInfo.getDetails());
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void requestData(String uri) {
        GetEventInfoTask task = new GetEventInfoTask();
        task.execute(uri);
    }

    public void showOnMap() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + (eventInfo.getLat() + ", " + eventInfo.getLng())));
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
        }
    }

    public void addEvent() {
        try {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss").parse(eventInfo.getBeginTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", cal.getTimeInMillis());
            intent.putExtra("allDay", false);
            intent.putExtra("endTime", cal.getTimeInMillis() + eventInfo.getLongTime() * 60 * 1000);
            intent.putExtra("title", eventInfo.getTitle());
            intent.putExtra("description", eventInfo.getDate() + "\n" + eventInfo.getAddress());
            intent.putExtra("eventLocation", "geo:0,0?q=" + (eventInfo.getLat() + ", " + eventInfo.getLng()));

            startActivity(intent);
        } catch (Exception ex) {
        }
    }


    public void registerEvent() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventInfo.getRegisterUrl()));
            startActivity(browserIntent);
        } catch (Exception ex) {
        }
    }

    public void btnReloadClick(View view) {
        LoadData();
    }

    public void btnGit(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SamsamBabadi/HamfekrEsfahan"));
        startActivity(browserIntent);
    }

    public void shareEvent() {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, eventInfo.getShareText());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception ex) {
        }
    }

    private class GetEventInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            failedView.setVisibility(View.GONE);
            mainView.setVisibility(View.GONE);
            mainProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String content = HttpManager.getData(params[0], "", "");
            return content;
        }

        @Override
        protected void onPostExecute(String result) {

            eventInfo = EventInfoJsonParser.parseFeed(result);
            if (eventInfo == null) {
                showFailedView(getString(R.string.data_fail));
                return;
            }
            mainProgressBar.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
            updateInfoDisplay();
        }
    }
}
