package com.magicianguo.fileexplorer.bean;

public class BeanFile {
    public BeanFile(String name, String path, boolean isDir, boolean isGrantedPath, String pathPackageName) {
        this.name = name;
        this.path = path;
        this.isDir = isDir;
        this.isGrantedPath = isGrantedPath;
        this.pathPackageName = pathPackageName;
    }

    /**
     * 文件名
     */
    public String name;
    /**
     * 文件路径
     */
    public String path;
    /**
     * 是否文件夹
     */
    public boolean isDir;
    /**
     * 是否被Document授权的路径
     */
    public boolean isGrantedPath;
    /**
     * 如果文件夹名称是应用包名，则将包名保存到该字段
     */
    public String pathPackageName;
}
