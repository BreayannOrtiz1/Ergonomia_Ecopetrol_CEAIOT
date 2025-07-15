package com.vivalnk.sdk.demo.vital.ui;

import android.os.Bundle;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.command.checkmeo2.CheckmeO2Constants;
import com.vivalnk.sdk.command.checkmeo2.base.CommandType;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.model.O2File;
import com.vivalnk.sdk.utils.GSON;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CheckmeO2Activity extends ConnectedActivity {

  private static final String TAG = "CheckmeO2Activity";

  @BindView(R.id.tvPrinter)
  TextView tvPrinter;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_checkmeo2);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Subscribe
  public void onSampleData(DeviceManager.VitalSampleData sampleData) {

    if (!sampleData.device.equals(mDevice)) {
      return;
    }

    Map<String, Object> data = sampleData.data;
    final int spo2 = (int) data.get("spo2");
    final int pr = (int) data.get("pr");
    final Float pi = (Float) data.get("pi");
    final int steps = (int) data.get("steps");
    final int battery = (int) data.get("battery");
    final int chargingStatus = (int) data.get("chargingStatus");
    StringBuffer sb = new StringBuffer();

    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm.ss");

    sb.append("\n")
        .append(format.format(new Date()))
        .append(" ")
        .append(GSON.toJson(data));

    FileUtils.writeFile(FileManager.getFileDataPath(mDevice.getName(), "data.txt"), sb.toString());

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateView(spo2, pr, pi, steps, battery, chargingStatus);
      }
    });
  }

  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    showProgressDialog("Disconnecting...");
    DeviceManager.getInstance().disconnect(mDevice);
  }

  @OnClick(R.id.btnGetRTWaveFormData)
  public void clickGetRTWaveFormData() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getRealTimeWaveform)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String filePath = FileManager.getFileDataPath(mDevice.getName(), "getRealTimeWaveform.txt");
        FileUtils.writeFile(filePath, GSON.toJson(data));
      }
    });
  }

  @OnClick(R.id.btnGetHistoryData)
  public void clickGetHistoryData() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getHistoryData)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        List<O2File> fileList = (List<O2File>) data.get("data");
        String filePath = FileManager.getFileDataPath(mDevice.getName(), "getHistoryData.txt");
        FileUtils.writeFile(filePath, GSON.toJson(fileList));
      }
    });
  }

  @OnClick(R.id.btnGetDeviceInfo)
  public void getDeviceInfo() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getDeviceInfo)
        .build();
    execute(request, new Callback() { });
  }

  @OnClick(R.id.btnPingDevice)
  public void pingDevice() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.pingDevice)
        .build();
    execute(request, new Callback() { });
  }

  @OnClick(R.id.btnSetParameters)
  public void setParameters() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.setParameters)
        .addParam(CheckmeO2Constants.ParamsKeys.SetTIME, System.currentTimeMillis())
        .build();
    execute(request);
  }

  @OnClick(R.id.btnGetRTData)
  public void getRTData() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getRealTimeData)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        final int spo2 = (int) data.get("spo2");
        final int pr = (int) data.get("pr");
        final Float pi = (Float) data.get("pi");
        final int steps = (int) data.get("steps");
        final int battery = (int) data.get("battery");
        final int chargingStatus = (int) data.get("chargingStatus");
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            updateView(spo2, pr, pi, steps, battery, chargingStatus);
          }
        });
      }
    });
  }

  @OnClick(R.id.btnFactoryReset)
  public void factoryReset() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.factoryReset)
        .build();
    execute(request);
  }

  private void updateView(int spo2, int pr, Float pi, int steps, int battery, int chargingStatus) {
    String text = new StringBuffer()
        .append("SpO2 = " + spo2).append("\n")
        .append("PI = " + pi).append("\n")
        .append("Pulse Rate = " + pr).append("\n")
        .append("Steps = " + steps).append("\n")
        .append("Battery = " + battery).append("\n")
        .append("ChargingStatus = " + getChargingStateDescription(chargingStatus)).append("\n")
        .toString();
    tvPrinter.setText(text);
  }

  private String getChargingStateDescription(int state) {
    String ret = "";
    switch (state) {
      case 0:
        ret = "No Charging";
        break;
      case 1:
        ret = "Charging";
        break;
      case 2:
        ret = "Charging Complete";
        break;
      case 3:
        ret = "Low Battery";
        break;
    }
    return ret;
  }

}
