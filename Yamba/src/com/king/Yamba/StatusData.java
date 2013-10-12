package com.king.Yamba;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 所有数据库相关功能的公用容器
 */
public class StatusData {
    private static final String TAG = StatusData.class.getSimpleName();

    static final String DATABASE = "timeline.db";
    static final int VERSION = 1;
    static final String TABLE = "timeline";

    public static final String C_ID = "_id";
    public static final String C_CREATED_AT = "created_at";
    public static final String C_TEXT = "txt";
    public static final String C_USER = "user";

    private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";
    private static final String[] MAX_CREATED_AT_COLUMNS = {"max(" + StatusData.C_CREATED_AT + ")"};
    private static final String[] DB_TEXT_COLUMNS = {C_TEXT};


    /**
     * 辅助类，创建数据库和更新数据库
     */
    public class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "创建数据库: " + DATABASE);
            String sql = "create table " + TABLE + " (" + C_ID + " integer primary key, "
                    + C_CREATED_AT + " int, " + C_USER + " text, " + C_TEXT + " text)";
            db.execSQL(sql);

        }

        //在newVersion ！= oldVersion时触发
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //一般在这里执行ALTER TABLE语句，不过这里我们不做数据库的迁移

            db.execSQL("drop table  " + TABLE);//删除旧的数据库
            Log.d(TAG, "onUpgraded");
            this.onCreate(db);
        }
    }

    private final DbHelper dbHelper;

    public StatusData(Context context) {
        this.dbHelper = new DbHelper(context);
        Log.i(TAG, "初始化数据");
    }

    public void close() {
        this.dbHelper.close();
    }

    //DbHelper中db.insert 的改进
    public void insertOrIgnore(ContentValues values) {
        Log.d(TAG,"插入或忽略" + values);
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);//CONFLICT_IGNORE表示有重复ID冲突是，这样的异常被忽略
        }finally {
            db.close();
        }
    }

    /**
     * Cursor可以访问的列为_id,create_at,user,txt
     * 返回数据库中的所有记录的消息，时间在前的排在前
     */
    public Cursor getStatusUpdates(){
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
    }

    /**
     * 返回数据库中最后一条状态的时间戳
     */
    public long getLatestStatusCreateAtTime(){
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null, null, null, null);
            try {
                return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;//a ? b : c条件表达式，表示如果a为真，则表达式值为b，如果a为假，则表达式值为c
            }finally {
                cursor.close();
            }
        }finally {
            db.close();
        }
    }

    /**
     *
     * @param id 目标状态的ID
     * @return 目标状态的正文
     * 用于返回给定ID的实际文本内容
     */
    public String getStatusTextById(long id){
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        try {
             Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null, null, null, null);
            try {
                 return cursor.moveToNext() ? cursor.getString(0) : null;
            }finally {
                cursor.close();
            }
        }finally {
            db.close();
        }
    }

    public void delete() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

}
