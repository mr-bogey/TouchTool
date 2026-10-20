package top.bogey.touch_tool.service.super_user.root;

import androidx.annotation.Keep;

import top.bogey.touch_tool.service.super_user.CmdResult;
import top.bogey.touch_tool.service.super_user.ISuperUser;
import top.bogey.touch_tool.service.super_user.shell.CommandShellSession;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.callback.BooleanResultCallback;

public class RootSuperUser implements ISuperUser {
    private boolean existRoot = false;

    private CommandShellSession commandShellSession;

    @Keep
    public RootSuperUser() {

    }

    @Override
    public void init(BooleanResultCallback callback) {
        openRootSession();
        callback.onResult(existRoot);
    }

    @Override
    public void exit() {
        if (commandShellSession != null) {
            commandShellSession.close();
            commandShellSession = null;
        }

        existRoot = false;
    }

    @Override
    public boolean isValid() {
        return existRoot && commandShellSession != null;
    }

    @Override
    public CmdResult runCommand(String cmd) {
        if (!existRoot || commandShellSession == null) return null;
        return commandShellSession.runCommand(cmd);
    }

    public void openRootSession() {
        if (existRoot) return;
        try {
            commandShellSession = new CommandShellSession("su");
            CmdResult result = commandShellSession.runCommand("echo root");
            existRoot = result.getResult();
            if (!existRoot) {
                exit();
            }
        } catch (Exception e) {
            AppLog.e(e);
            exit();
        }
    }
}
