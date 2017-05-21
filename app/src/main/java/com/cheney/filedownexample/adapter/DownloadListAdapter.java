package com.cheney.filedownexample.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheney.filedownexample.R;
import com.cheney.filedownexample.download.DownloadManager;
import com.cheney.filedownexample.download.DownloadTask;
import com.cheney.filedownexample.listener.DownloadListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {

    private List<DownloadTask> tasks = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    public DownloadListAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setTasks(List<DownloadTask> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
        notifyDataSetChanged();
    }

    public void addTask(DownloadTask task) {
        if (tasks.contains(task)) {
            return;
        }
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    public void updateTask(DownloadTask task) {
        int pos = tasks.indexOf(task);
        if (pos >= 0) {
            tasks.set(pos, task);
            notifyItemChanged(pos);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.view_download_test_download_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownloadTask task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView urlView;
        private TextView localPathView;
        private TextView statusTv;
        private TextView startButton;
        private TextView totalSizeView;
        private TextView downloadedSizeView;

        private DownloadTask task;
        public ViewHolder(View itemView) {
            super(itemView);
            urlView = (TextView) itemView.findViewById(R.id.id_url);
            localPathView = (TextView) itemView.findViewById(R.id.id_local_path);
            statusTv = (TextView) itemView.findViewById(R.id.status_tv);
            startButton = (TextView) itemView.findViewById(R.id.start_stop_button);
            totalSizeView = (TextView) itemView.findViewById(R.id.total_size);
            downloadedSizeView = (TextView) itemView.findViewById(R.id.downloaded_size);

            View item = itemView.findViewById(R.id.id_item_region);
            item.setOnLongClickListener(v -> {
                if (task == null || task.getStatus() != DownloadTask.Status.DOWNLOADED) {
                    return false;
                }
                // TODO: 2017/5/20
                return true;
            });

            startButton.setOnClickListener(v -> {
                switch (task.getStatus()) {
                    case DownloadTask.Status.IDLE:
                        if (context instanceof DownloadListener) {
                            DownloadManager.getInstance().addListener(task.getUrl(), (DownloadListener) context);
                        }
                        DownloadManager.getInstance().resumeTask(task.getUrl());
                        break;
                    case DownloadTask.Status.DOWNLOADING:
                    case DownloadTask.Status.WAITING:
                        DownloadManager.getInstance().pauseTask(task.getUrl());
                        break;
                }
            });
        }

        public void bind(DownloadTask task) {
            this.task = task;
            urlView.setText(task.getUrl());
            localPathView.setText(task.getLocalPath());
            statusTv.setText(task.getStatusText());
            totalSizeView.setText("Total: " + task.getFileTotalSize());
            downloadedSizeView.setText("Down: " + task.getDownloadedSize());
            startButton.setVisibility(View.VISIBLE);
            switch (task.getStatus()) {
                case DownloadTask.Status.IDLE:
                    startButton.setText("Start");
                    break;
                case DownloadTask.Status.DOWNLOADED:
                case DownloadTask.Status.FAILED:
                    startButton.setVisibility(View.GONE);
                    break;
                case DownloadTask.Status.DOWNLOADING:
                case DownloadTask.Status.WAITING:
                    startButton.setText("Stop");
                    break;
            }
        }
    }
}
