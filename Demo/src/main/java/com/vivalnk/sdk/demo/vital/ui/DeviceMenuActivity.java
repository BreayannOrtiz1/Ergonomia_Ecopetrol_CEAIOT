package com.vivalnk.sdk.demo.vital.ui;

import android.os.PowerManager;
import android.provider.Settings;

import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnClick;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.command.base.CommandType;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.utils.NotificationUtils;
import com.vivalnk.sdk.demo.base.widget.LogListDialogView;
import com.vivalnk.sdk.demo.core.WfdbUtils;
import com.vivalnk.sdk.demo.repository.database.DatabaseManager;
import com.vivalnk.sdk.demo.repository.database.VitalData;
import com.vivalnk.sdk.demo.repository.database.exception.DataEmptyExeption;
import com.vivalnk.sdk.demo.repository.device.DeviceETEManager;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.Motion;
import com.vivalnk.sdk.model.PatchStatusInfo;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.utils.DateFormat;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.android.service.MqttAndroidClient;
import javax.net.ssl.SSLSocketFactory;


import android.os.Handler;
import android.os.Looper;
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.core.ObservableEmitter;
//import io.reactivex.rxjava3.core.ObservableOnSubscribe;
//import io.reactivex.rxjava3.core.Observer;
//import io.reactivex.rxjava3.disposables.Disposable;
//import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备菜单界面
 *
 * @author jake
 * @date 2019/3/15
 */
public class DeviceMenuActivity extends ConnectedActivity {

  @BindView(R.id.btnAbrirCuestionario)
  Button mBtnCuestionario;
  @BindView(R.id.btnDetail)
  Button mBtnDetail;
  @BindView(R.id.tvStatus)
  TextView mTvStatus;
  //vv310
  @BindView(R.id.btnUploadFlash)
  Button btnUploadFlash;
  @BindView(R.id.btnCancelUpload)
  Button btnCancelUpload;
  @BindView(R.id.btnEngineerModule)
  Button btnEngineerModule;

  private LogListDialogView mDataLogView;
  private LogListDialogView mOperationLogView;
  private NotificationUtils mNotificationUtils;
  String NRF_CONNECT_CLASS = "com.vivalnk.sdk.engineer.ui.EngineerAcitivity";
  
  //MQTT configuration
  
  String iotHubName = "ingenieriaiothub";
  String deviceId = "ECG_ANDROID";
  String brokerUrl = "ssl://" + iotHubName + ".azure-devices.net:8883";
  String topic = "devices/" + deviceId + "/messages/events/";
  String username_mqtt = iotHubName + ".azure-devices.net/" + deviceId + "/api-version=2018-06-30";
  // SAS Token CORRECTO (sin doble SharedAccessSignature)
  String sasToken = "SharedAccessSignature sr=ingenieriaiothub.azure-devices.net%2Fdevices%2FECG_ANDROID&sig=PmfPmJmMATqkC8frB8tq4dgbaXTEXO2iTvmTMhVzdfQ%3D&se=1771883740";
  Boolean ToasMqtt = true;
  private boolean isSendingMQTT = false;
  private MqttAndroidClient mqttClient;

  // Control Variables
  private Integer lastBatteryLevel = null;
  private static final long MQTT_SEND_TIMEOUT_MS = 30_000; // 30 segundos
  private Handler mqttTimeoutHandler = new Handler(Looper.getMainLooper());
  private Runnable mqttTimeoutRunnable;

  // Questionnaire

  //  Respuestas
  String nombre = " ";
  String res1 = " ";
  String res2 = " ";
  String res3 = " ";
  String res4 = " ";
  String res5 = " ";
  String res6 = " ";
  String res7 = " ";
  String res8 = " ";
  String res9 = " ";
  String res10 = " ";

  String puntajeJuego = "";



  private void restartMqttTimeoutTimer() {
    if (mqttTimeoutRunnable != null) {
        mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
    }
    mqttTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // Notificación de alerta por timeout
            mNotificationUtils.sendNotification(
                "Alerta de transmisión",
                "No se están enviando datos por MQTT. Verifica la conexión o el sensor." 
            );
        }
    };
    mqttTimeoutHandler.postDelayed(mqttTimeoutRunnable, MQTT_SEND_TIMEOUT_MS);
}

