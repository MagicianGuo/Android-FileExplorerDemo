# 文件管理

这是一个简陋的，用于浏览安卓设备/sdcard下文件的应用程序。支持
Android 5.0（API 21）~ Android 14（API 34）。

## 不同安卓版本存储权限差异

### 1、Android 6.0 之前

应用只需要在 AndroidManifest.xml 下声明以下权限即可自动获取存储权限：

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 2、Android 6.0 起

从Android 6.0开始，除了以上操作以外，还需要在代码中动态申请权限。

```java
// 检查是否有存储权限
boolean granted = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
    PackageManager.PERMISSION_GRANTED;
```

```java
// 在activity中请求存储权限
requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
```

### 3、Android 10

Android 10 开始引入了沙盒机制，应用在 sdcard 中默认只能读写私有目录（即/sdcard/Android/data/[应用包名]/），其他目录即便执行前面的操作也无法读写。除非在 AndroidManifest.xml 下声明以下属性：

```xml
<application
        ...
        android:requestLegacyExternalStorage="true">
```

这样的话就会暂时停用沙盒机制，正常读写/sdcard下文件。

### 4、Android 11

Android 11开始，且应用的目标版本在30及以上，以上的操作也无法再读写sdcard目录。需要声明以下权限：

```xml
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

再动态申请权限：

```java
// 检查是否有存储权限
boolean granted = Environment.isExternalStorageManager();
```

```java
// 在activity中请求存储权限
Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    .setData(Uri.parse("package:"+getPackageName()));
startActivityForResult(intent, 0);
```

执行以上操作后，sdcard已能够正常读写。

但是，有2个特殊的目录仍然无法读写：

/sdcard/Android/data 和 /sdcard/Android/obb 。

这两个路径需要安卓自带的 DocumentsUI 授权才能访问。

首先，最重要的一点，添加 documentfile 依赖（SDK自带的那个版本有问题）：

```groovy
implementation "androidx.documentfile:documentfile:1.0.1"
```

Activity请求授权：

```java
Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    // 请求Android/data目录的权限，Android/obb目录则把data替换成obb即可。
    Uri treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata");
    DocumentFile df = DocumentFile.fromTreeUri(this, treeUri);
    if (df != null) {
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, df.getUri());
    }
}
startActivityForResult(intent, 1);
```

还需要在回调中保存权限：

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Uri uri;
    if (data != null && (uri = data.getData()) != null) {
        // 授权成功
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    } else {
        // 授权失败
    }
}
```

在请求授权时，会跳转到以下界面。点击下方按钮授权即可。

<img src="https://img-blog.csdnimg.cn/direct/79979094c5e346cbb2e1272a96c7a184.png" style="zoom:33%;" />

<img src="https://img-blog.csdnimg.cn/direct/2744b27269df4f468c2cec7be26d8e29.png" style="zoom:33%;" />

然后，使用接口获取文件列表：

```java
// Android 11~12转换路径。例如：/sdcard/Android/data，转换成：
// Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata")

// 路径 /sdcard/Android/data/com.xxx.yyy，转换成：
// Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2Fcom.xxx.yyy")
// 以此类推。
Uri pathUri = pathToUri(path);
DocumentFile documentFile = DocumentFile.fromTreeUri(context, pathUri);
if (documentFile != null) {
    DocumentFile[] documentFiles = documentFile.listFiles();
    for (DocumentFile df : documentFiles) {
        // 文件名
        String fName = df.getName();
        // 路径
        String fPath = path + "/" + fName;
    }
}
```

### 5、Android 13

Android 13 开始，上面提到的授权 Android/data、Android/obb目录的方法失效了。请求授权会出现如下界面：

<img src="https://img-blog.csdnimg.cn/direct/3d0741ebde9a4728a9dc240daca14cc1.png" style="zoom:33%;" />

点击下方按钮会提示：

<img src="https://img-blog.csdnimg.cn/direct/41c9914aa1474f9baa6e2650ff5b9033.png" style="zoom:33%;" />

说明安卓13无法再直接授权 Android/data 和 Android/obb 这两个目录了。但也并非无计可施。我们可以授权他们的子目录。

我们知道，这个目录下的文件夹名称一般都是应用的包名。我们可以直接用应用包名的路径来请求授权。

**这里要注意，Android 13和Android 11~12的路径转换规则不一样。**

```java
// Android 13转换路径。例如：/sdcard/Android/data/com.xxx.yyy，转换成：
// Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.xxx.yyy/document/primary%3AAndroid%2Fdata%2Fcom.xxx.yyy")

// 路径 /sdcard/Android/data/com.xxx.yyy/files，转换成：
// Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.xxx.yyy/document/primary%3AAndroid%2Fdata%2Fcom.xxx.yyy%2Ffiles")
// 以此类推。
```

