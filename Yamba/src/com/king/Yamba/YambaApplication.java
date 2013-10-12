package com.king.Yamba;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;

import java.util.List;

/**
 * 把一些通用的功能代码放到此基类，供其他类使用
 * 通常放一些连接服务器，读取首选项数据等.
 */
public class YambaApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = YambaApplication.class.getSimpleName();
    private static final String URL = "http://yamba.marakana.com/api";
    SharedPreferences prefs;
    private boolean serviceRunning;
    Twitter twitter;
    private StatusData statusData;

    @Override
    public void onCreate() {
        super.onCreate();

        //SharedPreferences可供程序的任何部分所访问，将上下文作为this传人
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //注册监听，使用当前类作为监听器
        this.prefs.registerOnSharedPreferenceChangeListener(this);
        this.statusData = new StatusData(this);
        Log.i(TAG, "onCreate");
    }

    //提供get和set方法供访问和修改这个标志
    public boolean isServiceRunning() {
        return serviceRunning;
    }

    public void setServiceRunning(boolean serviceRunning) {
        this.serviceRunning = serviceRunning;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }


    //应该结束前调用，可做一些清理动作吗，这里只记录Log
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }


    //重构代码，添加私有方法，初始化twitter对象
    //synchronized关键字表示，在同一时刻只能由一个线程执行
    public synchronized Twitter getTwitter() {
        if (twitter == null) {
            String username = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            String apiRoot = prefs.getString("apiRoot", URL);
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(apiRoot)) {
                //连接到twitter.com,使用用户提供的用户名和密码
                this.twitter = new Twitter(username, password);
                this.twitter.setAPIRootUrl(URL);
            }
        }
        return twitter;
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设为null使之失效，当下次试图获取它的引用时，getTwitter（）会重新创建他的实例
        this.twitter = null;
    }

    //让应用的其他部分能够并且只能够通过这个方法来访问数据
    public StatusData getStatusData() {
        return statusData;
    }

    //连接到服务器并储存最新的状态的数据库
    //返回所获得的状态数
    public synchronized int fetchStatusUpdates() {
        Log.d(TAG, "开始获取最新的数据");
        Twitter twitter = this.getTwitter();
        if (twitter == null) {
            Log.d(TAG, "Twitter连接信息没有初始化");
            return 0;
        }
        try {
            List<Status> statusUpdates = twitter.getFriendsTimeline();

            long latestStatusCreateAtTime = this.getStatusData().getLatestStatusCreateAtTime();
            int count = 0;
            ContentValues values = new ContentValues();
            for (Status status : statusUpdates) {
                values.put(StatusData.C_ID, status.getId());
                long createAt = status.getCreatedAt().getTime();
                values.put(StatusData.C_CREATED_AT, createAt);
                values.put(StatusData.C_TEXT, status.getText());
                values.put(StatusData.C_USER, status.getUser().getName());
                Log.d(TAG, "根据ID： " + status.getId() + " 保存");
                this.getStatusData().insertOrIgnore(values);
                if (latestStatusCreateAtTime < createAt) {
                    count++;
                }
            }
            Log.d(TAG, count > 0 ? "取得: " + count + "条更新" : "木有新的更新");
            return count;
        } catch (RuntimeException e) {
            Log.e(TAG, "获取最新数据失败", e);
            return 0;
        }
    }

}
