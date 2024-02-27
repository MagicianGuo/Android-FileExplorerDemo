package com.magicianguo.fileexplorer.constant;

import androidx.annotation.IntDef;

public interface PathType {
    /**
     * 通过File接口，访问一般路径
     */
    int FILE = 0;
    /**
     * 通过Document API 访问特殊路径
     */
    int DOCUMENT = 1;
    /**
     * 安卓13及以上，直接用包名展示data、obb下的目录（因为data、obb不能直接授权了，只能对子目录授权）
     */
    int PACKAGE_NAME = 2;
    /**
     * 通过Shizuku授权访问特殊路径
     */
    int SHIZUKU = 3;

    @IntDef({ FILE, DOCUMENT, PACKAGE_NAME, SHIZUKU })
    @interface PathType1 {
    }
}
