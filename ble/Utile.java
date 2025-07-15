package com.vivalnk.sdk.demo.vital.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

/**
 * 帮助类
 *
 * @author Aslan
 * @date 2019/3/15
 */
public class Utile {

  /**
   * 手机是否支持蓝牙
   */
  public static boolean isBLESupported(Context context) {
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
  }

  /**
   * 蓝牙是否可用
   */
  public static boolean isBLEEnabled(Context context) {
    final BluetoothManager manager = (BluetoothManager) context
        .getSystemService(Context.BLUETOOTH_SERVICE);
    final BluetoothAdapter adapter = manager.getAdapter();
    return adapter != null && adapter.isEnabled();
  }

  /**
   * android 6.0 以上location是否可用
   */
  public static boolean isLocationEnable(Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }

    try {
      LocationManager locationManager = (LocationManager) context
          .getSystemService(Context.LOCATION_SERVICE);
      boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      return gps || network;
    } catch (Exception e) {
      return false;
    }
  }
}
