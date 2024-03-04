package com.magicianguo.fileexplorer.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.magicianguo.fileexplorer.R;
import com.magicianguo.fileexplorer.adapter.FileListAdapter;
import com.magicianguo.fileexplorer.bean.BeanFile;
import com.magicianguo.fileexplorer.constant.BundleKey;
import com.magicianguo.fileexplorer.util.FileTools;

import java.util.ArrayList;

public class FileListFragment extends Fragment {
    private final FileListAdapter mAdapter = new FileListAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rvList = view.findViewById(R.id.rv_files);
        mAdapter.setListener(new FileListAdapter.IItemClickListener() {
            @Override
            public void onClickDir(String path) {
                FileTools.notifyClickDir(path);
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvList.setAdapter(mAdapter);
        Bundle arguments = getArguments();
        if (arguments != null) {
            ArrayList<BeanFile> list = arguments.getParcelableArrayList(BundleKey.FILE_LIST);
            mAdapter.updateList(list);
        }

    }
}
