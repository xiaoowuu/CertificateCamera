package win.smartown.android.app.camerademo;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import win.smartown.android.library.certificateCamera.CameraActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.main_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String path = CameraActivity.getResult(requestCode, resultCode, data);
        if (!TextUtils.isEmpty(path)) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }

    private void takePhoto(int type) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x12);
            return;
        }
        CameraActivity.openCertificateCamera(this, type);
    }

    public void 身份证正面(View view) {
        takePhoto(CameraActivity.TYPE_IDCARD_FRONT);
    }

    public void 身份证反面(View view) {
        takePhoto(CameraActivity.TYPE_IDCARD_BACK);
    }

    public void 营业执照竖版(View view) {
        takePhoto(CameraActivity.TYPE_COMPANY_PORTRAIT);
    }

    public void 营业执照横版(View view) {
        takePhoto(CameraActivity.TYPE_COMPANY_LANDSCAPE);
    }

}
