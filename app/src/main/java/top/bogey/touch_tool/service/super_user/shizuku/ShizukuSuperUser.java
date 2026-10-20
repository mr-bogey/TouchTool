package top.bogey.touch_tool.service.super_user.shizuku;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Keep;

import java.util.concurrent.atomic.AtomicReference;

import rikka.shizuku.Shizuku;
import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.service.super_user.CmdResult;
import top.bogey.touch_tool.service.super_user.ISuperUser;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.callback.BooleanResultCallback;

public class ShizukuSuperUser implements ISuperUser {
    private final static int SHIZUKU_CODE = 16777114;
    private final static String SHIZUKU_SUFFIX = "UserService";

    private IShizukuService shizukuService = null;
    private Shizuku.UserServiceArgs ARGS;

    private BooleanResultCallback callback;

    private final ServiceConnection USER_SERVICE_CONNECTION = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            shizukuService = IShizukuService.Stub.asInterface(service);
            if (callback != null) callback.onResult(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            shizukuService = null;
        }
    };

    @Keep
    public ShizukuSuperUser() {
        Context context = MainApplication.getInstance();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            ARGS = new Shizuku.UserServiceArgs(new ComponentName(context.getPackageName(), ShizukuService.class.getName()))
                    .processNameSuffix(SHIZUKU_SUFFIX)
                    .debuggable(!AppUtil.isRelease(context))
                    .version(packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e(e);
        }
    }

    @Override
    public void init(BooleanResultCallback callback) {
        if (isValid()) {
            callback.onResult(true);
        } else {
            if (existShizuku()) requestShizukuPermission(result -> {
                if (result) {
                    this.callback = callback;
                    bindShizukuService();
                } else {
                    callback.onResult(false);
                }
            });
        }
    }

    @Override
    public void exit() {
        if (existShizuku()) {
            try {
                Shizuku.unbindUserService(ARGS, USER_SERVICE_CONNECTION, true);
            } catch (Exception ignored) {
            }
        }
        shizukuService = null;
    }

    @Override
    public boolean isValid() {
        return shizukuService != null;
    }

    @Override
    public CmdResult runCommand(String cmd) {
        if (shizukuService != null) {
            try {
                return shizukuService.runCommand(cmd);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private void bindShizukuService() {
        if (shizukuService == null) {
            if (Shizuku.getVersion() < 12) {
                Shizuku.bindUserService(ARGS, USER_SERVICE_CONNECTION);
            } else {
                int version = Shizuku.peekUserService(ARGS, USER_SERVICE_CONNECTION);
                if (version == -1) {
                    Shizuku.bindUserService(ARGS, USER_SERVICE_CONNECTION);
                }
            }
        }
    }

    private void requestShizukuPermission(BooleanResultCallback callback) {
        if (!isValid()) {
            AtomicReference<Shizuku.OnRequestPermissionResultListener> listener = new AtomicReference<>();
            listener.set((requestCode, grantResult) -> {
                Shizuku.removeRequestPermissionResultListener(listener.get());
                callback.onResult(grantResult == PackageManager.PERMISSION_GRANTED);
            });
            Shizuku.addRequestPermissionResultListener(listener.get());
            Shizuku.requestPermission(SHIZUKU_CODE);
        }
    }

    public static boolean existShizuku() {
        return Shizuku.pingBinder();
    }
}
