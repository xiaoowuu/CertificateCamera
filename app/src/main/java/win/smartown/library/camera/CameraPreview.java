package win.smartown.library.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Smartown on 2017/8/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = CameraPreview.class.getName();

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x12) {
                if (camera != null) {
                    camera.autoFocus(null);
                    handler.sendEmptyMessageDelayed(0x12, 3000);
                }
            }
        }
    };

    public CameraPreview(Context context) {
        super(context);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Log.d(TAG, "init");
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = CameraUtils.openCamera();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                camera.setDisplayOrientation(90);
            } else {
                camera.setDisplayOrientation(0);
            }
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size bestSize = getBestSize(parameters.getSupportedPreviewSizes());
            if (bestSize != null) {
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                parameters.setPictureSize(bestSize.width, bestSize.height);
            } else {
                parameters.setPreviewSize(1920, 1080);
                parameters.setPictureSize(1920, 1080);
            }
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            handler.sendEmptyMessage(0x12);
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        release();
    }

    @Nullable
    private Camera.Size getBestSize(List<Camera.Size> sizes) {
        for (int i = sizes.size() - 1; i >= 0; i--) {
            Camera.Size size = sizes.get(i);
            if ((float) size.width / (float) size.height == 16.0f / 9.0f) {
                return size;
            }
        }
        return null;
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public boolean switchFlashLight() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                return true;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                return false;
            }
        }
        return false;
    }

    public void takePhoto(Camera.PictureCallback pictureCallback) {
        handler.removeCallbacksAndMessages(null);
        camera.takePicture(null, null, pictureCallback);
    }

}
