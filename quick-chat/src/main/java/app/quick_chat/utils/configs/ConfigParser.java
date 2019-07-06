package app.quick_chat.utils.configs;

import android.content.Context;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import app.quick_chat.CoreApp;
import app.quick_chat.utils.AssetsUtils;

public class ConfigParser {

    private Context context;

    public ConfigParser() {
        context = CoreApp.getInstance().getApplicationContext();
    }

    public String getConfigsAsJsonString(String fileName) throws IOException {
        return AssetsUtils.getJsonAsString(fileName, context);
    }

    public JSONObject getConfigsAsJson(String fileName) throws IOException, JSONException {
        return new JSONObject(getConfigsAsJsonString(fileName));
    }

    public String getConfigByName(JSONObject jsonObject, String fieldName) throws JSONException {
        return jsonObject.getString(fieldName);
    }
}
