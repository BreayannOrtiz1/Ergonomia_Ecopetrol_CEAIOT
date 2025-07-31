package com.vivalnk.sdk.demo.vital.ui;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
//import com.tbruyelle.rxpermissions3.RxPermissions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.demo.base.app.BaseToolbarActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.base.DemoApplication;
import io.reactivex.disposables.Disposable;
//import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 开始界面
 *
 * @author Aslan
 * @date 2019/3/14
 */
public class WelcomeActivity extends BaseToolbarActivity {

  private RxPermissions rxPermissions;
  private Disposable permissionDisposable;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_welcome);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    checkPermission();
  }

  //request location and write permissions at rum time
  private void checkPermission() {
    rxPermissions = new RxPermissions(this);
    String[] permissions = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };

    if (Build.VERSION.SDK_INT >= 29) {
      permissions = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_BACKGROUND_LOCATION
      };
    }

    permissionDisposable = rxPermissions.request(permissions)
        .subscribe(granted -> {
          if (granted) {
            startActivity(ScanningActivity.newIntent(this));
            finish();
          } else {
            showToast("You must grant the permissions!");
            finish();
          }
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (!permissionDisposable.isDisposed()) {
      permissionDisposable.dispose();
    }

  }
}
