package com.vivalnk.sdk.demo.vital.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.view.View;
import butterknife.BindView;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.common.ble.utils.CheckCodeUtils;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.base.BaseDeviceActivity;

/**
 * Created by JakeMo on 18-4-27.
 */
public class RuntimeCheckActivity extends BaseDeviceActivity {

  public static Intent newIntent(Context context) {
    Intent intent = new Intent(context, RuntimeCheckActivity.class);
    return intent;
  }

  @BindView(R.id.checkboxBLESupport)
  AppCompatCheckBox checkboxBLESupport;
  @BindView(R.id.checkboxBluetoothEnable)
  AppCompatCheckBox checkboxBluetoothEnable;
  // >= API23
  @BindView(R.id.checkboxLocationEnable)
  AppCompatCheckBox checkboxLocationEnable;

  @BindView(R.id.checkboxBluetoothPersmission)
  AppCompatCheckBox checkboxBluetoothPersmission;
  // >= API23
  @BindView(R.id.checkboxLocationPersmission)
  AppCompatCheckBox checkboxLocationPersmission;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_runtime_check);
  }

  @Override
  protected void onResume() {
    super.onResume();
    initData();
  }

  private void initData() {
    int code = DeviceManager.getInstance().checkBle();

    checkboxBLESupport.setChecked(CheckCodeUtils.isSupportBLE(code));
    checkboxBluetoothEnable.setChecked(CheckCodeUtils.isBluetoothEnable(code));
    checkboxLocationEnable.setChecked(CheckCodeUtils.isLocationEnable(code));

    checkboxBluetoothPersmission.setChecked(CheckCodeUtils.hasBluetoothPermission(code));
    checkboxLocationPersmission.setChecked(CheckCodeUtils.hasLocationPermission(code));
  }

  private void initView() {
    checkboxLocationPersmission.setVisibility(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? View.VISIBLE : View.GONE);
    checkboxLocationEnable.setVisibility(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? View.VISIBLE : View.GONE);
    toolbar.setNavigationOnClickListener((v) -> finish());
  }

  @Override
  protected void onBluetoothTurnOff() {
    super.onBluetoothTurnOff();
    checkboxBluetoothEnable.setChecked(false);
  }

  @Override
  protected void onBluetoothTurnOn() {
    super.onBluetoothTurnOn();
    checkboxBluetoothEnable.setChecked(true);
  }

  @Override
  protected void onLocationTurnOn() {
    super.onLocationTurnOn();
    checkboxLocationEnable.setChecked(true);
  }

  @Override
  protected void onLocationTurnOff() {
    super.onLocationTurnOff();
    checkboxLocationEnable.setChecked(false);
  }
}
