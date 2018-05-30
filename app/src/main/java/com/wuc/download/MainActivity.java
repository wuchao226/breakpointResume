package com.wuc.download;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.wuc.download.entities.FileInfo;
import com.wuc.download.services.DownloadService;
import com.wuc.download.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<FileInfo> mFileList;
    private FileListAdapter mAdapter;
    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                long finished = intent.getLongExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finished);
                mNotificationUtils.updateNotification(id, (int) finished);
            } else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
                FileInfo fileInfo = intent.getParcelableExtra("fileInfo");
                mAdapter.updateProgress(fileInfo.getId(), 0);
                mNotificationUtils.cancelNotification(fileInfo.getId());
                Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_LONG).show();
            }else if (DownloadService.ACTION_START.equals(intent.getAction())) {
                FileInfo fileInfo = intent.getParcelableExtra("fileInfo");
                mNotificationUtils.showNotification(fileInfo);
            }
        }
    };
    private NotificationUtils mNotificationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        mNotificationUtils = new NotificationUtils(this);

        mFileList = new ArrayList<>();
        FileInfo fileInfo = new FileInfo(0, "http://xlc.qxgs.net/api/pc/image/download/sp",
                "sp.apk", 0, 0);
        FileInfo fileInfo1 = new FileInfo(1, "http://xlc.qxgs.net/api/pc/image/download/sp",
                "sp.apk1", 0, 0);
        FileInfo fileInfo2 = new FileInfo(2, "http://xlc.qxgs.net/api/pc/image/download/sp",
                "sp.apk2", 0, 0);
        FileInfo fileInfo3 = new FileInfo(3, "http://xlc.qxgs.net/api/pc/image/download/sp",
                "sp.apk3", 0, 0);
        FileInfo fileInfo4 = new FileInfo(4, "http://xlc.qxgs.net/api/pc/image/download/sp",
                "sp.apk4", 0, 0);
        mFileList.add(fileInfo);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);

        mAdapter = new FileListAdapter(this, mFileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        intentFilter.addAction(DownloadService.ACTION_FINISH);
        intentFilter.addAction(DownloadService.ACTION_START);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

        }
    }
}
