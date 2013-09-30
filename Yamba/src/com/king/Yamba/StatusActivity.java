package com.king.Yamba;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

public class StatusActivity extends Activity implements View.OnClickListener, TextWatcher {
    private static final String TAG = "StatusActivity";
    private static final String URL = "http://yamba.marakana.com/api";
    EditText status_ed;
    Button update_bt;
    Twitter twitter;
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


        twitter = new Twitter("student", "password");
        twitter.setAPIRootUrl(URL);
    }

    //点击更新
    @Override
    public void onClick(View v) {
        String status = status_ed.getText().toString();
        new PostToTwitter().execute(status);
        Log.d(TAG, "onClick");
    }


    //异步线程，发送消息
    class PostToTwitter extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... statuses) {
            try {
                Twitter.Status status = twitter.updateStatus(statuses[0]);
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
            Toast.makeText(StatusActivity.this, s, Toast.LENGTH_LONG).show();
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
