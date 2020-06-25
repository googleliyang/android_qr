package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import cn.bingoogolapple.qrcode.core.BarcodeType;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.BarcodeFormat;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements QRCodeView.Delegate , EasyPermissions.PermissionCallbacks{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;

    private ZBarView mZBarView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        BGAQRCodeUtil.setDebug(true);

        mZBarView = findViewById(R.id.zbarview);
        mZBarView.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        requestCodeQRCodePermissions();


        mZBarView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//        mZBarView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        mZBarView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    @Override
    protected void onDestroy() {
        mZBarView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        // setTitle("扫描结果为：" + result);
        Toast.makeText(this, "扫描结果为：" + result, Toast.LENGTH_SHORT).show();
        vibrate();

        mZBarView.startSpot(); // 开始识别
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = mZBarView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZBarView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZBarView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_preview:
                mZBarView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
                break;
            case R.id.stop_preview:
                mZBarView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
                break;
            case R.id.start_spot:
                mZBarView.startSpot(); // 开始识别
                break;
            case R.id.stop_spot:
                mZBarView.stopSpot(); // 停止识别
                break;
            case R.id.start_spot_showrect:
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.stop_spot_hiddenrect:
                mZBarView.stopSpotAndHiddenRect(); // 停止识别，并且隐藏扫描框
                break;
            case R.id.show_scan_rect:
                mZBarView.showScanRect(); // 显示扫描框
                break;
            case R.id.hidden_scan_rect:
                mZBarView.hiddenScanRect(); // 隐藏扫描框
                break;
            case R.id.decode_scan_box_area:
                mZBarView.getScanBoxView().setOnlyDecodeScanBoxArea(true); // 仅识别扫描框中的码
                break;
            case R.id.decode_full_screen_area:
                mZBarView.getScanBoxView().setOnlyDecodeScanBoxArea(false); // 识别整个屏幕中的码
                break;
            case R.id.open_flashlight:
                mZBarView.openFlashlight(); // 打开闪光灯
                break;
            case R.id.close_flashlight:
                mZBarView.closeFlashlight(); // 关闭闪光灯
                break;
            case R.id.scan_one_dimension:
                mZBarView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
                mZBarView.setType(BarcodeType.ONE_DIMENSION, null); // 只识别一维条码
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_two_dimension:
                mZBarView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
                mZBarView.setType(BarcodeType.TWO_DIMENSION, null); // 只识别二维条码
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_qr_code:
                mZBarView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
                mZBarView.setType(BarcodeType.ONLY_QR_CODE, null); // 只识别 QR_CODE
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_code128:
                mZBarView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
                mZBarView.setType(BarcodeType.ONLY_CODE_128, null); // 只识别 CODE_128
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_ean13:
                mZBarView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
                mZBarView.setType(BarcodeType.ONLY_EAN_13, null); // 只识别 EAN_13
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_high_frequency:
                mZBarView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
                mZBarView.setType(BarcodeType.HIGH_FREQUENCY, null); // 只识别高频率格式，包括 QR_CODE、ISBN13、UPC_A、EAN_13、CODE_128
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_all:
                mZBarView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
                mZBarView.setType(BarcodeType.ALL, null); // 识别所有类型的码
                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;
            case R.id.scan_custom:
                mZBarView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式

                List<BarcodeFormat> formatList = new ArrayList<>();
                formatList.add(BarcodeFormat.QRCODE);
                formatList.add(BarcodeFormat.ISBN13);
                formatList.add(BarcodeFormat.UPCA);
                formatList.add(BarcodeFormat.EAN13);
                formatList.add(BarcodeFormat.CODE128);
                mZBarView.setType(BarcodeType.CUSTOM, formatList); // 自定义识别的类型

                mZBarView.startSpotAndShowRect(); // 显示扫描框，并开始识别
                break;

            case R.id.choose_qrcde_from_gallery:
                /*
                从相册选取二维码图片，这里为了方便演示，使用的是
                https://github.com/bingoogolapple/BGAPhotoPicker-Android
                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
                 */
                Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(this)
                        .cameraFileDir(null)
                        .maxChooseCount(1)
                        .selectedPhotos(null)
                        .pauseOnScroll(false)
                        .build();
                startActivityForResult(photoPickerIntent, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mZBarView.showScanRect();

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picturePath = BGAPhotoPickerActivity.getSelectedPhotos(data).get(0);
            mZBarView.decodeQRCode(picturePath);
        }
    }
}
