package top.bogey.touch_tool.service.capture;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.nio.ByteBuffer;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.utils.AppLog;

public class CaptureService extends Service {
    public static final String DATA = "DATA";
    public static final String FOREGROUND_NOTIFICATION_CHANNEL = "FOREGROUND_NOTIFICATION_CHANNEL";

    private static final String NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL";
    private static final int NOTIFICATION_ID = 10000;
    private static final String STOP_CAPTURE = "STOP_CAPTURE";

    private MediaProjection projection;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;

    private int width = 0, height = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (projection == null) {
            Intent data = intent.getParcelableExtra(DATA);
            if (data != null) {
                MediaProjectionManager manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                projection = manager.getMediaProjection(Activity.RESULT_OK, data);
                if (projection != null) {
                    projection.registerCallback(new MediaProjection.Callback() {
                        @Override
                        public void onStop() {
                            stopService();
                        }

                        @Override
                        public void onCapturedContentResize(int width, int height) {
                            adjustCaptureSize(width, height);
                        }
                    }, null);
                    setVirtualDisplay();
                }
            }
        }
        return new CaptureBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();
        if (projection != null) projection.stop();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra(STOP_CAPTURE, false)) {
                stopService();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return;
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();

        if (projection != null) setVirtualDisplay();
        else stopService();
    }

    private void stopService() {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service != null) {
            service.stopCapture();
        } else {
            stopSelf();
        }
    }

    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel runningChannel = notificationManager.getNotificationChannel(FOREGROUND_NOTIFICATION_CHANNEL);
            if (runningChannel == null) {
                runningChannel = new NotificationChannel(FOREGROUND_NOTIFICATION_CHANNEL, getString(R.string.app_setting_forge_service_channel_title), NotificationManager.IMPORTANCE_DEFAULT);
                runningChannel.setDescription(getString(R.string.app_setting_forge_service_channel_desc));
                notificationManager.createNotificationChannel(runningChannel);
            }

            Notification foregroundNotification = new NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL).build();
            startForeground((int) (Math.random() * Integer.MAX_VALUE), foregroundNotification);

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, getString(R.string.permission_setting_capture_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.permission_setting_capture_channel_tips));
            notificationManager.createNotificationChannel(channel);

            Intent intent = new Intent(this, CaptureService.class);
            intent.putExtra(STOP_CAPTURE, true);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Notification closeNotification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.permission_setting_capture_channel_title))
                    .setContentText(getString(R.string.permission_setting_capture_channel_desc))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            closeNotification.flags |= Notification.FLAG_NO_CLEAR;
            notificationManager.notify(NOTIFICATION_ID, closeNotification);
        }
    }

    private void setVirtualDisplay() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
        virtualDisplay = projection.createVirtualDisplay("CaptureService", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
    }

    private void adjustCaptureSize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (virtualDisplay == null || imageReader == null) return;
        if (width == this.width && height == this.height) return;
        this.width = width;
        this.height = height;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = metrics.densityDpi;
        imageReader.close();
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay.resize(width, height, densityDpi);
        virtualDisplay.setSurface(imageReader.getSurface());
    }

    public class CaptureBinder extends Binder {

        public synchronized Bitmap getScreenShot() {
            Bitmap bitmap = null;
            try (Image image = imageReader.acquireLatestImage()) {
                if (image == null) return null;
                bitmap = rgba8888ImageToBitmap(image);
            } catch (Exception | Error e) {
                AppLog.e(e);
            }
            return bitmap;
        }
    }

    private static Bitmap rgba8888ImageToBitmap(Image image) {
        if (image.getFormat() != PixelFormat.RGBA_8888) {
            throw new IllegalArgumentException("Image format must be RGBA_8888");
        }

        Image.Plane plane = image.getPlanes()[0]; // RGBA 只有 1 个 Plane
        ByteBuffer buffer = plane.getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = plane.getPixelStride(); // 通常为 4（RGBA=4字节）
        int rowStride = plane.getRowStride();     // 每行的字节数（可能包含 padding）

        // 创建 Bitmap（ARGB_8888 格式）
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 检查是否有行填充（rowStride != width * pixelStride）
        if (rowStride == width * pixelStride) {
            // 无填充：直接复制整个缓冲区
            bitmap.copyPixelsFromBuffer(buffer);
        } else {
            // 有填充：逐行复制，跳过填充字节
            byte[] rowData = new byte[rowStride];
            int[] pixels = new int[width];
            buffer.rewind();

            for (int y = 0; y < height; y++) {
                buffer.get(rowData);
                for (int x = 0; x < width; x++) {
                    int r = rowData[x * pixelStride] & 0xFF;     // R
                    int g = rowData[x * pixelStride + 1] & 0xFF; // G
                    int b = rowData[x * pixelStride + 2] & 0xFF; // B
                    int a = rowData[x * pixelStride + 3] & 0xFF; // A
                    pixels[x] = (a << 24) | (r << 16) | (g << 8) | b; // ARGB
                }
                bitmap.setPixels(pixels, 0, width, 0, y, width, 1);
            }
        }

        return bitmap;
    }
}
