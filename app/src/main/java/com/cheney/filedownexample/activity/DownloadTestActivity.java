package com.cheney.filedownexample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cheney.filedownexample.R;
import com.cheney.filedownexample.adapter.DownloadListAdapter;
import com.cheney.filedownexample.constant.Constant;
import com.cheney.filedownexample.constant.ExtraInfo;
import com.cheney.filedownexample.download.DownloadManager;
import com.cheney.filedownexample.download.DownloadTask;
import com.cheney.filedownexample.events.DownloadStatusEvent;
import com.cheney.filedownexample.handler.EventSender;
import com.cheney.filedownexample.service.DownloadService;
import com.cheney.filedownexample.utils.LogUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by star on 16/4/7.
 */
public class DownloadTestActivity extends Activity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private DownloadListAdapter adapter;
    private EditText inputEdit;

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_test);
        EventSender.getInstance().register(this);
        initView();

        asyncLoadTask();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.download_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DownloadListAdapter(this);
        recyclerView.setAdapter(adapter);

        inputEdit = (EditText) findViewById(R.id.url_input);
        View button = findViewById(R.id.btn_download);
        button.setOnClickListener(this);
    }

    private void asyncLoadTask() {
        new Thread(() -> {
            List<DownloadTask> list = DownloadManager.getInstance().getAllTasksInDb();
            for (DownloadTask task : list) {
                sendFileDownloadCommand(task.getUrl(), true);
            }
            runOnUiThread(() -> adapter.setTasks(list));
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveDownloadStatusEvent(DownloadStatusEvent downloadStatusEvent) {
        DownloadTask task = downloadStatusEvent.downloadTask;
        LogUtil.logI("onReceiveDownloadStatusEvent");
        if (task != null) {
            adapter.updateTask(task);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventSender.getInstance().unregister(this);
    }


    @Override
    public void onClick(View v) {
        String url = inputEdit.getText().toString();
        if (TextUtils.isEmpty(url)) {
            url = getTestUrl(clickCount);
        }
        clickCount++;

        if (!TextUtils.isEmpty(url)) {
            DownloadTask task = new DownloadTask();
            task.setUrl(url);
            adapter.addTask(task);
            sendFileDownloadCommand(url, false);
        } else {
            Toast.makeText(DownloadTestActivity.this, "Pls input valid url", Toast.LENGTH_SHORT).show();
        }
        inputEdit.setText("");
    }

    private String getTestUrl(int count) {
        if (count % 2 == 0) {
            return "http://zhcn.web.cdn.cootekservice.com/download/TouchPal%20Dialer/ChuBaoDianHua_01006A.apk";
        } else {
            return "http://180.153.105.143/imtt.dd.qq.com/16891/11701B933C5473B5B702FD8B71DFC690.apk";
        }
    }

    private void sendFileDownloadCommand(String url, boolean isPending) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(Constant.ACTION_START);
        intent.putExtra(ExtraInfo.FILE_URL, url);
        intent.putExtra(ExtraInfo.FILE_DOWNLOAD_PENDING, isPending);
        startService(intent);
    }
}
