package com.magicianguo.fileexplorer.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BeanFile implements Parcelable {
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

    protected BeanFile(Parcel in) {
        name = in.readString();
        path = in.readString();
        isDir = in.readByte() != 0;
        isGrantedPath = in.readByte() != 0;
        pathPackageName = in.readString();
    }

    public static final Creator<BeanFile> CREATOR = new Creator<BeanFile>() {
        @Override
        public BeanFile createFromParcel(Parcel in) {
            return new BeanFile(in);
        }

        @Override
        public BeanFile[] newArray(int size) {
            return new BeanFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeByte((byte) (isDir ? 1 : 0));
        dest.writeByte((byte) (isGrantedPath ? 1 : 0));
        dest.writeString(pathPackageName);
    }

    @NonNull
    @Override
    public String toString() {
        return "BeanFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", isDir=" + isDir +
                ", isGrantedPath=" + isGrantedPath +
                ", pathPackageName='" + pathPackageName + '\'' +
                '}';
    }
}
