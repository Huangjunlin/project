package com.king.Yamba.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.king.Yamba.R;
import com.king.Yamba.YambaApplication;
import com.king.Yamba.service.UpdateService;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

public class StatusActivity extends Activity implements View.OnClickListener, TextWatcher {
    private static final String TAG = "StatusActivity";
    EditText status_ed;
    Button update_bt;
    TextView textCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);

        status_ed = (EditText) findViewById(R.id.status_ed);
        update_bt = (Button) findViewById(R.id.status_bt);
        update_bt.setOnClickListener(this);

        textCount = (TextView) findViewById(R.id.textCount);
        textCount.setText(Integer.toString(140));
        textCount.setTextColor(Color.GREEN);
        status_ed.addTextChangedListener(this);


        /*
        写死了，下面重构了，getTwitter（）
        twitter = new Twitter("student", "password");
        twitter.setAPIRootUrl(URL);*/

       /*
       重构，通用功能写到YambaApplication.class
       //SharedPreferences可供程序的任何部分所访问，将上下文作为this传人
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //注册监听，使用当前类作为监听器
        prefs.registerOnSharedPreferenceChangeListener(this);*/

    }


    //点击更新状态
    @Override
    public void onClick(View v) {

       /*
       下面重构
       String status = status_ed.getText().toString();
        new PostToTwitter().execute(status);
        Log.d(TAG, "onClick");*/

        //4.0之后在主线程里面执行Http请求都会报这个错android.os.NetworkOnMainThreadException
        try{
        //getTwitter().setStatus(status_ed.getText().toString());
            new PostToTwitter().execute(status_ed.getText().toString());
        }catch (TwitterException e){
               Log.d(TAG, "Twitter更新状态失败");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //从上下文中获取MenuInflater
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        //要让菜单显示出来必须返回true
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemPrefs:
                startActivity(new Intent(this, PrefsActivity.class));
                break;
            case R.id.itemStartService:
                startService(new Intent(this, UpdateService.class));
                break;
            case R.id.itemStopService:
                stopService(new Intent(this, UpdateService.class));
                break;
        }
        return true;
    }

    /*//选项数据变化时触发
    重构，通用功能写到YambaApplication.class
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设为null使之失效，当下次试图获取它的引用时，getTwitter（）会重新创建他的实例
        twitter = null;
    }*/


    /*
    通用功能写到YambaApplication.class
    重构代码，添加私有方法，初始化twitter对象
    private Twitter getTwitter(){
        if (twitter == null){
            String username, password, apiRoot;
            username = prefs.getString("username","");
            password = prefs.getString("password","");
            apiRoot = prefs.getString("apiRoot",URL);
            //连接到twitter.com,使用用户提供的用户名和密码
            twitter = new Twitter(username, password);
            twitter.setAPIRootUrl(URL);

        }
        return twitter;
    }
*/

    //异步线程，发送消息
    class PostToTwitter extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... statuses) {
            try {
                //Twitter.Status status = twitter.updateStatus(statuses[0]);
                YambaApplication yamba = (YambaApplication) getApplication();
                Twitter.Status status = yamba.getTwitter().updateStatus(statuses[0]);
                return status.text;
            } catch (TwitterException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return "发送失败";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);    //To change body of overridden methods use File | Settings | File Templates.
        }

        //在后台任务执行完之后触发
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(StatusActivity.this, s+" :发送成功", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable statusText) {
        int count = 140 - statusText.length();
        textCount.setText(Integer.toString(count));
        textCount.setTextColor(Color.GREEN);
        if (count < 10) {
            textCount.setTextColor(Color.YELLOW);
        }
        if (count < 0) {
            textCount.setTextColor(Color.RED);
        }
    }
}
