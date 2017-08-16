package win.smartown.android.app.camerademo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import win.smartown.library.camera.CameraPreview;

/**
 * Created by Smartown on 2017/8/16.
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraPreview cameraPreview;
    private ImageView cropView;
    private ImageView flashImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", 1);
        if (type == 3) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_camera);
        cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        float maxSize = screenMinSize / 9.0f * 16.0f;
        RelativeLayout.LayoutParams layoutParams;
        if (type == 3) {
            layoutParams = new RelativeLayout.LayoutParams((int) screenMinSize, (int) maxSize);
        } else {
            layoutParams = new RelativeLayout.LayoutParams((int) maxSize, (int) screenMinSize);
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraPreview.setLayoutParams(layoutParams);

        View contentView = findViewById(R.id.camera_crop_container);
        cropView = (ImageView) findViewById(R.id.camera_crop);
        if (type == 3) {
            float width = (int) (screenMinSize * 0.752);
            float height = (int) (width * 75.0f / 47.0f);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            contentView.setLayoutParams(contentParams);
            cropView.setLayoutParams(cropParams);
        } else {
            float height = (int) (screenMinSize * 0.752);
            float width = (int) (height * 75.0f / 47.0f);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
            contentView.setLayoutParams(contentParams);
            cropView.setLayoutParams(cropParams);
        }
        switch (type) {
            case 2:
                cropView.setImageResource(R.mipmap.camera_idcard_back);
                break;
            case 3:
                cropView.setImageResource(R.mipmap.camera_company);
                break;
        }

        flashImageView = (ImageView) findViewById(R.id.camera_flash);
        findViewById(R.id.camera_close).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);
        flashImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.camera_close:
                finish();
                break;
            case R.id.camera_take:
                cameraPreview.takePhoto(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        File pictureFile = new File(getExternalCacheDir(), "picture.jpg");
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();

                            Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath());
                            float x = (float) cropView.getLeft() / (float) cropView.getWidth() * bitmap.getWidth();
                            float y = (float) cropView.getTop() / (float) cropView.getHeight() * bitmap.getHeight();
                            float width = (float) cropView.getWidth() / (float) cameraPreview.getWidth() * bitmap.getWidth();
                            float height = (float) cropView.getWidth() / (float) cameraPreview.getWidth() * bitmap.getHeight();

                            Bitmap cropBitmap = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) width, (int) height);

                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
                            cropBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                            bos.flush();
                            bos.close();
                            Intent intent = new Intent();
                            intent.putExtra("picture", pictureFile.getPath());
                            setResult(0x14, intent);
                            finish();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.camera_flash:
                boolean isFlashOn = cameraPreview.switchFlashLight();
                flashImageView.setImageResource(isFlashOn ? R.mipmap.camera_flash_on : R.mipmap.camera_flash_off);
                break;
        }
    }

}
