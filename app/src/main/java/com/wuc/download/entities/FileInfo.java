package com.wuc.download.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: wuchao
 * @date: 2018/5/22 15:31
 * @desciption:
 */
public class FileInfo implements Parcelable {

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
    private int id;
    private String url;
    private String fileName;
    private long length;
    private long finished;

    public FileInfo() {
    }

    public FileInfo(int id, String url, String fileName, long length, long finished) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.length = length;
        this.finished = finished;
    }

    protected FileInfo(Parcel in) {
        id = in.readInt();
        url = in.readString();
        fileName = in.readString();
        length = in.readLong();
        finished = in.readLong();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(fileName);
        dest.writeLong(length);
        dest.writeLong(finished);
    }

    @Override
    public String toString() {
        return "id:" + id + ",url:" + url + ",fileName:" + fileName + ",length:" + length
                + ",finished:" + finished;
    }
}
