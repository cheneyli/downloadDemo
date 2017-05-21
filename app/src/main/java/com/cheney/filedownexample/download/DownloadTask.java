package com.cheney.filedownexample.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.cheney.filedownexample.constant.Constant;
import com.cheney.filedownexample.db.DownloadDbConstant;

import java.io.Serializable;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadTask implements Serializable {
    public interface Status {
        int IDLE = 0;
        int WAITING = 1;
        int DOWNLOADING = 2;
        int DOWNLOADED = 3;
        int FAILED = 4;
    }

    private int id;
    private String url;
    private String localPath;
    private long fileTotalSize;
    private long downloadedSize;
    private int status = Status.WAITING;

    private int retryCount = 1;
    private int progress;

    public DownloadTask() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getFileTotalSize() {
        return fileTotalSize;
    }

    public void setFileTotalSize(long fileTotalSize) {
        this.fileTotalSize = fileTotalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized int getRetryCount() {
        return retryCount;
    }

    public synchronized void increaseRetryCount() {
        retryCount++;
        retryCount = Math.min(retryCount, Constant.MAX_RETRY_COUNT);
    }

    public synchronized void decreaseRetryCount() {
        retryCount--;
        retryCount = Math.max(retryCount, 0);
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getStatusText() {
        switch (status) {
            case Status.IDLE:
                return "Idle";
            case Status.WAITING:
                return "Waiting";
            case Status.DOWNLOADING:
                return "Downloading";
            case Status.DOWNLOADED:
                return "Downloaded";
            case Status.FAILED:
                return "Failed";
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof DownloadTask && TextUtils.equals(url, ((DownloadTask) o).getUrl())) {
            return true;
        }
        return false;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        if (url != null) {
            contentValues.put(DownloadDbConstant.DownloadTaskField.URL, url);
        }
        if (localPath != null) {
            contentValues.put(DownloadDbConstant.DownloadTaskField.LOCAL_PATH, localPath);
        }
        contentValues.put(DownloadDbConstant.DownloadTaskField.FILE_TOTAL_SIZE, fileTotalSize);
        contentValues.put(DownloadDbConstant.DownloadTaskField.DOWNLOADED_SIZE, downloadedSize);
        contentValues.put(DownloadDbConstant.DownloadTaskField.STATUS, status);
        contentValues.put(DownloadDbConstant.DownloadTaskField.PROGRESS, progress);
        return contentValues;
    }

    public void fromCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                id = cursor.getInt(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.ID));
                url = cursor.getString(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.URL));
                localPath = cursor.getString(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.LOCAL_PATH));
                fileTotalSize = cursor.getInt(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.FILE_TOTAL_SIZE));
                downloadedSize = cursor.getInt(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.DOWNLOADED_SIZE));
                status = cursor.getInt(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.STATUS));
                progress = cursor.getInt(cursor.getColumnIndex(DownloadDbConstant.DownloadTaskField.PROGRESS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
