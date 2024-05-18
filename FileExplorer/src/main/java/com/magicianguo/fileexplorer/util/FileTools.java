package com.magicianguo.fileexplorer.util;

import android.app.Activity;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.magicianguo.fileexplorer.App;
import com.magicianguo.fileexplorer.R;
import com.magicianguo.fileexplorer.bean.BeanFile;
import com.magicianguo.fileexplorer.constant.PathType;
import com.magicianguo.fileexplorer.constant.RequestCode;
import com.magicianguo.fileexplorer.observer.IFileItemClickObserver;
import com.magicianguo.fileexplorer.userservice.IFileExplorerService;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kotlin.collections.CollectionsKt;

public class FileTools {
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
    public static int specialPathReadType = PathType.DOCUMENT;

    private static final PackageManager PACKAGE_MANAGER = App.get().getPackageManager();

    private static final Comparator<BeanFile> COMPARATOR = new Comparator<BeanFile>() {
        @Override
        public int compare(BeanFile o1, BeanFile o2) {
            String name1 = o1.name;
            String name2 = o2.name;
            int compareCount = Math.max(name1.length(), name2.length());
            for (int i = 0; i < compareCount; i++) {
                int code1 = getCharCode(name1, i);
                int code2 = getCharCode(name2, i);
                if (code1 != code2) {
                    return code1 - code2;
                }
            }
            return 0;
        }

        private int getCharCode(String str, int index) {
            if (index >= str.length()) {
                return -1;
            }
            char c = str.charAt(index);
            if (Character.isLetter(c)) {
                if (Character.isLowerCase(c)) {
                    return Character.toUpperCase(c);
                } else {
                    return c;
                }
            } else {
                return -1;
            }
        }
    };

    public static IFileExplorerService iFileExplorerService;

    public static List<BeanFile> getSortedFileList(String path) {
        List<BeanFile> fileList = getFileList(path);
        CollectionsKt.sortWith(fileList, COMPARATOR);
        return fileList;
    }

    private static List<BeanFile> getFileList(String path) {
        int type = getPathType(path);
        if (type == PathType.SHIZUKU) {
            return getFileListByShizuku(path);
        }
        if (type == PathType.DOCUMENT) {
            return getFileListByDocument(path);
        }
        if (type == PathType.PACKAGE_NAME) {
            return getPackageNameFileList(path);
        }
        return getFileListByFile(path);
    }

    private static List<BeanFile> getFileListByFile(String path) {
        boolean isPkgNamePath = isDataPath(path) || isObbPath(path);
        List<BeanFile> list = new ArrayList<>();
        File dir = new File(path);
        File[] files;
        if ((files = dir.listFiles()) != null) {
            for (File file : files) {
                list.add(new BeanFile(file.getName(), file.getPath(), file.isDirectory(), false, isPkgNamePath ? file.getName() : null));
            }
        }
        return list;
    }

    private static List<BeanFile> getFileListByDocument(String path) {
        Uri pathUri = pathToUri(path);
        Log.d("TAG", "getFileListByDocument: pathUri = "+pathUri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(App.get(), pathUri);
        List<BeanFile> list = new ArrayList<>();
        if (documentFile != null) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            for (DocumentFile df : documentFiles) {
                String fName = df.getName();
                String fPath = path + "/" + fName;
                list.add(new BeanFile(fName, fPath, df.isDirectory(), false, getPathPackageName(fName)));
            }
        }
        return list;
    }

