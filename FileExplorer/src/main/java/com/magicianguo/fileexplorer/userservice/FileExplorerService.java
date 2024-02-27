package com.magicianguo.fileexplorer.userservice;

import android.os.RemoteException;
import com.magicianguo.fileexplorer.bean.BeanFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileExplorerService extends IFileExplorerService.Stub {
    private static final String TAG = "FileExplorerService";

    @Override
    public List<BeanFile> listFiles(String path) throws RemoteException {
        List<BeanFile> list = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File f : files) {
                list.add(new BeanFile(f.getName(), f.getPath(), f.isDirectory(), false, f.getName()));
            }
        }
        return list;
    }
}
