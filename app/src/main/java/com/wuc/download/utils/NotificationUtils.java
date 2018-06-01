package com.wuc.download.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.wuc.download.MainActivity;
import com.wuc.download.R;
import com.wuc.download.entities.FileInfo;
import com.wuc.download.services.DownloadService;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;

/**
 * @author: wuchao
 * @date: 2018/5/30 16:56
 * @desciption: 通知栏工具类
 */
public class NotificationUtils extends ContextWrapper {
    public static final String CHANNEL_ID = "default";
    private static final String CHANNEL_NAME = "Default Channel";
    private static final String CHANNEL_DESCRIPTION = "this is default channel!";
    private Context mContext;
    private NotificationManager mNotificationManager;
    private Map<Integer, Notification> mNotificationMap;

    public NotificationUtils(Context context) {
        super(context);
        mContext = context;
        //创建通知集合
        mNotificationMap = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.canBypassDnd();//是否绕过请勿打扰模式
        channel.enableLights(true);//闪光灯
        channel.setLockscreenVisibility(VISIBILITY_SECRET);//锁屏显示通知
        channel.setLightColor(Color.RED);//闪关灯的灯光颜色
        channel.canShowBadge();//桌面launcher的消息角标
        channel.enableVibration(true);//是否允许震动
        channel.getAudioAttributes();//获取系统通知响铃声音的配置
        channel.getGroup();//获取通知取到组
        channel.setBypassDnd(true);//设置可绕过  请勿打扰模式
        channel.setVibrationPattern(new long[]{100, 100, 200});//设置震动模式
        channel.shouldShowLights();//是否会有灯光
        getNotificationManager().createNotificationChannel(channel);
    }

    /**
     * 获取通知系统服务
     *
     * @return
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * 显示通知
     *
     * @param fileInfo
     */
    public void showNotification(FileInfo fileInfo) {
        //判断通知是否已显示
        if (!mNotificationMap.containsKey(fileInfo.getId())) {
            //创建通知对象
            NotificationCompat.Builder notification = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            } else {
                notification = new NotificationCompat.Builder(getApplicationContext());
                notification.setPriority(PRIORITY_DEFAULT);
            }
            //设置滚动文字
            notification.setTicker("开始下载");
            //notification.tickerText = "开始下载";
            //设置通知时间
            notification.setWhen(System.currentTimeMillis());
            //notification.when = System.currentTimeMillis();
            //图片
            notification.setSmallIcon(R.mipmap.ic_launcher);
            //notification.icon = R.mipmap.ic_launcher;
            //通知特性
            notification.setAutoCancel(true);
            //notification.flags = Notification.FLAG_AUTO_CANCEL;
            //点击通知栏
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            // notification.contentIntent = pendingIntent;
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);
            notification.setContentIntent(pendingIntent);
            remoteViews.setTextViewText(R.id.txt_fileName, fileInfo.getFileName());
            //设置开始按钮操作
            Intent intentStart = new Intent(mContext, DownloadService.class);
            intentStart.setAction(DownloadService.ACTION_START);
            intentStart.putExtra("fileInfo", fileInfo);
            PendingIntent piStart = PendingIntent.getService(mContext, 0, intentStart, 0);
            remoteViews.setOnClickPendingIntent(R.id.start, piStart);
            //设置结算按钮操作
            Intent intentStop = new Intent(mContext, DownloadService.class);
            intentStop.setAction(DownloadService.ACTION_STOP);
            intentStop.putExtra("fileInfo", fileInfo);
            PendingIntent piStop = PendingIntent.getService(mContext, 0, intentStop, 0);
            remoteViews.setOnClickPendingIntent(R.id.pause, piStop);
            //设置notification的视图
            notification.setContent(remoteViews);
            //notification.contentView = remoteViews;
            //发出通知
            getNotificationManager().notify(fileInfo.getId(), notification.build());
            //把通知加到集合中
            mNotificationMap.put(fileInfo.getId(), notification.build());
        }
    }

    /**
     * 取消通知
     *
     * @param id
     */
    public void cancelNotification(int id) {
        mNotificationManager.cancel(id);
        mNotificationMap.remove(id);
    }

    /**
     * 更新进度
     *
     * @param id
     * @param progress
     */
    public void updateNotification(int id, int progress) {
        Notification notification = mNotificationMap.get(id);
        if (notification != null) {
            //修改进度条
            notification.contentView.setProgressBar(R.id.progressBar, 100, progress, false);
            mNotificationManager.notify(id, notification);
        }
    }
}
