package app.quick_chat.utils.configs;

import com.google.gson.Gson;

import java.io.IOException;

import app.quick_chat.models.SampleConfigs;


public class ConfigUtils extends CoreConfigUtils {

    public static SampleConfigs getSampleConfigs(String fileName) throws IOException {
        ConfigParser configParser = new ConfigParser();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(fileName), SampleConfigs.class);
    }
}
