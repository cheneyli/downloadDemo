package com.cheney.filedownexample.constant;

import android.os.Environment;

/**
 * Created by cheney on 2017/5/20.
 */

public interface Constant {
    int MAX_DOWNLOAD_THREAD = 3;

    String DOWNLOAD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/download_sample/";
    int DEFAULT_THREAD_COUNT = 2;

    int MAX_RETRY_COUNT = 5;

    String ACTION_START = "ACTION_START";
    String ACTION_STOP = "ACTION_STOP";
}
