package win.smartown.android.app.camerademo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

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
        if (requestCode == 0x13 && resultCode == 0x14) {
            Bitmap bitmap = data.getParcelableExtra("picture");
            System.out.println(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }

    public void 横屏拍照(View view) {
        takePhoto(true);
    }

    public void 竖屏拍照(View view) {
        takePhoto(false);
    }

    private void takePhoto(boolean landscape) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x12);
            return;
        }
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("landscape", landscape);
        startActivityForResult(intent, 0x13);
    }
}
