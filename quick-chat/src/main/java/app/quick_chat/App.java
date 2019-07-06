package app.quick_chat;


import androidx.multidex.MultiDex;

import java.io.IOException;

import app.quick_chat.models.SampleConfigs;
import app.quick_chat.utils.ActivityLifecycle;
import app.quick_chat.utils.Consts;
import app.quick_chat.utils.configs.ConfigUtils;


public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();
    private static SampleConfigs sampleConfigs;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        initSampleConfigs();
        MultiDex.install(this);
    }

    private void initSampleConfigs() {
        try {
            sampleConfigs = ConfigUtils.getSampleConfigs(Consts.SAMPLE_CONFIG_FILE_NAME);
//            sampleConfigs.setChatSocketTimeout(60000);
//            sampleConfigs.setKeepAlive(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SampleConfigs getSampleConfigs() {
        return sampleConfigs;
    }
}