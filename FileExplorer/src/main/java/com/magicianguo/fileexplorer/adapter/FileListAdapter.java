package com.magicianguo.fileexplorer.adapter;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magicianguo.fileexplorer.App;
import com.magicianguo.fileexplorer.R;
import com.magicianguo.fileexplorer.bean.BeanFile;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<BeanFile> mList = new ArrayList<>();
    private IItemClickListener mListener;

    private final PackageManager PACKAGE_MANAGER = App.get().getPackageManager();

    public void setListener(IItemClickListener listener) {
        mListener = listener;
    }

    public void updateList(List<BeanFile> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileListHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FileListHolder) {
            FileListHolder fileListHolder = (FileListHolder) holder;
            BeanFile file = mList.get(position);
            fileListHolder.tvName.setText(file.name);
            if (file.isGrantedPath) {
                fileListHolder.tvName.setTextColor(Color.BLUE);
            } else {
                fileListHolder.tvName.setTextColor(Color.BLACK);
            }
            if (file.isDir) {
                fileListHolder.tvIcon.setText(R.string.file_item_icon_txt_dir);
                fileListHolder.tvIcon.setBackgroundResource(R.drawable.bg_icon_dir);
            } else {
                fileListHolder.tvIcon.setText(R.string.file_item_icon_txt_file);
                fileListHolder.tvIcon.setBackgroundResource(R.drawable.bg_icon_file);
            }
            if (file.pathPackageName != null) {
                fileListHolder.ivSmall.setImageDrawable(getAppIconDrawable(file));
            } else {
                fileListHolder.ivSmall.setImageDrawable(null);
            }
            fileListHolder.itemView.setOnClickListener(v -> {
                if (mListener != null && file.isDir) {
                    mListener.onClickDir(file.path);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private Drawable getAppIconDrawable(BeanFile file) {
        try {
            return PACKAGE_MANAGER.getApplicationIcon(file.pathPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class FileListHolder extends RecyclerView.ViewHolder {
        TextView tvIcon = itemView.findViewById(R.id.tv_icon);
        ImageView ivSmall = itemView.findViewById(R.id.iv_small_icon);
        TextView tvName = itemView.findViewById(R.id.tv_name);

        public FileListHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_file_list_item, parent, false));
        }
    }

    public interface IItemClickListener {
        void onClickDir(String path);
    }
}
