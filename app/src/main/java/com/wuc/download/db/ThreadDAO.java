package com.wuc.download.db;

import com.wuc.download.entities.ThreadInfo;

import java.util.List;

/**
 * @author: wuchao
 * @date: 2018/5/22 18:06
 * @desciption: 数据访问接口
 */
public interface ThreadDAO {
    /**
     * 插入线程信息
     *
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     *
     * @param url
     */
    public void deleteThread(String url);

    /**
     * 更新线程信息
     *
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url, int thread_id, long finished);

    /**
     * 获取文件的线程信息
     *
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     *
     * @param url
     * @param thread_id
     * @return
     */
    public boolean isExists(String url, int thread_id);
}
