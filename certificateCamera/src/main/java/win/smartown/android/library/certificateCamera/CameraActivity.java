package win.smartown.android.library.certificateCamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Smartown on 2017/8/16.
 */
public class CameraActivity extends Activity implements View.OnClickListener {

    private CameraPreview cameraPreview;
    private View containerView;
    private ImageView cropView;
    private ImageView flashImageView;

    /**
     * 1.身份证正面；2.身份证反面；3.营业执照（竖版）；4.营业执照（横版）
     */
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", 0);
        if (type == 3) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_camera);
        cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
//        //获取屏幕最小边，设置为cameraPreview的宽
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
//        //根据cameraPreview的宽，计算出cameraPreview的长，长宽比为标准的16:9
        float maxSize = screenMinSize / 9.0f * 16.0f;
        RelativeLayout.LayoutParams layoutParams;
        if (type == 3) {
            layoutParams = new RelativeLayout.LayoutParams((int) screenMinSize, (int) maxSize);
        } else {
            layoutParams = new RelativeLayout.LayoutParams((int) maxSize, (int) screenMinSize);
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraPreview.setLayoutParams(layoutParams);

        containerView = findViewById(R.id.camera_crop_container);
        cropView = (ImageView) findViewById(R.id.camera_crop);
        if (type == 3) {
            float width = (int) (screenMinSize * 0.8);
            float height = (int) (width * 43.0f / 30.0f);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            containerView.setLayoutParams(containerParams);
            cropView.setLayoutParams(cropParams);
        } else if (type == 4) {
            float height = (int) (screenMinSize * 0.8);
            float width = (int) (height * 43.0f / 30.0f);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            containerView.setLayoutParams(containerParams);
            cropView.setLayoutParams(cropParams);
        } else {
            float height = (int) (screenMinSize * 0.75);
            float width = (int) (height * 75.0f / 47.0f);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            containerView.setLayoutParams(containerParams);
            cropView.setLayoutParams(cropParams);
        }
        switch (type) {
            case 1:
                cropView.setImageResource(R.mipmap.camera_idcard_front);
                break;
            case 2:
                cropView.setImageResource(R.mipmap.camera_idcard_back);
                break;
            case 3:
                cropView.setImageResource(R.mipmap.camera_company);
                break;
            case 4:
                cropView.setImageResource(R.mipmap.camera_company_landscape);
                break;
        }

        flashImageView = (ImageView) findViewById(R.id.camera_flash);
        cameraPreview.setOnClickListener(this);
        findViewById(R.id.camera_close).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);
        flashImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_surface) {
            cameraPreview.focus();
        } else if (id == R.id.camera_close) {
            finish();
        } else if (id == R.id.camera_take) {
            takePhoto();
        } else if (id == R.id.camera_flash) {
            boolean isFlashOn = cameraPreview.switchFlashLight();
            flashImageView.setImageResource(isFlashOn ? R.mipmap.camera_flash_on : R.mipmap.camera_flash_off);
        }
    }

    private void takePhoto() {
        cameraPreview.takePhoto(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File originalFile = getOriginalFile();
                            FileOutputStream originalFileOutputStream = new FileOutputStream(originalFile);
                            originalFileOutputStream.write(data);
                            originalFileOutputStream.close();

                            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getPath());

                            float left, top, right, bottom;
                            if (type == 3) {
                                left = (float) cropView.getLeft() / (float) cameraPreview.getWidth();
                                top = ((float) containerView.getTop() - (float) cameraPreview.getTop()) / (float) cameraPreview.getHeight();
                                right = (float) cropView.getRight() / (float) cameraPreview.getWidth();
                                bottom = (float) containerView.getBottom() / (float) cameraPreview.getHeight();
                            } else {
                                left = ((float) containerView.getLeft() - (float) cameraPreview.getLeft()) / (float) cameraPreview.getWidth();
                                top = (float) cropView.getTop() / (float) cameraPreview.getHeight();
                                right = (float) containerView.getRight() / (float) cameraPreview.getWidth();
                                bottom = (float) cropView.getBottom() / (float) cameraPreview.getHeight();
                            }
                            Bitmap cropBitmap = Bitmap.createBitmap(bitmap,
                                    (int) (left * (float) bitmap.getWidth()),
                                    (int) (top * (float) bitmap.getHeight()),
                                    (int) ((right - left) * (float) bitmap.getWidth()),
                                    (int) ((bottom - top) * (float) bitmap.getHeight()));

                            final File cropFile = getCropFile();
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cropFile));
                            cropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            bos.flush();
                            bos.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.putExtra("result", cropFile.getPath());
                                    setResult(0x14, intent);
                                    finish();
                                }
                            });
                            return;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO: 2017/8/17 failed
                            }
                        });
                    }
                }).start();

            }
        });
    }

    private File getOriginalFile() {
        switch (type) {
            case 1:
                return new File(getExternalCacheDir(), "idCardFront.jpg");
            case 2:
                return new File(getExternalCacheDir(), "idCardBack.jpg");
            case 3:
                return new File(getExternalCacheDir(), "companyInfo.jpg");
        }
        return new File(getExternalCacheDir(), "picture.jpg");
    }

    private File getCropFile() {
        switch (type) {
            case 1:
                return new File(getExternalCacheDir(), "idCardFrontCrop.jpg");
            case 2:
                return new File(getExternalCacheDir(), "idCardBackCrop.jpg");
            case 3:
                return new File(getExternalCacheDir(), "companyInfoCrop.jpg");
        }
        return new File(getExternalCacheDir(), "pictureCrop.jpg");
    }

}
