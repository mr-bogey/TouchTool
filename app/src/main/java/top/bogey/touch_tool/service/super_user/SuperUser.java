package top.bogey.touch_tool.service.super_user;

import androidx.annotation.Keep;

import java.lang.reflect.Constructor;

import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.service.super_user.root.RootSuperUser;
import top.bogey.touch_tool.service.super_user.shizuku.ShizukuSuperUser;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.callback.BooleanResultCallback;

public class SuperUser implements ISuperUser {
    private static ISuperUser instance;

    static ISuperUser getInstance(Class<? extends ISuperUser> clazz) {
        synchronized (SuperUser.class) {
            if (instance != null) {
                if (!clazz.equals(instance.getClass())) {
                    instance.exit();
                    instance = null;
                }
            }

            if (instance == null) {
                try {
                    Constructor<? extends ISuperUser> constructor = clazz.getDeclaredConstructor();
                    instance = constructor.newInstance();
                } catch (Exception e) {
                    AppLog.e(e);
                }
            }
        }
        return instance;
    }

    public static ISuperUser getInstance() {
        if (SettingSaver.PERMISSION_SUPER_USER.get() == 1) {
            return getInstance(ShizukuSuperUser.class);
        } else if (SettingSaver.PERMISSION_SUPER_USER.get() == 2) {
            return getInstance(RootSuperUser.class);
        } else {
            return getInstance(SuperUser.class);
        }
    }

    @Keep
    private SuperUser() {

    }

    @Override
    public void init(BooleanResultCallback callback) {
        callback.onResult(false);
    }


    @Override
    public void exit() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public CmdResult runCommand(String cmd) {
        return null;
    }
}
