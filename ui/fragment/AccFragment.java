package com.vivalnk.sdk.demo.vital.ui.fragment;

import butterknife.BindView;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.base.widget.AccView;
import com.vivalnk.sdk.model.SampleData;

public class AccFragment extends ConnectedFragment {

  @BindView(R.id.accView)
  AccView accView;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.fragment_acc_graphic);
  }

  @Subscribe
  public void onEcgDataEvent(SampleData ecgData) {
    if (!ecgData.getDeviceID().equals(mDevice.getId())) {
      return;
    }
    if (!ecgData.isFlash()) {
      accView.addAccData(ecgData.getACC());
    }
  }

}
