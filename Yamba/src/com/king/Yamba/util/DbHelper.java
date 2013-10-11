package com.king.Yamba.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * 辅助类，创建数据库和更新数据库
 */
public class DbHelper extends SQLiteOpenHelper {
    static final String TAG = "DbHelper";
    static final String DB_NAME = "timeline.db";
    static final int DB_VERSION = 1;
    public static final String TABLE = "timeline";
    public static final String C_ID = BaseColumns._ID;
    public static final String C_CREATED_AT = "created_at";
    public static final String C_SOURCE = "source";
    public static final String C_TEXT = "txt";
    public static final String C_USER = "user";
    Context context;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE + " (" + C_ID + " int primary key, "
                + C_CREATED_AT + " int, " + C_USER + " text, " + C_TEXT + " text)";
        db.execSQL(sql);
        Log.d(TAG, "onCreate sql: "+ sql);
    }

    //在newVersion ！= oldVersion时触发
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       //一般在这里执行ALTER TABLE语句，不过这里我们不做数据库的迁移

        db.execSQL("drop table if exists "+TABLE);//删除旧的数据库
        Log.d(TAG , "onUpgraded");
        onCreate(db);
    }
}
