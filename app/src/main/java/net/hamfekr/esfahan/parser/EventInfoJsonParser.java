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

            JSONArray ar = new JSONArray(content);
            EventInfo eventInfo = new EventInfo();

            JSONObject obj = ar.getJSONObject(0);
            eventInfo.setAddress(obj.getString("address"));
            eventInfo.setDate(obj.getString("date"));
            eventInfo.setLat(obj.getDouble("lat"));
            eventInfo.setLng(obj.getDouble("lng"));
            eventInfo.setBeginTime(obj.getString("beginTime"));
            eventInfo.setLongTime(obj.getInt("longTime"));
            eventInfo.setTime(obj.getString("time"));
            eventInfo.setTitle(obj.getString("title"));
            eventInfo.setDetails(obj.getString("details"));
            eventInfo.setRegisterUrl(obj.getString("registerUrl"));

            return eventInfo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
