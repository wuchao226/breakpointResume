# breakpointResume
Android Service-多线程断点续传下载
1. 使用BroadcastReceiver和Handler实现Activity和Service的通信
2. 使用Messenger传递Handler
3. Http协议字段 Range "bytes="+start+"-"+end
4. RandomAccessFile 设置吸入位置
### Handler实现Activity和Service的通信
- 使用Messenger实现跨进程通信
- Messenger包含Handler的引用
##### 实现步骤
- 在Service中创建Messenger包含Handler的引用
- 在onBinder方法中返回Messenger
- Activity绑定Service获取Service的Messenger
- 在Activity中创建Messenger包含Handler的引用
- 使用Service的Messenger发送信息给Service的Handler，信息包括Activity的Messenger

![image](https://github.com/wuchao226/breakpointResume/blob/master/images/preview.png)
