package com.magicianguo.fileexplorer.userservice;

import com.magicianguo.fileexplorer.bean.BeanFile;

interface IFileExplorerService {
    List<BeanFile> listFiles(String path);
}