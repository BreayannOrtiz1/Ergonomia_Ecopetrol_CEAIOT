package com.vivalnk.sdk.demo.vital.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.vivalnk.sdk.demo.base.app.BaseFragment;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.model.Device;

public class ConnectedFragment extends BaseFragment {

  protected Device mDevice;

  @Override
  protected Layout getLayout() {
    return null;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initDevice(getArguments());
  }

  protected void initDevice(Bundle arguments) {
    try {
      mDevice = (Device) arguments.getSerializable("device");
    } catch (Exception e) {
      e.printStackTrace();
    }

    assert (mDevice != null);
  }

  public static Fragment newInstance(Context context, Device device, Class clazz) {
    Bundle extras = new Bundle();
    extras.putSerializable("device", device);
    return createFragment(extras, clazz);
  }
}
