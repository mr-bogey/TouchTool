package top.bogey.touch_tool.service.super_user.shell;

import top.bogey.touch_tool.service.super_user.CmdResult;
import top.bogey.touch_tool.utils.AppLog;

public class CommandShellSession extends ShellSession {
    private final static String EXIT_MARKER = "EXIT_MARKER";

    public CommandShellSession(String cmd) throws Exception {
        super(cmd);
    }

    public synchronized CmdResult runCommand(String cmd) {
        try {
            writeLine(cmd);
            writeLine("echo");
            writeLine("echo " + EXIT_MARKER + ":$?");

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = outputReader.readLine()) != null) {
                AppLog.d("Shell", "runCommand readLine: " + line);
                if (line.startsWith(EXIT_MARKER)) {
                    int exitCode = Integer.parseInt(line.substring(EXIT_MARKER.length() + 1));
                    return new CmdResult(exitCode == 0, output.toString().trim());
                }
                output.append(line).append("\n");
            }

            return new CmdResult(false, "");

        } catch (Exception e) {
            AppLog.e(e);
            return new CmdResult(false, e.getMessage());
        }
    }
}
