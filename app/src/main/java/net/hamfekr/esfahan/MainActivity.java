package net.hamfekr.esfahan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.hamfekr.esfahan.model.EventInfo;
import net.hamfekr.esfahan.parser.EventInfoJsonParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity {

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

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        mainProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        mainView = (LinearLayout) findViewById(R.id.mainView);
        failedView = (LinearLayout) findViewById(R.id.failedView);

        LoadData();
    }

    private void LoadData() {
        if (isOnline()) {
            requestData("http://samsambabadi.ir/HamfekrEsfahan/eventinfo.json");
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

    public void btnShowOnMapClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + (eventInfo.getLat() + ", " + eventInfo.getLng())));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnAddEventClick(View view) {
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
    }

    public void btnRegisterClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventInfo.getRegisterUrl()));
        startActivity(browserIntent);
    }

    public void btnReloadClick(View view) {
        LoadData();
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

            String content = HttpManager.getData(params[0],"HamfekrEsfahan","!EsfahanHamfekr1820");
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
