package com.wuc.download.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wuc.download.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: wuchao
 * @date: 2018/5/22 16:09
 * @desciption:
 */
public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISH = "ACTION_FINISH";
    public static final int MSG_INIT = 0x01;
    //绑定标识
    public static final int MSG_BINDER = 0x02;
    public static final int MSG_START = 0x03;
    public static final int MSG_STOP = 0x04;
    public static final int MSG_UPDATE = 0x05;
    public static final int MSG_FINISH = 0x06;
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
    private Messenger mMessengerActivity;//Activity中的Messenger
    private Map<Integer, DownloadTask> mDownloadTasks = new LinkedHashMap<>();
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d("DownloadService", "init:" + fileInfo);
                    //启动下载任务
                    DownloadTask task = new DownloadTask(DownloadService.this, mMessengerActivity, fileInfo, 3);
                    task.download();
                    mDownloadTasks.put(fileInfo.getId(), task);
                    //发动启动命令的广播
//                    Intent intent = new Intent();
//                    intent.setAction(DownloadService.ACTION_START);
//                    intent.putExtra("fileInfo", fileInfo);
//                    sendBroadcast(intent);
                    Message message = Message.obtain();
                    message.what = DownloadService.MSG_START;
                    message.obj = fileInfo;
                    try {
                        mMessengerActivity.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_BINDER:
                    //处理绑定的Messenger
                    mMessengerActivity = msg.replyTo;
                    break;
                case MSG_START:
                    FileInfo fileInfo1 = (FileInfo) msg.obj;
                    InitThread initThread = new InitThread(fileInfo1);
                    DownloadTask.sExecutorService.execute(initThread);
                    break;
                case MSG_STOP:
                    FileInfo fileInfo2 = (FileInfo) msg.obj;
                    DownloadTask task2 = mDownloadTasks.get(fileInfo2.getId());
                    if (task2 != null) {
                        task2.isPause = true;
                    }
                    break;

            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
//            FileInfo fileInfo = intent.getParcelableExtra("fileInfo");
//            Log.d("DownloadService", "start：" + fileInfo);
//            InitThread initThread = new InitThread(fileInfo);
//            DownloadTask.sExecutorService.execute(initThread);
        } else if (ACTION_STOP.equals(intent.getAction())) {
//            FileInfo fileInfo = intent.getParcelableExtra("fileInfo");
//            Log.d("DownloadService", "stop：" + fileInfo);
//            DownloadTask task = mDownloadTasks.get(fileInfo.getId());
//            if (task != null) {
//                task.isPause = true;
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //创建一个Messenger对象包含Handler的引用
        Messenger messenger = new Messenger(mHandler);
        //返回Messenger的Binder
        return messenger.getBinder();
    }

    class InitThread extends Thread {

        private FileInfo mFileInfo;

        public InitThread(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            super.run();
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //链接网路文件
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000);
                conn.setRequestMethod("GET");
                int length = -1;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //获取文件长度
                    length = conn.getContentLength();
                }
                if (length < 0) {
                    return;
                }

                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //创建本地文件
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //设置文件长度
                raf.setLength(length);

                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
