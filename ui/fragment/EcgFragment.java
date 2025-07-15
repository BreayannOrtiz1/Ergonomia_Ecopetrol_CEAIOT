package com.vivalnk.sdk.demo.vital.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.widget.LiveEcgScreen;
import com.vivalnk.sdk.demo.base.widget.RTSEcgView;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.Motion;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.open.BaseLineRemover;
import java.util.List;
import com.microsoft.azure.sdk.iot.device.*;



public class EcgFragment extends ConnectedFragment {

  @BindView(R.id.ecgView)
  RTSEcgView ecgView;

  @BindView(R.id.btnSwitchGain)
  Button btnSwitchGain;

  @BindView(R.id.btnRevert)
  Button btnRevert;

  @BindView(R.id.btnNoisy)
  Button btnNoisy;

  @BindView(R.id.tvHR)
  TextView tvHR;

  @BindView(R.id.tvRR)
  TextView tvRR;

  LiveEcgScreen mLiveEcgScreen;

  private BaseLineRemover remover;

  private volatile boolean denoisy;
  private volatile boolean revert;

  private final String connString = "HostName=ingenieriaiothub2.azure-devices.net;DeviceId=ECG_ANDROID;SharedAccessKey=3+dDj9xhVlODdWlHcqdHta3BRggYZe1CZ97xavTj5JM=";

  private DeviceClient client;
  private Twin twin;

  IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.fragment_ecg_graphic);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView();
  }

  private void initView() {

    client = new DeviceClient(connString, protocol);

    try
    {
      client.open(true);
      //MessageCallbackMqtt callback = new MessageCallbackMqtt();
      //client.setMessageCallback(callback, counter);
      //client.subscribeToMethods(this, null);
      //client.subscribeToDesiredProperties(this, null);
      //twin = client.getTwin();

    }
    catch (Exception e2)
    {
      System.err.println("Exception while opening IoTHub connection: " + e2.getMessage());
      client.close();
      System.out.println("Shutting down...");
    }

    remover = new BaseLineRemover(mDevice, new BaseLineRemover.Listener() {
      @Override
      public void onDataPop(Device device, SampleData ecgData) {
        mLiveEcgScreen.update(ecgData);




        if (ecgData.getHR() != null && ecgData.getHR() > 0) {
          int[] ec = ecgData.getECG();


          Motion[] ac =ecgData.getACC();

          String msgStr = "{\"TimeEpoch\":" + ecgData.getTime() + ",\"deviceId\":\"" + ecgData.getDeviceID()+ "\",\"HR\":" + ecgData.getHR() +",\"RR\":" + ecgData.getRR() + ",\"ECG\": [";
          for ( int i =0;i <127;i++){
            msgStr=msgStr+ec[i]+",";
          }

          msgStr=msgStr+ec[127]+"],\"ac0\":" +ac[0]+",\"ac1\":" +ac[1]+",\"ac2\":" +ac[2]+",\"ac3\":" +ac[3]+",\"ac4\":" +ac[4]+"}";

          tvHR.setText("HR: " + ecgData.getHR());
          try
          {
            Message msg = new Message(msgStr);
            msg.setMessageId(java.util.UUID.randomUUID().toString());
            client.sendEventAsync(msg, new MessageSentCallbackImpl(), null);
          }
          catch (Exception e)
          {
            System.err.println("Exception while sending event: " + e.getMessage());
          }
        }

        if (ecgData.getRR() != null && ecgData.getRR() > 0) {
          tvRR.setText("RR: " + ecgData.getRR());
        }
      }

      @Override
      public void onDataDiscontinous(Device device, List<SampleData> dataList) {

      }

      @Override
      public void onError(Device device, int code, String msg) {

      }
    });

    mLiveEcgScreen = new LiveEcgScreen(getContext(), mDevice, ecgView);
    mLiveEcgScreen.setDrawDirection(RTSEcgView.LEFT_IN_RIGHT_OUT);
    mLiveEcgScreen.showMarkPoint(true);

    btnNoisy.setText(denoisy ? R.string.tv_denoising_close : R.string.tv_denoising_open);
    btnRevert.setText(revert ? R.string.tv_de_revert : R.string.tv_revert);
  }

  @Subscribe
  public void onEcgDataEvent(SampleData ecgData) {
    if (!ecgData.getDeviceID().equals(mDevice.getId())) {
      return;
    }
    if (!ecgData.isFlash()) {
      remover.handle(ecgData);
    }
  }

  @OnClick(R.id.btnSwitchGain)
  protected void clickSwitchGain() {
    mLiveEcgScreen.switchGain();
  }

  @OnClick(R.id.btnRevert)
  protected void clickRevert() {
    revert = !revert;
    mLiveEcgScreen.revert(revert);
    btnRevert.setText(revert ? R.string.tv_de_revert : R.string.tv_revert);
  }

  @OnClick(R.id.btnNoisy)
  protected void clickDenoisy() {
    denoisy = !denoisy;
    mLiveEcgScreen.denoisy(denoisy);
    btnNoisy.setText(denoisy ? R.string.tv_denoising_close : R.string.tv_denoising_open);
  }

  @Override
  public void onDestroyView() {
    mLiveEcgScreen.destroy();
    super.onDestroyView();
  }

  static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
  {
    public IotHubMessageResult onCloudToDeviceMessageReceived(Message msg, Object context)
    {

      System.out.println(
              "Received message " +
                      " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
      ;

      return IotHubMessageResult.COMPLETE;
    }
  }
  static class MessageSentCallbackImpl implements MessageSentCallback
  {
    @Override
    public void onMessageSent(Message message, IotHubClientException e, Object o)
    {
      if (e == null)
      {
        System.out.println("IoT Hub responded to message " + message.getMessageId() + " with status OK");
      }
      else
      {
        System.out.println("IoT Hub responded to message " + message.getMessageId() + " with status " + e.getStatusCode().name());
      }
    }
  }
}
