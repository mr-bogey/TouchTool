package top.bogey.touch_tool.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.accessibility.selecttospeak.SelectToSpeakService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.callback.ActivityResultCallback;

public class BaseActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native");
    }

    private ActivityResultLauncher<Intent> intentLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private ActivityResultLauncher<String> createDocumentLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private ActivityResultCallback resultCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLog.d("BaseActivity", "onCreate: " + this.getClass().getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }

        intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (resultCallback == null) return;
            resultCallback.onResult(result.getResultCode(), result.getData());
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (resultCallback == null) return;
            if (result) resultCallback.onResult(RESULT_OK, null);
            else resultCallback.onResult(RESULT_CANCELED, null);
        });

        openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            if (resultCallback == null) return;
            if (result != null) {
                Intent intent = new Intent();
                intent.setData(result);
                resultCallback.onResult(RESULT_OK, intent);
            } else {
                resultCallback.onResult(RESULT_CANCELED, null);
            }
        });

        createDocumentLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/*"), result -> {
            if (resultCallback == null) return;
            if (result != null) {
                Intent intent = new Intent();
                intent.setData(result);
                resultCallback.onResult(RESULT_OK, intent);
            } else {
                resultCallback.onResult(RESULT_CANCELED, null);
            }
        });

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), result -> {
            if (resultCallback == null) return;
            if (result != null) {
                Intent intent = new Intent();
                intent.setData(result);
                resultCallback.onResult(RESULT_OK, intent);
            } else {
                resultCallback.onResult(RESULT_CANCELED, null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppLog.d("BaseActivity", "onStart: " + this.getClass().getName());
        restartAccessibilityServiceBySecurePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d("BaseActivity", "onResume: " + this.getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppLog.d("BaseActivity", "onPause: " + this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppLog.d("BaseActivity", "onStop: " + this.getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLog.d("BaseActivity", "onDestroy: " + this.getClass().getName());
        intentLauncher = null;
        permissionLauncher = null;
        openDocumentLauncher = null;
        createDocumentLauncher = null;
        pickMediaLauncher = null;
        resultCallback = null;
    }

    public void launchCapture(ActivityResultCallback callback) {
        if (intentLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        resultCallback = callback;
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        intentLauncher.launch(manager.createScreenCaptureIntent());
    }

    public void launchNotification(ActivityResultCallback callback) {
        if (permissionLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String permission = Manifest.permission.POST_NOTIFICATIONS;
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                if (callback != null) callback.onResult(Activity.RESULT_OK, null);
            } else if (shouldShowRequestPermissionRationale(permission)) {
                AppUtil.showDialog(this, R.string.setting_need_notification_desc, result -> {
                    if (result) {
                        resultCallback = callback;
                        permissionLauncher.launch(permission);
                    } else {
                        if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
                    }
                });
            } else {
                resultCallback = callback;
                permissionLauncher.launch(permission);
            }
        } else {
            if (callback != null) callback.onResult(Activity.RESULT_OK, null);
        }
    }

    public void launcherOpenDocument(ActivityResultCallback callback, String mimeType) {
        if (openDocumentLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        resultCallback = callback;
        try {
            openDocumentLauncher.launch(new String[]{mimeType});
        } catch (ActivityNotFoundException e) {
            AppLog.e(e);
        }
    }

    public void launcherCreateDocument(String fileName, ActivityResultCallback callback) {
        if (createDocumentLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        resultCallback = callback;
        try {
            createDocumentLauncher.launch(fileName);
        } catch (ActivityNotFoundException e) {
            AppLog.e(e);
        }
    }

    public void launcherPickMedia(ActivityResultCallback callback, ActivityResultContracts.PickVisualMedia.VisualMediaType type) {
        if (pickMediaLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        resultCallback = callback;

        try {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(type)
                    .build());
        } catch (ActivityNotFoundException e) {
            AppLog.e(e);
        }
    }

    public void launcherRingtone(String path, ActivityResultCallback callback) {
        if (intentLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }
        resultCallback = callback;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        if (path != null && !path.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(path));
        }
        try {
            intentLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            AppLog.e(e);
        }
    }

    public void launcherBluetooth(ActivityResultCallback callback) {
        if (permissionLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }

        String permission;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            permission = Manifest.permission.BLUETOOTH;
        } else {
            permission = Manifest.permission.BLUETOOTH_CONNECT;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            if (callback != null) callback.onResult(Activity.RESULT_OK, null);
        } else if (shouldShowRequestPermissionRationale(permission)) {
            AppUtil.showDialog(this, R.string.permission_setting_bluetooth_desc, result -> {
                if (result) {
                    resultCallback = callback;
                    permissionLauncher.launch(permission);
                } else {
                    if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
                }
            });
        } else {
            resultCallback = callback;
            permissionLauncher.launch(permission);
        }
    }

    public void launcherFineLocation(ActivityResultCallback callback) {
        if (permissionLauncher == null) {
            if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
            return;
        }

        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            if (callback != null) callback.onResult(Activity.RESULT_OK, null);
        } else if (shouldShowRequestPermissionRationale(permission)) {
            AppUtil.showDialog(this, R.string.permission_setting_location_desc, result -> {
                if (result) {
                    resultCallback = callback;
                    permissionLauncher.launch(permission);
                } else {
                    if (callback != null) callback.onResult(Activity.RESULT_CANCELED, null);
                }
            });
        } else {
            resultCallback = callback;
            permissionLauncher.launch(permission);
        }
    }

    public void restartAccessibilityServiceBySecurePermission() {
        // 界面打开时尝试恢复无障碍服务
        // 如果应用服务设置关闭了，就啥都不管
        if (!SettingSaver.APP_SERVICE.get()) return;

        // 是否有权限去重启无障碍服务
        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) return;

        // 看一下服务有没有开启
        if (AppUtil.isAccessibilityServiceEnabled(this)) return;

        Toast.makeText(this, R.string.app_setting_reload_tips, Toast.LENGTH_SHORT).show();
        // 没有开启去开启
        String serviceName = String.format("%s/%s", getPackageName(), SelectToSpeakService.class.getName());
        String enabledService = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledService == null) enabledService = "";
        Set<String> services = new HashSet<>(Arrays.asList(enabledService.split(":")));
        services.remove("");
        services.add(serviceName);
        enabledService = TextUtils.join(":", services);
        Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledService);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
    }

    public boolean stopAccessibilityServiceBySecurePermission() {
        // 是否有权限去重启无障碍服务
        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) return false;

        // 开启去关闭
        String serviceName = String.format("%s/%s", getPackageName(), SelectToSpeakService.class.getName());
        String enabledService = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledService == null) enabledService = "";
        Set<String> services = new HashSet<>(Arrays.asList(enabledService.split(":")));
        services.remove("");
        services.remove(serviceName);
        enabledService = TextUtils.join(":", services);
        Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledService);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
        return true;
    }
}
