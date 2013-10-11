package com.king.Yamba.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.king.Yamba.YambaApplication;
import com.king.Yamba.util.DbHelper;


/**
 * 定时更新Twitter上的最新消息存入本地
 */
public class UpdateService extends Service {
    private static final String TAG = "UpdateService";
    private boolean runFlag = false;
    static final int time = 30000;//30秒
    private Updater updater;
    private YambaApplication yamba;
    DbHelper dbHelper;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //独立线程，只需创建一次，故在onCreate中
        this.updater = new Updater();
        //获取YambaApplication对象的引用
        this.yamba = (YambaApplication) getApplication();

        dbHelper = new DbHelper(this);

        Log.d(TAG, "onCreate");
    }

    //每次收到startIntent的intent的时候都会调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.runFlag = true;
        this.updater.start();
        this.yamba.setServiceRunning(true);
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    //收到stopService时会调用
    @Override
    public void onDestroy() {
        super.onDestroy();

        this.runFlag = false;
        //interrupt中断一个线程的执行，随后设置变量的引用设置为null，以便垃圾回收
        this.updater.interrupt();
        this.updater = null;
        this.yamba.setServiceRunning(false);
        Log.d(TAG, "onDestroy");
    }

    /**
     * 负责从在线服务抓取更新的数据
     */
    private class Updater extends Thread {
       // List<Twitter.Status> timeline;
        public Updater() {
            super("UpdaterService-Updater");
        }

        @Override
        public void run() {
            //创建一个对服务类的引用
            UpdateService updateService = UpdateService.this;
            while (updateService.runFlag) {
                Log.d(TAG, "正在进行更新");
                try {
                    //从服务器获取数据
                    YambaApplication yamba = (YambaApplication) updateService.getApplication();
                    int newUpdates = yamba.fetchStatusUpdates();
                    if (newUpdates > 0){
                       Log.d(TAG,"有新的状态消息");
                    }
                    Thread.sleep(time);
                    //timeline = yamba.getTwitter().getFriendsTimeline();

/*
                    重构，在YambaApplication中处理
                    //打开数据库写入
                    db = dbHelper.getWritableDatabase();

                    //遍历时间戳数据并输出,ContentValues暂存数据的容器,键值对数据结构
                    ContentValues values = new ContentValues();
                    for (Twitter.Status status : timeline){
                        //插入数据库
                        values.clear();
                        values.put(DbHelper.C_ID, status.id);
                        values.put(DbHelper.C_CREATED_AT, status.createdAt.getTime());
                     //   values.put(DbHelper.C_SOURCE,status.source);
                        values.put(DbHelper.C_TEXT,status.text);
                        values.put(DbHelper.C_USER,status.user.name);

                        try {
                            db.insertOrThrow(DbHelper.TABLE, null, values);
                            Log.d(TAG,String.format("%s: %s",status.user.name,status.text));
                        } catch (SQLException e) {
                        }
                    }
                    //关闭数据库
                    db.close();*/
                } catch (InterruptedException e) {
                    updateService.runFlag = false;
                }
            }
        }
    }

}
