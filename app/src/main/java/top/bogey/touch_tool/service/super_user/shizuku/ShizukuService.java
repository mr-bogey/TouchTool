package top.bogey.touch_tool.service.super_user.shizuku;

import android.content.Context;

import androidx.annotation.Keep;

import top.bogey.touch_tool.service.super_user.CmdResult;
import top.bogey.touch_tool.service.super_user.shell.CommandShellSession;
import top.bogey.touch_tool.utils.AppLog;

public class ShizukuService extends IShizukuService.Stub {
    private CommandShellSession commandShellSession;

    public ShizukuService() {
        try {
            commandShellSession = new CommandShellSession("sh");
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    @Keep
    public ShizukuService(Context context) {
        this();
    }

    @Override
    public void destory() {
        if (commandShellSession != null) {
            commandShellSession.close();
            commandShellSession = null;
        }

        System.exit(0);
    }

    @Override
    public CmdResult runCommand(String cmd) {
        if (commandShellSession == null) return null;
        return commandShellSession.runCommand(cmd);
    }
}