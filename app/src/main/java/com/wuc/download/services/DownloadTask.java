package com.wuc.download.services;

import android.content.Context;
import android.content.Intent;

import com.wuc.download.db.ThreadDAOImpl;
import com.wuc.download.entities.FileInfo;
import com.wuc.download.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: wuchao
 * @date: 2018/5/23 10:18
 * @desciption: 下载任务
 */
public class DownloadTask {
    //线程池
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();
    public boolean isPause = false;
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAOImpl mThreadDAO;
    private long mFinished = 0;
    //线程个数
    private int mThreadCount = 1;
    //线程集合
    private List<DownloadThread> mDownloadThreads;
    private Timer mTimer=new Timer();//定时器

    public DownloadTask(Context context, FileInfo fileInfo, int count) {
        mContext = context;
        mFileInfo = fileInfo;
        mThreadCount = count;
        mThreadDAO = new ThreadDAOImpl(context);
    }

    public void download() {
        //读取数据库线程信息
        List<ThreadInfo> threadInfos = mThreadDAO.getThreads(mFileInfo.getUrl());
        if (threadInfos.size() == 0) {
            //获取每个线程下载的长度
            long len = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                //初始化线程信息对象
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), i * len,
                        (i + 1) * len - 1, 0);
                //最后个可能除不尽，设置为最大长度
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threadInfos.add(threadInfo);
                //插入下载线程信息
                mThreadDAO.insertThread(threadInfo);
            }

        }
        mDownloadThreads = new ArrayList<>();
        //创建多线程进行下载
        for (ThreadInfo info : threadInfos) {
            DownloadThread thread = new DownloadThread(info);
            DownloadTask.sExecutorService.execute(thread);
            mDownloadThreads.add(thread);
        }
        //启动定时器
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                intent.putExtra("id", mFileInfo.getId());
                mContext.sendBroadcast(intent);
            }
        },1000,1000);
    }

    /**
     * 判断所有的线程是否都执行完毕
     */
    private synchronized void checkAllThreadsFinished() {
        boolean isAllFinished = true;
        //判断线程是否都执行完
        for (DownloadThread thread : mDownloadThreads) {
            if (!thread.isFinished) {
                isAllFinished = false;
                break;
            }
        }
        if (isAllFinished) {
            //取消定时器
            mTimer.cancel();
            //删除线程信息
            mThreadDAO.deleteThread(mFileInfo.getUrl());
            //发送广播通知UI下载任务结束
            Intent intent = new Intent(DownloadService.ACTION_FINISH);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        private boolean isFinished = false;//线程是否执行完毕

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();
            HttpURLConnection conn = null;
            InputStream input = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes =" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //seek()方法，在读写的时候跳过设置好的字节数，从下一个字节数开始读写
                raf.seek(start);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK
                        || conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    while ((len = input.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //累加整个文件完成度
                        mFinished += (long) len;
                        //累加每个线程完成的进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        //在下载暂停时保存下载进度
                        if (isPause) {
                            //保存进度到数据库
                            mThreadDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                    //标识线程执行完毕
                    isFinished = true;
                    //检查下载任务是否执行完毕
                    checkAllThreadsFinished();
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