private void sendMQTTMessage(SampleData data){
    try {
        // 1. TimeStamp
        long timestamp = data.getTime();
        // 2. DeviceID
        String deviceID = data.getDeviceID();
        // 3. DeviceSN
        String deviceSN = mDevice.getSn(); // Asumiendo que mDevice está disponible
        // 4. HR
        Integer hr = data.getHR();
        // 5. RR
        Float rr = data.getRR();
        // 6. ACC (acelerómetro)
        Motion[] acc = data.getACC();
        // 7. AccAccuracy
        int accAccuracy = data.getAccAccuracy();
        // 8. AverageRR
        Float avgRR = data.getAverageRR();
        // 9. isLeadOn
        Boolean isLeadOn = data.isLeadOn();
        // Mostrar alerta si isLeadOn es false
        if (isLeadOn != null && !isLeadOn) {
            mNotificationUtils.sendNotification("Sensor desconectado", "Por favor, revisa el electrodo. No se están enviando datos.");
        }
        // 10. isActivity
        Boolean isActivity = data.isActivity();
        // 11. Batería
        Integer batteryLevel = lastBatteryLevel;
        // 12. ACC
        StringBuilder accArray = new StringBuilder("[");
        if (acc != null) {
            for (int i = 0; i < acc.length; i++) {
                accArray.append(acc[i]);
                if (i < acc.length - 1) accArray.append(",");
            }
        }
        accArray.append("]");
        // 13. ECG
        int[] ecg = data.getECG(); // 128 datos
        StringBuilder ecgArray = new StringBuilder("[");
        if (ecg != null) {
            for (int i = 0; i < ecg.length; i++) {
                ecgArray.append(ecg[i]);
                if (i < ecg.length - 1) ecgArray.append(",");
            }
        }
        ecgArray.append("]");
        // JSON
        if (timestamp <= 0) timestamp = System.currentTimeMillis(); // o usar System.currentTimeMillis() si viene vacío
        if (deviceID == null || deviceID.isEmpty()) deviceID = "N/A";
        if (deviceSN == null) deviceSN = "N/A";
        if (hr == null) hr = -1;
        if (rr == null) rr = -1f;
        // ACC
        if (avgRR == null) avgRR = -1f;
        if (isLeadOn == null) isLeadOn = false;
        if (isActivity == null) isActivity = false;
        if (batteryLevel == null) batteryLevel = -1;
        // ECG
        if (nombre == null) nombre = "N/A";
        if (res1 == null) res1 = "N/A";
        if (res2 == null) res2 = "N/A";
        if (res3 == null) res3 = "N/A";
        if (res4 == null) res4 = "N/A";
        if (res5 == null) res5 = "N/A";
        if (res6 == null) res6 = "N/A";
        if (res7 == null) res7 = "N/A";
        if (res8 == null) res8 = "N/A";
        if (res9 == null) res9 = "N/A";
        if (res10 == null) res10 = "N/A";
        if (puntajeJuego == null) puntajeJuego = "N/A";


        String payload = "{"
                + "\"TimeStamp\":" + timestamp
                + ",\"DeviceID\":\"" + deviceID + "\""
                + ",\"DeviceSN\":\"" + deviceSN + "\""
                + ",\"HR\":" + hr
                + ",\"RR\":" + rr
                + ",\"ACC\":" + accArray.toString()
                + ",\"AccAccuracy\":" + accAccuracy
                + ",\"AverageRR\":" + avgRR
                + ",\"isLeadOn\":" + isLeadOn
                + ",\"isActivity\":" + isActivity
                + ",\"BatteryPercentage\":" + batteryLevel
                + ",\"ECG\":" + ecgArray.toString()
                + ",\"Nombre\":\"" + nombre + "\""
                + ",\"R1\":\""+ res1 + "\""
                + ",\"R2\":\""+ res2 + "\""
                + ",\"R3\":\""+ res3 + "\""
                + ",\"R4\":\""+ res4 + "\""
                + ",\"R5\":\""+ res5 + "\""
                + ",\"R6\":\""+ res6 + "\""
                + ",\"R7\":\""+ res7 + "\""
                + ",\"R8\":\""+ res8 + "\""
                + ",\"R9\":\""+ res9 + "\""
                + ",\"R10\":\""+ res10 + "\""
                + ",\"PuntajeJuego\":\""+ puntajeJuego +"\""
                + "}";

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        message.setRetained(false);
        mqttClient.publish(topic, message);

        restartMqttTimeoutTimer(); // Reiniciar el temporizador de timeout, se utiliza para revisar si se estan enviando mensajes MQTT cada X time
    } catch (MqttException e) {
        showToast("Error publishing message: " + e.getMessage());
    }
}
  @Subscribe
  public void onDataUpdate(SampleData data) {
    if (!data.getDeviceID().equals(mDevice.getId())) {
        return;
    }
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            mDataLogView.updateLog(data.toSimpleString());
            if (isSendingMQTT && mqttClient.isConnected()) {
                sendMQTTMessage(data);

            }
        }
    });
  }

  @Subscribe
  public void onEteResult(DeviceETEManager.DeviceETEResult deviceETEResult) {
    if (!deviceETEResult.device.equals(mDevice)) {
      return;
    }
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mDataLogView.updateLog("flash = " + deviceETEResult.flash + "\n"
            + deviceETEResult.result.toString());
      }
    });
  }

  @Subscribe
  public void onBatteryEvent(DeviceManager.BatteryData batteryData) {
    if (batteryData.device.equals(mDevice)) {
        lastBatteryLevel = batteryData.batteryInfo.getPercent(); // Level is posible too
        if (batteryData.batteryInfo.needWarming() && batteryData.batteryInfo.getStatus() == BatteryInfo.ChargeStatus.NOT_INCHARGING) {
          mNotificationUtils.sendNotification(mDevice.getName(), getString(R.string.low_battery_warning));
        }
        mTvStatus.setText(batteryData.batteryInfo.getNotifyStr());
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mNotificationUtils = new NotificationUtils(this.getApplicationContext());
    initView();
    
    mBtnCuestionario.setOnClickListener(v -> {
        Intent intent = new Intent(DeviceMenuActivity.this, Cuestionario.class);
        startActivityForResult(intent, 101); // Cambia de pantalla
    });

    solicitarExclusionBateria();

    // Initialize MQTT client
    String Client_ID_MQTT_Sensor = deviceId;
    mqttClient = new MqttAndroidClient(getApplicationContext(), brokerUrl, Client_ID_MQTT_Sensor);

    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession(true);
    options.setUserName(username_mqtt);
    options.setPassword(sasToken.toCharArray());
    options.setSocketFactory(SSLSocketFactory.getDefault());
    
    try {
        mqttClient.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                if(ToasMqtt){
                    Boolean ToasMqtt = false;
                    showToast("Connected to MQTT broker");
                    // Send message to the topic
                    try {
                        String payload = "{\"msg\": \"Start to send-MQTT\", \"Device ID\":\""+mDevice.getId()+"\"}";
                        MqttMessage message = new MqttMessage(payload.getBytes());
                        message.setQos(1);
                        message.setRetained(false);
                        mqttClient.publish(topic, message, null, new IMqttActionListener(){
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                showToast("Message published to topic: " + topic);
                            }
                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                showToast("Failed to publish message: " + exception.getMessage());
                            }
                        });
                    } catch (MqttException e) {
                        showToast("Error publishing message: " + e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                showToast("Failed to connect to MQTT broker: " + exception.getMessage());
            }
        });
    } catch (MqttException e) {
        showToast("Failed to CONNECT to MQTT broker: " + e.getMessage());
        throw new RuntimeException(e);
    }
  }

