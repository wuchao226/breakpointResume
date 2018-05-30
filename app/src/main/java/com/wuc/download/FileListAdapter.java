package com.wuc.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.wuc.download.entities.FileInfo;
import com.wuc.download.services.DownloadService;

import java.util.List;

/**
 * @author: wuchao
 * @date: 2018/5/29 15:06
 * @desciption:
 */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

    private Context mContext;
    private List<FileInfo> mFileList;

    public FileListAdapter(Context context, List<FileInfo> fileList) {
        mContext = context;
        mFileList = fileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FileInfo fileInfo = mFileList.get(position);
        holder.mTxt_fileName.setText(fileInfo.getFileName() + "（进度：" + fileInfo.getFinished() + "）");
        holder.mProgressBar.setMax(100);
        holder.mProgressBar.setProgress((int) fileInfo.getFinished());
        holder.mBtn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });
        holder.mBtn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    /**
     * 更新列表相中的进度条
     *
     * @param id
     * @param progress
     */
    public void updateProgress(int id, long progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        //notifyDataSetChanged();
        notifyItemChanged(mFileList.indexOf(fileInfo), 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mTxt_fileName;
        private ProgressBar mProgressBar;
        private AppCompatButton mBtn_start;
        private AppCompatButton mBtn_stop;

        public ViewHolder(View itemView) {
            super(itemView);
            mTxt_fileName = itemView.findViewById(R.id.txt_fileName);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            mBtn_start = itemView.findViewById(R.id.btn_start);
            mBtn_stop = itemView.findViewById(R.id.btn_stop);
        }
    }
}
