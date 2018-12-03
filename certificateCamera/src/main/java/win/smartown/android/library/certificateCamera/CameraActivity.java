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
 * Created by smartown on 2018/2/24 11:46.
 * <br>
 * Desc:
 * <br>
 * 拍照界面
 */
public class CameraActivity extends Activity implements View.OnClickListener {

    /**
     * 拍摄类型-身份证正面
     */
    public final static int TYPE_IDCARD_FRONT = 1;
    /**
     * 拍摄类型-身份证反面
     */
    public final static int TYPE_IDCARD_BACK = 2;
    /**
     * 拍摄类型-竖版营业执照
     */
    public final static int TYPE_COMPANY_PORTRAIT = 3;
    /**
     * 拍摄类型-横版营业执照
     */
    public final static int TYPE_COMPANY_LANDSCAPE = 4;

    public final static int REQUEST_CODE = 0X13;
    public final static int RESULT_CODE = 0X14;

    /**
     * @param type {@link #TYPE_IDCARD_FRONT}
     *             {@link #TYPE_IDCARD_BACK}
     *             {@link #TYPE_COMPANY_PORTRAIT}
     *             {@link #TYPE_COMPANY_LANDSCAPE}
     */
    public static void openCertificateCamera(Activity activity, int type) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("type", type);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * @return 结果文件路径
     */
    public static String getResult(Intent data) {
        if (data != null) {
            return data.getStringExtra("result");
        }
        return "";
    }

    private CameraPreview cameraPreview;
    private View containerView;
    private ImageView cropView;
    private ImageView flashImageView;
    private View optionView;
    private View resultView;

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", 0);
        if (type == TYPE_COMPANY_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_camera);
        cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
        //获取屏幕最小边，设置为cameraPreview较窄的一边
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        //根据screenMinSize，计算出cameraPreview的较宽的一边，长宽比为标准的16:9
        float maxSize = screenMinSize / 9.0f * 16.0f;
        RelativeLayout.LayoutParams layoutParams;
        if (type == TYPE_COMPANY_PORTRAIT) {
            layoutParams = new RelativeLayout.LayoutParams((int) screenMinSize, (int) maxSize);
        } else {
            layoutParams = new RelativeLayout.LayoutParams((int) maxSize, (int) screenMinSize);
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraPreview.setLayoutParams(layoutParams);

        containerView = findViewById(R.id.camera_crop_container);
        cropView = (ImageView) findViewById(R.id.camera_crop);
        if (type == TYPE_COMPANY_PORTRAIT) {
            float width = (int) (screenMinSize * 0.8);
            float height = (int) (width * 43.0f / 30.0f);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            containerView.setLayoutParams(containerParams);
            cropView.setLayoutParams(cropParams);
        } else if (type == TYPE_COMPANY_LANDSCAPE) {
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
            case TYPE_IDCARD_FRONT:
                cropView.setImageResource(R.mipmap.camera_idcard_front);
                break;
            case TYPE_IDCARD_BACK:
                cropView.setImageResource(R.mipmap.camera_idcard_back);
                break;
            case TYPE_COMPANY_PORTRAIT:
                cropView.setImageResource(R.mipmap.camera_company);
                break;
            case TYPE_COMPANY_LANDSCAPE:
                cropView.setImageResource(R.mipmap.camera_company_landscape);
                break;
        }

        flashImageView = (ImageView) findViewById(R.id.camera_flash);
        optionView = findViewById(R.id.camera_option);
        resultView = findViewById(R.id.camera_result);
        cameraPreview.setOnClickListener(this);
        findViewById(R.id.camera_close).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);
        flashImageView.setOnClickListener(this);
        findViewById(R.id.camera_result_ok).setOnClickListener(this);
        findViewById(R.id.camera_result_cancel).setOnClickListener(this);
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
        } else if (id == R.id.camera_result_ok) {
            goBack();
        } else if (id == R.id.camera_result_cancel) {
            optionView.setVisibility(View.VISIBLE);
            cameraPreview.setEnabled(true);
            resultView.setVisibility(View.GONE);
            cameraPreview.startPreview();
        }
    }

    private void takePhoto() {
        optionView.setVisibility(View.GONE);
        cameraPreview.setEnabled(false);
        cameraPreview.takePhoto(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                //子线程处理图片，防止ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File originalFile = getOriginalFile();
                            FileOutputStream originalFileOutputStream = new FileOutputStream(originalFile);
                            originalFileOutputStream.write(data);
                            originalFileOutputStream.close();

                            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getPath());

                            //计算裁剪位置
                            float left, top, right, bottom;
                            if (type == TYPE_COMPANY_PORTRAIT) {
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
                            //裁剪及保存到文件
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
                                    resultView.setVisibility(View.VISIBLE);
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
                                optionView.setVisibility(View.VISIBLE);
                                cameraPreview.setEnabled(true);
                            }
                        });
                    }
                }).start();

            }
        });
    }

    /**
     * @return 拍摄图片原始文件
     */
    private File getOriginalFile() {
        switch (type) {
            case TYPE_IDCARD_FRONT:
                return new File(getExternalCacheDir(), "idCardFront.jpg");
            case TYPE_IDCARD_BACK:
                return new File(getExternalCacheDir(), "idCardBack.jpg");
            case TYPE_COMPANY_PORTRAIT:
            case TYPE_COMPANY_LANDSCAPE:
                return new File(getExternalCacheDir(), "companyInfo.jpg");
        }
        return new File(getExternalCacheDir(), "picture.jpg");
    }

    /**
     * @return 拍摄图片裁剪文件
     */
    private File getCropFile() {
        switch (type) {
            case TYPE_IDCARD_FRONT:
                return new File(getExternalCacheDir(), "idCardFrontCrop.jpg");
            case TYPE_IDCARD_BACK:
                return new File(getExternalCacheDir(), "idCardBackCrop.jpg");
            case TYPE_COMPANY_PORTRAIT:
            case TYPE_COMPANY_LANDSCAPE:
                return new File(getExternalCacheDir(), "companyInfoCrop.jpg");
        }
        return new File(getExternalCacheDir(), "pictureCrop.jpg");
    }

    /**
     * 点击对勾，使用拍照结果，返回对应图片路径
     */
    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra("result", getCropFile().getPath());
        setResult(RESULT_CODE, intent);
        finish();
    }

}
