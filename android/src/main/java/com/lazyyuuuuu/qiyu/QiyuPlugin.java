package com.lazyyuuuuu.qiyu;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.qiyukf.unicorn.api.ConsultSource;
import com.qiyukf.unicorn.api.ImageLoaderListener;
import com.qiyukf.unicorn.api.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.UnicornImageLoader;
import com.qiyukf.unicorn.api.YSFOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * QiyuPlugin
 */
public class QiyuPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private static String CHANNEL_NAME = "plugins.lazyyuuuuu.io/qiyu";
    private static String EVENT_CHANNEL_NAME = "plugins.lazyyuuuuu.io/event_qiyu";
    private Context context;
    private MethodChannel methodChannel;
    private EventChannel eventChannel;
//    private EventChannel.EventSink buttonClickCallbackEvent;
//    private EventChannel.EventSink onURLClickCallbackEvent;
//    private EventChannel.EventSink onBotClickCallbackEvent;
//    private EventChannel.EventSink onQuitWaitingCallbackEvent;
//    private EventChannel.EventSink onPushMessageClickCallbackEvent;
//    private EventChannel.EventSink onBotCustomInfoCallbackEvent;
//    private EventChannel.EventSink unreadCountChangedEvent;
//    private EventChannel.EventSink sessionListChangedEvent;
//    private EventChannel.EventSink receiveMessageEvent;

    private YSFOptions options;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        QiyuPlugin qiyuPlugin = new QiyuPlugin();
        qiyuPlugin.context = registrar.activity().getApplication().getApplicationContext();
        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(qiyuPlugin);
        final EventChannel event = new EventChannel(registrar.messenger(), EVENT_CHANNEL_NAME);
        event.setStreamHandler(qiyuPlugin);
    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "register":
                String appKey = call.argument("appKey");
                String appName = call.argument("appName");

                Unicorn.init(context, appKey, options(), new UnicornImageLoader() {
                    @Nullable
                    @Override
                    public Bitmap loadImageSync(String uri, int width, int height) {
                        return null;
                    }

                    @Override
                    public void loadImage(String uri, int width, int height, ImageLoaderListener listener) {

                    }
                });
                result.success(null);
                break;
            case "logout":
                Unicorn.logout();
                result.success(null);
                break;
            case "openServiceWindow":
                String sessionTitle = call.argument("sessionTitle");
                Integer groupId = call.argument("groupId");
                Integer staffId = call.argument("staffId");
                Integer robotId = call.argument("robotId");
                Integer vipLevel = call.argument("vipLevel");
                Boolean openRobotInShuntMode = call.argument("openRobotInShuntMode");
                Integer commonQuestionTemplateId = call.argument("commonQuestionTemplateId");
                Map<String, String> sourceDict = call.argument("source");
                Map<String, Object> commodityInfoDict = call.argument("commodityInfo");
                List<Map<String, Object>> buttonInfoList = call.argument("buttonInfoArray");
                if (sourceDict != null) {
                    ConsultSource source = new ConsultSource(sourceDict.get("urlString"), sourceDict.get("title"), sourceDict.get("customInfo"));
                    Unicorn.openServiceActivity(context, sessionTitle, source);
                    result.success(null);
                }

                break;
//            case "sendCommodityInfo":
//                result.success(null);
//                break;
//            case "sendSelectedCommodityInfo":
//                result.success(null);
//                break;
//            case "setCustomUIConfig":
//                result.success(null);
//                break;
//            case "restoreCustomUIConfigToDefault":
//                result.success(null);
//                break;
//            case "setDeactivateAudioSessionAfterComplete":
//                result.success(null);
//                break;
//            case "getUnreadCount":
//                result.success(null);
//                break;
//            case "setUserInfo":
//                result.success(null);
//                break;
//            case "setAuthToken":
//                result.success(null);
//                break;
//            case "setUserInfoWithVerificationResultCallback":
//                result.success(null);
//                break;
//            case "getPushMessage":
//                result.success(null);
//                break;
//            case "registerPushMessageNotificationCallback":
//                result.success(null);
//                break;
//            case "cleanResourceCacheCallback":
//                result.success(null);
//                break;
//            case "trackHistory":
//                result.success(null);
//                break;
//            case "getQiyuLogPath":
//                result.success(null);
//                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private YSFOptions options() {
        options = new YSFOptions();
        options.statusBarNotificationConfig = new StatusBarNotificationConfig();
        return options;
    }

    public static boolean inMainProcess(Context context) {
        String mainProcessName = context.getApplicationInfo().processName;
        String processName = getProcessName();
        return TextUtils.equals(mainProcessName, processName);
    }

    /**
     * 获取当前进程名
     */
    private static String getProcessName() {
        BufferedReader reader = null;
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            reader = new BufferedReader(new FileReader(file));
            return reader.readLine().trim();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        context = applicationContext;
        methodChannel = new MethodChannel(messenger, CHANNEL_NAME);
        methodChannel.setMethodCallHandler(this);
        eventChannel = new EventChannel(messenger, EVENT_CHANNEL_NAME);
        eventChannel.setStreamHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        if (arguments == null) {
            events.error("", "arguments需要传入字符串！", null);
            return;
        }

//        switch (arguments.toString()) {
//            case "onButtonClickCallback":
//                buttonClickCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onURLClickCallback":
//                onURLClickCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onBotClickCallback":
//                onBotClickCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onQuitWaitingCallback":
//                onQuitWaitingCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onPushMessageClickCallback":
//                onPushMessageClickCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onBotCustomInfoCallback":
//                onBotCustomInfoCallbackEvent = events;
//                events.success(null);
//                break;
//            case "onUnreadCountChanged":
//                unreadCountChangedEvent = events;
//                events.success(null);
//                break;
//            case "onSessionListChanged":
//                sessionListChangedEvent = events;
//                events.success(null);
//                break;
//            case "onReceiveMessage":
//                receiveMessageEvent = events;
//                events.success(null);
//                break;
//            default:
        events.error("", "没有对应的事件", null);
//                break;
//        }
    }

    @Override
    public void onCancel(Object arguments) {
//        switch (arguments.toString()) {
//            case "onButtonClickCallback":
//                buttonClickCallbackEvent = null;
//                break;
//            case "onURLClickCallback":
//                onURLClickCallbackEvent = null;
//                break;
//            case "onBotClickCallback":
//                onBotClickCallbackEvent = null;
//                break;
//            case "onQuitWaitingCallback":
//                onQuitWaitingCallbackEvent = null;
//                break;
//            case "onPushMessageClickCallback":
//                onPushMessageClickCallbackEvent = null;
//                break;
//            case "onBotCustomInfoCallback":
//                onBotCustomInfoCallbackEvent = null;
//                break;
//            case "onUnreadCountChanged":
//                unreadCountChangedEvent = null;
//                break;
//            case "onSessionListChanged":
//                sessionListChangedEvent = null;
//                break;
//            case "onReceiveMessage":
//                receiveMessageEvent = null;
//                break;
//            default:
//                break;
//        }
    }
}
