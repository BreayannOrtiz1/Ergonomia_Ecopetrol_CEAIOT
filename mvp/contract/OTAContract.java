package com.vivalnk.sdk.demo.vital.mvp.contract;

import com.vivalnk.sdk.demo.base.mvp.IModel;
import com.vivalnk.sdk.model.Device;

/**
 * Created by JakeMo on 18-7-25.
 */
public class OTAContract {


  public interface Model extends IModel {

  }

  public interface View extends TestContract.BaseTestView {
    void showAlertDialog(Runnable runnable, int compare);
    void showOTAResult(boolean success);
    void showOTADialog(Device device, String msg);
  }

}