请求授权时，出现如下界面。照常授权即可。

<img src="https://img-blog.csdnimg.cn/direct/f537a37560234a5fa7f178896dd8c287.png" style="zoom:33%;" />

### 6、Android 14

Android 14对于data、obb目录的授权进一步收紧。在Android 14的后期版本和Android 15预览版中，以上方法已失效（传入uri请求授权只会跳转到sdcard根目录）。这种情况下，想要访问data和obb目录就需要使用Shizuku了。（目前MT、FV就是用这种方法访问的）

Shizuku的用法可以查阅相关教程，这里不多赘述。请先将Shizuku启动，便于后续使用。

首先，添加Shizuku的依赖：

```groovy
def shizuku_version = "13.1.5"
implementation "dev.rikka.shizuku:api:$shizuku_version"
// Add this line if you want to support Shizuku
implementation "dev.rikka.shizuku:provider:$shizuku_version"
```

AndroidManifest.xml添加以下内容：

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-sdk tools:overrideLibrary="rikka.shizuku.api, rikka.shizuku.provider, rikka.shizuku.shared, rikka.shizuku.aidl" />
    <uses-permission android:name="moe.shizuku.manager.permission.API_V23" />

    <application>
        <provider
            android:name="rikka.shizuku.ShizukuProvider"
            android:authorities="${applicationId}.shizuku"
            android:enabled="true"
            android:exported="true"
            android:multiprocess="false"
            android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
        <meta-data
            android:name="moe.shizuku.client.V3_SUPPORT"
            android:value="true" />
    </application>
</manifest>
```

判断Shizuku是否安装：

```java
private static boolean isShizukuInstalled() {
    try {
        context.getPackageManager().getPackageInfo("moe.shizuku.privileged.api", 0);
        return true;
    } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
    }
    return false;
}
```

判断Shizuku是否可用：

```java
boolean available = Shizuku.pingBinder();
```

检查Shizuku是否授权：

```java
boolean granted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
```

请求Shizuku权限：

```java
Shizuku.requestPermission(0);
```

监听授权结果：

```java
Shizuku.addRequestPermissionResultListener(listener);
Shizuku.removeRequestPermissionResultListener(listener);
```

授权后，自己定义一个aidl文件：

**IFileExplorerService.aidl**

```java
package com.magicianguo.fileexplorer.userservice;

import com.magicianguo.fileexplorer.bean.BeanFile;

interface IFileExplorerService {
    List<BeanFile> listFiles(String path);
}
```

**BeanFile.java**

```java
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
}
```

**IFileExplorerService实现类：**

```java
public class FileExplorerService extends IFileExplorerService.Stub {

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
```

然后使用Shizuku绑定UserService：

```java
private static final Shizuku.UserServiceArgs USER_SERVICE_ARGS = new Shizuku.UserServiceArgs(
    new ComponentName(packageName, FileExplorerService.class.getName())
).daemon(false).debuggable(BuildConfig.DEBUG).processNameSuffix("file_explorer_service").version(1);

public static IFileExplorerService iFileExplorerService;

private static final ServiceConnection SERVICE_CONNECTION = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        iFileExplorerService = IFileExplorerService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        iFileExplorerService = null;
    }
};

// 绑定服务
public static void bindService() {
    Shizuku.bindUserService(USER_SERVICE_ARGS, SERVICE_CONNECTION);
}
```

绑定之后，调用 aidl 里面的方法来管理文件即可。

**2024/05/18 更新**

目前发现了新的Document授权方式，能够在Android 13、14上直接授权Android/data(obb)目录。

例1：Android/data目录请求授权，可使用如下Uri：

```java
    Uri.Builder uriBuilder = new Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath("primary:A\u200Bndroid/data")
        .appendPath("document")
        .appendPath("primary:A\u200Bndroid/data");
// 相当于 content://com.android.externalstorage.documents/tree/primary%3AA%E2%80%8Bndroid%2Fdata/document/primary%3AA%E2%80%8Bndroid%2Fdata
```

例2：Android/data/com.xxx.yyy目录请求授权，可使用如下Uri：

```java
    Uri.Builder uriBuilder = new Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath("primary:A\u200Bndroid/data")
        .appendPath("document")
        .appendPath("primary:A\u200Bndroid/data/com.xxx.yyy");
// 相当于 content://com.android.externalstorage.documents/tree/primary%3AA%E2%80%8Bndroid%2Fdata/document/primary%3AA%E2%80%8Bndroid%2Fdata%2Fcom.xxx.yyy
```