    private static List<BeanFile> getFileListByShizuku(String path) {
        try {
            return iFileExplorerService.listFiles(path);
        } catch (NullPointerException | RemoteException e) {
            ToastUtils.longCall(R.string.toast_shizuku_load_file_failed);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<BeanFile> getPackageNameFileList(String path) {
        List<BeanFile> list = new ArrayList<>();
        List<PackageInfo> installedPackages = PACKAGE_MANAGER.getInstalledPackages(0);
        for (PackageInfo packageInfo : installedPackages) {
            String packageName = packageInfo.packageName;
            String dirPath = path + "/" + packageName;
            File dir = new File(dirPath);
            if (dir.exists()) {
                list.add(new BeanFile(packageName, dirPath, true, hasUriPermission(dirPath) || isFromMyPackageNamePath(dirPath), packageName));
            }
        }
        return list;
    }

    public static boolean shouldRequestUriPermission(String path) {
        if (getPathType(path) != PathType.DOCUMENT) {
            return false;
        }
        return !hasUriPermission(path);
    }

    @PathType.PathType1
    private static int getPathType(String path) {
        if (isFromMyPackageNamePath(path)) {
            return PathType.FILE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isDataPath(path) || isObbPath(path) || isUnderDataPath(path) || isUnderObbPath(path)) {
                if (specialPathReadType == PathType.SHIZUKU) {
                    return PathType.SHIZUKU;
                } else {
                    return PathType.DOCUMENT;
                }
            } else {
                return PathType.FILE;
            }
        } else {
            return PathType.FILE;
        }
    }

    private static boolean hasUriPermission(String path) {
        List<UriPermission> uriPermissions = App.get().getContentResolver().getPersistedUriPermissions();
        Log.d("TAG", "hasUriPermission: uriPermissions = " + uriPermissions);
        String uriPath = pathToUri(path).getPath();
        Log.d("TAG", "hasUriPermission: uriPath = "+uriPath);
        for (UriPermission uriPermission : uriPermissions) {
            String itemPath = uriPermission.getUri().getPath();
            Log.d("TAG", "hasUriPermission: itemPath = " + itemPath);
            if (uriPath != null && itemPath != null && (uriPath + "/").contains(itemPath + "/")) {
                return true;
            }
        }
        return false;
    }

    public static void requestUriPermission(Activity activity, String path) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri treeUri = pathToUri(path);
            DocumentFile df = DocumentFile.fromTreeUri(activity, treeUri);
            if (df != null) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, df.getUri());
            }
        }
        activity.startActivityForResult(intent, RequestCode.DOCUMENT);
    }

    private static Uri pathToUri(String path) {
        String halfPath = path.replace(ROOT_PATH + "/", "");
        String[] segments = halfPath.split("/");
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("content")
                .authority("com.android.externalstorage.documents")
                .appendPath("tree");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uriBuilder.appendPath("primary:A\u200Bndroid/" + segments[1]);
        } else {
            uriBuilder.appendPath("primary:Android/" + segments[1]);
        }
        uriBuilder.appendPath("document");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uriBuilder.appendPath("primary:A\u200Bndroid/" + halfPath.replace("Android/", ""));
        } else {
            uriBuilder.appendPath("primary:" + halfPath);
        }
        return uriBuilder.build();
    }

    private static boolean isFromMyPackageNamePath(String path) {
        return (path + "/").contains(ROOT_PATH + "/Android/data/"+ App.get().getPackageName()+"/");
    }

    private static boolean isDataPath(String path) {
        return (ROOT_PATH + "/Android/data").equals(path);
    }

    private static boolean isObbPath(String path) {
        return (ROOT_PATH + "/Android/obb").equals(path);
    }

    private static boolean isUnderDataPath(String path) {
        return path.contains(ROOT_PATH + "/Android/data/");
    }

    private static boolean isUnderObbPath(String path) {
        return path.contains(ROOT_PATH + "/Android/obb/");
    }

    /**
     * 如果字符串是应用包名，返回字符串，反之返回null
     */
    public static String getPathPackageName(String name) {
        try {
            PACKAGE_MANAGER.getPackageInfo(name, 0);
            return name;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return null;
    }

    private static List<IFileItemClickObserver> sFileItemClickObservers = new ArrayList<>();

    public static void addFileItemClickObserver(IFileItemClickObserver observer) {
        sFileItemClickObservers.add(observer);
    }

    public static void removeFileItemClickObserver(IFileItemClickObserver observer) {
        sFileItemClickObservers.remove(observer);
    }

    public static void notifyClickDir(String path) {
        for (IFileItemClickObserver observer : sFileItemClickObservers) {
            observer.onClickDir(path);
        }
    }

}
