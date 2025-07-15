package com.vivalnk.sdk.demo.vital.mvp.contract;

import com.vivalnk.sdk.demo.base.mvp.IModel;
import com.vivalnk.sdk.demo.base.mvp.IView;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.Device;

/**
 * Created by JakeMo on 18-7-16.
 */
public interface TestContract {

  public static interface BaseTestView extends IView {
    default void clearData() {

    }
    default void startTiming() {

    }
    default void stopTiming() {

    }

    default void showLeadOffDialog(Integer index, Device device, Integer time, Runnable okRunnable,
        Runnable cancelRunnable){

    }

    default void showLeadStatus(boolean leadOn) {

    }

  }

  interface ConnectView extends BaseTestView {
    void showVoltage(boolean isError, String name, String voltage);
    void setChargerStatus(BatteryInfo info);
    void showFWVersion(boolean isFWValid, String fwVersion);
    void showHWVersion(boolean isHWValid, String hwVersion);
    void showErrorPatchClock(int code, String msg);
    void showSelfTest(boolean success);
    void clearStatus();
  }

  interface EcgView extends BaseTestView {
    void clearEcgText();

    void showECGStartDialog(int index, int hr, Device device);
    void showEcgResult(boolean success);
    void showDialog(String msg);
    default void showDialog(String msg, Runnable okRunnable, Runnable cancelRunnable){

    }
  }

  interface FqView extends BaseTestView {
    void clearFqData();
    void showFqStartDialog(int i, int fv, float fr, int time, Device device);
    void showFqRetryDialog(int i, int fv, float fr, int time, Device device);
    void showFqResult(boolean success);
  }

  interface HeartView extends BaseTestView {
    void clearHeartData();
    void showStartDialog(int index, int hr, int time, Device device);
    void showErrorRetryDialog(int index, int hr, int time, Device device);
    void showHeartResult(boolean success);
  }

  interface LeadAccView extends BaseTestView {
    void showMotionDirection(String motionDirection);
  }

  interface ShutdownView extends BaseTestView {

    void showShutResult(boolean success);
  }

  interface Model extends IModel {

  }

}
