package com.vivalnk.sdk.demo.vital.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.TextView;
import butterknife.BindView;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.vital.R;

/**
 * 设备菜单界面,200
 *
 * @author jake
 * @date 2019/3/15
 */
public class DeviceMenu200Activity extends ConnectedActivity {

  @BindView(R.id.tvPatchMac)
  TextView tvPatchMac;
  @BindView(R.id.tvPatchSN)
  TextView tvPatchSN;
  @BindView(R.id.tvPatchFWVersion)
  TextView tvPatchFWVersion;
  @BindView(R.id.tvPatchHWVersion)
  TextView tvPatchHWVersion;
  @BindView(R.id.tvChargeFWVersion)
  TextView tvChargeFWVersion;
  @BindView(R.id.tvChargeHWVersion)
  TextView tvChargeHWVersion;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_detail_200);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}