private void solicitarExclusionBateria() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        String packageName = getPackageName();
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }
}
  private void initView() {
    if (DeviceInfoUtils.isVV310(mDevice)) {
      btnUploadFlash.setVisibility(View.VISIBLE);
      btnCancelUpload.setVisibility(View.VISIBLE);
    } else {
      btnUploadFlash.setVisibility(View.GONE);
      btnCancelUpload.setVisibility(View.GONE);
    }

    mDataLogView = new LogListDialogView();
    mOperationLogView = new LogListDialogView();

    mDataLogView.create(this);
    mOperationLogView.create(this);

    initEngineerModule();
  }

  private void initEngineerModule() {
    try {
      Class.forName(NRF_CONNECT_CLASS);
      btnEngineerModule.setVisibility(View.VISIBLE);
    } catch (ClassNotFoundException e) {
      btnEngineerModule.setVisibility(View.GONE);
    }
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_detail);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mNotificationUtils = null;

      //isSendingMQTT = false;
      // Detener el Foreground Service
      //Intent serviceIntent = new Intent(this, MqttForegroundService.class);
      //stopService(serviceIntent);
      // Cancela el timer de timeout
      //if (mqttTimeoutRunnable != null) {
      //   mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
      //}
  }

  @OnClick(R.id.btnDetail)
  void clickBtnDetail() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.title_todo)
        .setItems(R.array.log_details, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            //DataLog
            if (which == 0) {
              mDataLogView.show();
              //Operation Log
            } else if (which == 1) {
              mOperationLogView.show();
            }
          }
        });
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  //int count = 0;
  //private void requestRTTReadSN() {
  //  count = 0;
  //  CommandRequest readSnFromPatch = new CommandRequest.Builder().setType(CommandAllType.readSnFromPatch).build();
  //  execute(readSnFromPatch, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readSnFromPatch, this);
  //      }else {
  //        count = 0;
  //        requestRTTReadInfo();
  //      }
  //    }
  //  });
  //}
  //
  //private void requestRTTReadInfo() {
  //  CommandRequest readUserInfoFromFlash = new CommandRequest.Builder().setType(CommandAllType.readUserInfoFromFlash).build();
  //  execute(readUserInfoFromFlash, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readUserInfoFromFlash, this);
  //      }else {
  //        count = 0;
  //        requestRTTReadDeviceInfo();
  //      }
  //    }
  //  });
  //}
  //private void requestRTTReadDeviceInfo() {
  //  CommandRequest readDeviceInfo = new CommandRequest.Builder().setType(CommandAllType.readDeviceInfo).build();
  //  execute(readDeviceInfo, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readDeviceInfo, this);
  //      }else {
  //        count = 0;
  //      }
  //    }
  //  });
  //}

  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    showProgressDialog("Disconnecting...");
    isSendingMQTT = false;
    // Detener el Foreground Service
    Intent serviceIntent = new Intent(this, MqttForegroundService.class);
    stopService(serviceIntent);
    // Cancela el timer de timeout
    if (mqttTimeoutRunnable != null) {
        mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
    }
    DeviceManager.getInstance().disconnect(mDevice);

  }

  @OnClick(R.id.btnReadPatchVersion)
  public void clickReadPatchVersion(Button view) {
    execute(CommandType.readPatchVersion, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String hwVersion = (String) data.get("hwVersion");
        String fwVersion = (String) data.get("fwVersion");
        showToast(getString(R.string.device_read_patch_version, hwVersion, fwVersion));
      }
    });
  }

  @OnClick(R.id.btnReadDeviceInfo)
  public void clickReadDeviceInfo(Button view) {
    execute(CommandType.readDeviceInfo, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String magnification = (String) data.get("magnification");
        String samplingFrequency = (String) data.get("ecgSamplingFrequency");
        String model = (String) data.get("model");
        String encryption = (String) data.get("encryption");
        String manufacturer = (String) data.get("manufacturer");
        String info = (String) data.get("info");
        String TroyHR = (String) data.get("hasHR");
        showToast(
            getString(R.string.device_read_device_info, magnification, samplingFrequency, model,
                encryption, manufacturer, info));
      }
    });
  }

  @OnClick(R.id.btnReadSn)
  public void clickReadSN(Button view) {
    execute(CommandType.readSnFromPatch, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String sn = (String) data.get("sn");
        showToast(sn);
      }
    });
  }

  @OnClick(R.id.btnQueryFlash)
  public void clickQueryFlashCount(Button view) {
    execute(CommandType.checkFlashDataStatus, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        long number = (long) data.get("number"); //bytes
        if (data.containsKey("totalNumber") && data.containsKey("seconds")) {
          long totalNumber = (long) data.get("totalNumber"); //bytes
          //unit seconds
          long seconds = (long) data.get("seconds");
          showToast(getString(R.string.flash_info_new, String.valueOf(totalNumber), String.valueOf(number), String.valueOf(seconds)));
        } else {
          showToast(getString(R.string.flash_info_old, String.valueOf(number)));
          showToast(String.valueOf(number));
        }
      }
    });
  }

  @OnClick(R.id.btnCheckPatchStatus)
  public void clickCheckPatchStatus(Button view) {
    execute(CommandType.checkPatchStatus, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        PatchStatusInfo patchStatusInfo = (PatchStatusInfo) data.get("data");
        try {
          InfoDialog.newInstance(mDevice, patchStatusInfo).show(getSupportFragmentManager(), InfoDialog.TAG);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @OnClick(R.id.btnUploadFlash)
  public void clickUploadFlash(Button view) {
    CommandRequest uploadFlashRequest = getCommandRequest(CommandType.uploadFlash, 10 * 1000);
    execute(uploadFlashRequest);
  }

  @OnClick(R.id.btnCancelUpload)
  public void clickCancelUpload(Button view) {
    execute(CommandType.cancelUploadFlash);
  }

  @OnClick(R.id.btnEraseFlash)
  public void clickEraseFlash(Button view) {
    execute(CommandType.eraseFlash);
  }

  @OnClick(R.id.btnStartSampling)
  public void clickStartSampling(Button view) {
    execute(CommandType.startSampling);
    isSendingMQTT = true;
    // Iniciar el Foreground Service
    Intent serviceIntent = new Intent(this, MqttForegroundService.class);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
  }

  @OnClick(R.id.btnStopSampling)
  public void clickStopSampling(Button view) {
    execute(CommandType.stopSampling);
    isSendingMQTT = false;
    // Detener el Foreground Service
    Intent serviceIntent = new Intent(this, MqttForegroundService.class);
    stopService(serviceIntent);
     // Cancela el timer de timeout
    if (mqttTimeoutRunnable != null) {
        mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
    }
  }

  /*@OnClick(R.id.btnShutDown)
  public void clickShutdown(Button view) {
    execute(CommandType.shutdown, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        showProgressDialog("Shutdown...");
      }
    });
  }
*/
  @OnClick(R.id.btnSelfTest)
  public void clickSelfTest(Button view) {
    /*CommandRequest selfTestRequest = getCommandRequest(CommandType.selfTest, 10000);
    execute(selfTestRequest, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        BatteryInfo batteryInfo = (BatteryInfo) data.get("batteryInfo");
        InfoDialog.newInstance(mDevice, batteryInfo)
            .show(getSupportFragmentManager(), InfoDialog.TAG);
      }
    });
    */
  }

  @OnClick(R.id.btnSetPatchClock)
  public void clickSetPatchClock(Button view) {
    //execute(CommandType.setPatchClock);
  }

  @OnClick(R.id.btnReadUserInfo)
  public void clickReadlUserInfo(Button view) {
    execute(CommandType.readUserInfoFromFlash, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String userInfo = (String) data.get("userInfo");
        showToast(userInfo);
      }
    });
  }

  @OnClick(R.id.btnEraseUserInfo)
  public void clickEraseUserInfo(Button view) {
    execute(CommandType.eraseUserInfoFromFlash);
  }

  @OnClick(R.id.btnSetUserInfo)
  public void clickSetUserInfo(Button view) {
    final EditText et = new EditText(this);
    et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
    AlertDialog mUserInfoDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.input_text_hint)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setView(et)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            String input = et.getText().toString();
            if (TextUtils.isEmpty(input)) {
              showToast(R.string.input_text_empty);
            } else {
              CommandRequest setUserInfoRequest = getCommandRequest(CommandType.setUserInfoToFlash,
                  3000, "info", input);
              execute(setUserInfoRequest);
            }
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
    mUserInfoDialog.setCanceledOnTouchOutside(false);
  }

  @OnClick(R.id.btnGraphics)
  void clickBtnGraphics() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.title_todo)
        .setItems(R.array.data_graphics, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            //RTS
            if (which == 0) {
              navToConnectedActivity(mDevice, MotionGraphicActivity.class);
              //History
            } else if (which == 1) {
              navToConnectedActivity(mDevice, HistoryActivity.class);
            }
          }
        });
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  @OnClick(R.id.btnClearDatabase)
  public void clickClearDatabase() {
    DatabaseManager.getInstance().getDataDAO().deleteAll();
    showToast("delete all sample data success!");
  }

  @OnClick(R.id.btnExportMIT16)
  public void clickExportMIT16() {
    Observable.create(new ObservableOnSubscribe<Object>() {
      @Override
      public void subscribe(ObservableEmitter<Object> emitter) throws Exception {

        String timeStr = (System.currentTimeMillis() / 1000) + "";
        String name = mDevice.getSn().replace('/', '_');
        String heaFile = FileManager.getFileDataPath(mDevice.getSn(), name + "_" + timeStr);
        String dataFile = FileManager.getFileDataPath(mDevice.getSn(), name + "_" + timeStr + ".dat");

        List<com.vivalnk.sdk.demo.repository.database.VitalData>
            data = DatabaseManager.getInstance().getDataDAO().queryAllOrderByTimeASC(mDevice.getId());

        if (data.size() <= 0) {
          emitter.onError(new DataEmptyExeption("empty database"));
          return;
        }

        com.vivalnk.sdk.demo.repository.database.VitalData firstdata = data.get(0);

        WfdbUtils.initFile(dataFile, heaFile);

        WfdbUtils.initSignalInfo(
            firstdata.getECG().length,
            16,
            "sample data",
            "mV",
            DeviceInfoUtils.getMagnification(mDevice),
            0,
            0);

        WfdbUtils.open();

        String time = DateFormat.format(firstdata.getTime(), "HH:mm:ss yyyy/MM/dd");
        WfdbUtils.setBaseTime(time);

        VitalData preData = data.get(0);
        WfdbUtils.doSample(preData.getECG());

        for (int i = 1; i < data.size(); i++) {

          long deltaTime = data.get(i).time - preData.getTime();
          if (deltaTime >= 2000) {
            //there data missing, should fill by default zero value
            long delta = deltaTime / 1000 - 1;
            for (int j = 0; j < delta; j++) {
              WfdbUtils.doSample(new int[preData.getECG().length]);
            }
          }

          VitalData dataI = data.get(i);
          int[] ecg = dataI.getECG();
          WfdbUtils.doSample(ecg);
          preData = dataI;
        }

        WfdbUtils.newHeader();

        WfdbUtils.close();

        emitter.onNext(new Object());
        emitter.onComplete();
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Object>() {
          @Override
          public void onSubscribe(Disposable d) {
            showProgressDialog("processing...");
          }

          @Override
          public void onNext(Object o) {

          }

          @Override
          public void onError(Throwable e) {
            showToast(e.getMessage());
            dismissProgressDialog();
          }

          @Override
          public void onComplete() {
            showToast("process complete, please see the data file");
            dismissProgressDialog();
          }
        });
  }


  private static final int OTA_RET_CODE = 2019;
  private static final int ACTIVITY_CHOOSE_FILE = 3;
  @OnClick(R.id.btnOTA)
  public void clickOTA() {
    openFileSelector();
  }

  private void openFileSelector() {
    Intent intent = new Intent(this, FilePickerActivity.class);
    intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("(VV|vv|BLACK_GOLD).*(_FW_|_BL_|project)+.*\\.zip"));
    startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 101 && resultCode == RESULT_OK) {
        nombre = data.getStringExtra("Nombre");
        res1 = data.getStringExtra("respuesta1");
        res2 = data.getStringExtra("respuesta2");
        res3 = data.getStringExtra("respuesta3");
        res4 = data.getStringExtra("respuesta4");
        res5 = data.getStringExtra("respuesta5");
        res6 = data.getStringExtra("respuesta6");
        res7 = data.getStringExtra("respuesta7");
        res8 = data.getStringExtra("respuesta8");
        res9 = data.getStringExtra("respuesta9");
        res10 = data.getStringExtra("respuesta10");
        puntajeJuego = data.getStringExtra("puntajeJuego");
        //estado = data.getStringExtra("respuestaTexto");
    }
    /*if (requestCode == ACTIVITY_CHOOSE_FILE) {
      if (resultCode != RESULT_OK || data == null) {
        isOTAing = false;
      } else {
        String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        startActivityForResult(OTAActivity.newIntent(this, mDevice, filePath), OTA_RET_CODE);
        isOTAing = true;
      }
    } else if (requestCode == OTA_RET_CODE) {
      isOTAing = false;
    } else {
      finish();
    }*/
  }

  @OnClick(R.id.btnEngineerModule)
  public void openEngineerModule() {
    try {
      // look for engineer Activity
      Intent engineerActivity = new Intent(this, Class.forName(NRF_CONNECT_CLASS));
      engineerActivity.putExtra("device", mDevice);
      startActivity(engineerActivity);
    } catch (final Exception e) {
      showToast(R.string.error_no_support_engineer_module);
    }
  }

  public void execute(final CommandRequest request, final Callback callback) {
    super.execute(request, new Callback() {
      @Override
      public void onStart() {
        String log = request.getTypeName() + ": onStart";
        mOperationLogView.updateLog(log);
        if (null != callback) {
          callback.onStart();
        }
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        String log = request.getTypeName() + ": " + (data != null ? "onComplete: data = " + data : "onComplete");
        mOperationLogView.updateLog(log);
        if (null != callback) {
          callback.onComplete(data);
        }
      }

      @Override
      public void onError(int code, String msg) {
        String log = request.getTypeName() + ": " + "onError: code = " + code + ", msg = " + msg;
        mOperationLogView.updateLog(log);
        if (null != callback) {
          callback.onError(code, msg);
        }
      }
    });
  }
}
