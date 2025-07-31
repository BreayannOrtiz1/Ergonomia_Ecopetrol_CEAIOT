package com.vivalnk.sdk.demo.vital.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import android.text.TextUtils;
//import com.tbruyelle.rxpermissions3.RxPermissions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vivalnk.sdk.demo.base.app.BaseToolbarActivity;
import com.vivalnk.sdk.demo.repository.device.ConnectEvent;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.PermissionHelper;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.ble.Utile;
import com.vivalnk.sdk.model.Device;
import io.reactivex.disposables.Disposable;
//import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 设备控制模块
 *
 * @author Aslan
 * @date 2019/3/15
 */
public abstract class BaseDeviceActivity extends BaseToolbarActivity {

  private static final int REQUESTCODE_BLE_SETTING = 1000;
  private static final int REQUESTCODE_GPS_SETTING = REQUESTCODE_BLE_SETTING + 1;

  private AlertDialog dialogBluetoothError;//蓝牙检查错误
  private AlertDialog dialogLocationError;//location检查错误
  protected ProgressDialog dialogConnect;//连接等待框

  private RxPermissions rxPermissions;
  private Disposable permissionDisposable;

  /**
   * 蓝牙开关监听
   */
  private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (null == intent || TextUtils.isEmpty(intent.getAction())) {
        return;
      }

      switch (intent.getAction()) {
        case BluetoothAdapter.ACTION_STATE_CHANGED:
          int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
          switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
              break;
            case BluetoothAdapter.STATE_ON:
              onBluetoothTurnOn();
            case BluetoothAdapter.STATE_TURNING_OFF:
              break;
            case BluetoothAdapter.STATE_OFF:
              onBluetoothTurnOff();
              break;
          }
          break;
      }
    }
  };

  /**
   * 位置开关监听
   */
  private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (null == intent || TextUtils.isEmpty(intent.getAction())) {
        return;
      }

      if (Utile.isLocationEnable(BaseDeviceActivity.this)) {
        onLocationTurnOn();
      } else {
        onLocationTurnOff();
      }
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (EventBus.getDefault().isRegistered(this) == false) {
      EventBus.getDefault().register(this);
    }

    IntentFilter filter = new IntentFilter();
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    registerReceiver(mBleReceiver, filter);

    registerReceiver(mLocationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

    if (Utile.isBLEEnabled(this)) {
      onBluetoothTurnOn();
    } else {
      onBluetoothTurnOff();
    }

    if (Utile.isLocationEnable(BaseDeviceActivity.this)) {
      onLocationTurnOn();
    } else {
      onLocationTurnOff();
    }
  }

  /**
   * 操作之前检查蓝牙信息,会弹出错误提示框
   */
  protected boolean checkBLE() {

    if (isFinishing()) {
      return false;
    }

    rxPermissions = new RxPermissions(this);
    if (!PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
        !PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
        !PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
      permissionDisposable = rxPermissions.request(
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.ACCESS_COARSE_LOCATION)
          .subscribe();
      return false;
    }

    if (!Utile.isBLESupported(this)) {
      return false;
    }

    if (!Utile.isBLEEnabled(this)) {
      showBluetoothErrorDialog();
      return false;
    }
    closeBluetoothErrorDialog();

    if (!Utile.isLocationEnable(this)) {
      showGpsErrorDialog();
      return false;
    }
    closeLocationErrorDialog();
    return true;
  }

  /**
   * 蓝牙检查错误
   */
  private void showBluetoothErrorDialog() {
    if (dialogBluetoothError != null && dialogBluetoothError.isShowing()) {
      return;
    }
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.error_bluetooth_enable_title)
        .setMessage(R.string.error_bluetooth_enable_message)
        .setPositiveButton(R.string.error_bluetooth_enable_open, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            openBLESetting();
          }
        });
    dialogBluetoothError = builder.show();
  }

  private void closeBluetoothErrorDialog() {
    if (dialogBluetoothError == null || dialogBluetoothError.isShowing() == false) {
      return;
    }

    dialogBluetoothError.dismiss();
  }

  /**
   * 位置信息错误
   */
  private void showGpsErrorDialog() {
    if (dialogLocationError != null && dialogLocationError.isShowing()) {
      return;
    }
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.error_gps_enable_title)
        .setMessage(R.string.error_gps_enable_message)
        .setPositiveButton(R.string.error_bluetooth_enable_open, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            openGPSSetting();
          }
        });
    dialogLocationError = builder.show();
  }

  private void closeLocationErrorDialog() {
    if (dialogLocationError == null || dialogLocationError.isShowing() == false) {
      return;
    }

    dialogLocationError.dismiss();
  }

  /**
   * 申请打开蓝牙设置界面
   */
  private void openBLESetting() {
    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivityForResult(intent, REQUESTCODE_BLE_SETTING);
  }

  /**
   * 打开位置服务设置界面
   */
  private void openGPSSetting() {
    Intent intent = new Intent();
    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivityForResult(intent, REQUESTCODE_GPS_SETTING);
  }

  @CallSuper
  protected void onBluetoothTurnOn() {
    closeBluetoothErrorDialog();
  }

  @CallSuper
  protected void onBluetoothTurnOff() {
    DeviceManager.getInstance().stopScan();
    DeviceManager.getInstance().disconnectAll();
    showBluetoothErrorDialog();
  }

  @CallSuper
  protected void onLocationTurnOn() {
    closeLocationErrorDialog();
  }

  @CallSuper
  protected void onLocationTurnOff() {
    showGpsErrorDialog();
  }

  @CallSuper
  @Override
  protected void onDestroy() {
    closeBluetoothErrorDialog();
    closeLocationErrorDialog();
    closeConnectDialog();
    unregisterReceiver(mBleReceiver);
    unregisterReceiver(mLocationReceiver);
    if (permissionDisposable != null) {
      permissionDisposable.dispose();
    }
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }

  protected void showConnectDialog(Device device) {
    if (dialogConnect != null && dialogConnect.isShowing()) {
      return;
    }

    if (dialogConnect == null) {
      dialogConnect = new ProgressDialog(this);
      dialogConnect.setCancelable(false);
      dialogConnect.setCanceledOnTouchOutside(false);
    }
    dialogConnect.setMessage(getString(R.string.dialog_connect, device.getName()));
    dialogConnect.show();
  }

  protected void closeConnectDialog() {
    if (dialogConnect == null || dialogConnect.isShowing() == false) {
      return;
    }

    dialogConnect.dismiss();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onConnectEvent(ConnectEvent connectEvent) {
    onConnectChanged(connectEvent);
  }

  @CallSuper
  protected void onConnectChanged(ConnectEvent connectEvent) {
    if (connectEvent == null) {
      return;
    }

    if (ConnectEvent.ON_START.equalsIgnoreCase(connectEvent.event)) {
      showConnectDialog(connectEvent.device);
    } else if (ConnectEvent.ON_DEVICE_READY.equalsIgnoreCase(connectEvent.event)
        || ConnectEvent.ON_ERROR.equalsIgnoreCase(connectEvent.event)) {
      closeConnectDialog();
    }
  }
}
