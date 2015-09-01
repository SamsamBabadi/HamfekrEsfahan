package net.hamfekr.esfahan.parser;

import net.hamfekr.esfahan.model.EventInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Samsam on 4/11/2015.
 */
public class EventInfoJsonParser {
    public static EventInfo parseFeed(String content) {

        try {

            JSONObject obj = new JSONObject(content);
            EventInfo eventInfo = new EventInfo();

            eventInfo.setAddress(obj.getString("Address"));
            eventInfo.setDate(obj.getString("Date"));
            eventInfo.setLat(obj.getDouble("Latitude"));
            eventInfo.setLng(obj.getDouble("Longitude"));
            eventInfo.setBeginTime(obj.getString("BeginTime"));
            eventInfo.setLongTime(obj.getInt("LongTime"));
            eventInfo.setTime(obj.getString("Time"));
            eventInfo.setTitle(obj.getString("Title"));
            eventInfo.setDetails(obj.getString("Details"));
            eventInfo.setShareText(obj.getString("ShareText"));
            eventInfo.setRegisterUrl(obj.getString("RegisterURL"));

            return eventInfo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
