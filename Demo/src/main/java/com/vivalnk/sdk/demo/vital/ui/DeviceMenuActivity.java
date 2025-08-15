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
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
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
    @BindView(R.id.tvLastFormTime) TextView tvLastFormTime;
    @BindView(R.id.tvMqttIndicatorDot) TextView tvMqttIndicatorDot;
    @BindView(R.id.tvMqttIndicatorDetail) TextView tvMqttIndicatorDetail;
    private static final int REQ_QUESTIONNAIRE = 2001;
    private long lastFormSavedAt = 0L;
    private long lastMqttAckAt = 0L;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable mqttIndicatorTicker = new Runnable() {
        @Override public void run() {
            updateMqttIndicator();
            uiHandler.postDelayed(this, 1000);
        }
    };
  @BindView(R.id.btnStartSampling)
  Button btnStartSampling;
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
  // ECG_C740209, ECG_C740200, ECG_C740211
  String deviceId_SN_G = "ECG_C740209"; // Extraido de azure IoT Explorer.
  String brokerUrl = "ssl://" + iotHubName + ".azure-devices.net:8883";
  String topic = "devices/" + deviceId_SN_G + "/messages/events/";
  String username_mqtt = iotHubName + ".azure-devices.net/" + deviceId_SN_G + "/api-version=2018-06-30";

  // SAS Token CORRECTO (sin doble SharedAccessSignature). Hasta Enero 2026:
  // C740200:
  //String sasToken = "SharedAccessSignature sr=ingenieriaiothub.azure-devices.net%2Fdevices%2FECG_C740200&sig=JWjM9d6wGqA7QxXX5WvpDvz1WXt2lDP3dfVyYPw68O8%3D&se=1772352730";
  // C740211:
  //String sasToken = "SharedAccessSignature sr=ingenieriaiothub.azure-devices.net%2Fdevices%2FECG_C740211&sig=SAALVClspdlhy7Pkotfe6ujCQroto9SD1aeXRdwpAaQ%3D&se=1772353766";
  //  C740209:
  //String sasToken = "SharedAccessSignature sr=ingenieriaiothub.azure-devices.net%2Fdevices%2FECG_C740209&sig=PHLD52rRxCypPeTxoEzLqUc8s77yHZkjeATJnUM5n5Y%3D&se=1791136107";
  String sasToken = "SharedAccessSignature sr=ingenieriaiothub.azure-devices.net%2Fdevices%2FECG_C740209&sig=PHLD52rRxCypPeTxoEzLqUc8s77yHZkjeATJnUM5n5Y%3D&se=1791136107";
  private boolean sendMQTT = false;
  private MqttAndroidClient mqttClient;
  private final MqttConnectOptions options = new MqttConnectOptions();


  // Control Variables
  private Integer lastBatteryLevel = null;
  private static final long MQTT_SEND_TIMEOUT_MS = 30_000; // 30 segundos, Alerta problema de transmisión.
  private Handler mqttTimeoutHandler = new Handler(Looper.getMainLooper());
  private Runnable mqttTimeoutRunnable;
  private int mqttRetryCount = 0;
  private static final int MAX_RETRIES = 5;
  // Variable de control para evitar enviar duplicados. Al parecer onDataupdate sellama 2 veces por mensaje.
  private long lastSentTimestamp = -1;

  // Questionnaire
  String nombre = " ";
  String edad = " ";
  String sexo = " ";
  String cargo = "";
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
  String resA=""/*, resB="", resC=""*/;
  ArrayList<String> factoresSeleccionados;
  String factoresSeleccionadosJson = null;

  private void updateLastFormTime() {
    if (tvLastFormTime == null) return;
    if (lastFormSavedAt <= 0) {
        tvLastFormTime.setText("Aún no se ha enviado el formulario: —");
        return;
    }
    java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
    tvLastFormTime.setText("Último formulario enviado: " + df.format(new java.util.Date(lastFormSavedAt)));
  }
  private void updateMqttIndicator() {
    if (tvMqttIndicatorDot == null || tvMqttIndicatorDetail == null) return;

    boolean connected = (mqttClient != null && mqttClient.isConnected());
    boolean sending = sendMQTT; // ya existe en tu clase
    long now = System.currentTimeMillis();
    boolean ackRecent = (lastMqttAckAt > 0) && (now - lastMqttAckAt < 5000); // ventana 5s

    if (!connected) {
        setDot("#9E9E9E"); // gris
        tvMqttIndicatorDetail.setText("MQTT: desconectado");
        return;
    }
    if (sending && ackRecent) {
        setDot("#2E7D32"); // verde
        tvMqttIndicatorDetail.setText("MQTT: enviando (OK)");
    } else if (sending) {
        setDot("#F9A825"); // amarillo
        tvMqttIndicatorDetail.setText("MQTT: sin ACK reciente");
    } else {
        setDot("#1565C0"); // azul
        tvMqttIndicatorDetail.setText("MQTT: conectado");
    }
  }

  private void setDot(String colorHex) {
        tvMqttIndicatorDot.setTextColor(android.graphics.Color.parseColor(colorHex));
  }

  @Override protected void onResume() {
        super.onResume();
        uiHandler.post(mqttIndicatorTicker);
        updateLastFormTime();
        updateMqttIndicator();
  }
  @Override protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(mqttIndicatorTicker);
  }

    @Override protected void onStop() {
        super.onStop();
        // cancela timeout al salir de foreground
        if (mqttTimeoutRunnable != null) {
            mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
        }
    }

  private void restartMqttTimeoutTimer() {
    if (mqttTimeoutRunnable != null) {
        mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
    }
    mqttTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // Notificación de alerta por timeout
            if(mNotificationUtils != null && !isFinishing()){
                mNotificationUtils.sendNotification(
                        "Alerta problema de transmisión",
                        "No se están enviando datos por MQTT. Verifica la conexión o el sensor."
                );
            }

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
        String deviceSN = data.getDeviceSN();// mDevicesn. Asumiendo que mDevice está disponible
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
        float[] ecg = data.getEcgInMillivolt(); // 128 datos
        StringBuilder ecgArray = new StringBuilder("[");
        if (ecg != null) {
            for (int i = 0; i < ecg.length; i++) {
                ecgArray.append(ecg[i]);
                if (i < ecg.length - 1) ecgArray.append(",");
            }
        }
        // 14. RRi [ms]
        int[] RRi = data.getRRI();
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
        if (RRi == null) RRi = new int[]{0, 0, 0, 0};
        if (nombre == null) nombre = "N/A";
        if (edad == null) edad = "N/A";
        if (sexo == null) sexo = "N/A";
        if (cargo == null) cargo = "N/A";
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
        if (resA == null) resA = "N/A";
        /*if (resB == null) resB = "N/A";
        if (resC == null) resC = "N/A";*/
        if (factoresSeleccionados != null && !factoresSeleccionados.isEmpty()) {
            // Poner comillas a cada elemento
          ArrayList<String> quoted = new ArrayList<>();
          for (String s : factoresSeleccionados) {
            quoted.add("\"" + s + "\"");
          }
          factoresSeleccionadosJson = "[" + TextUtils.join(",", quoted) + "]";
        } else {
            factoresSeleccionadosJson = "[]";
            Log.d("MQTT", "No se seleccionaron factores.");
        }
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
                + ",\"RRi\":" + Arrays.toString(RRi).replace(" ", "")
                + ",\"Nombre\":\"" + nombre + "\""
                + ",\"Edad\":\""+ edad + "\""
                + ",\"Sexo\":\""+ sexo + "\""
                + ",\"Cargo\":\""+ cargo + "\""
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
                + ",\"RA\":\""+ resA + "\""
                /*+ ",\"RB\":\""+ resB + "\""
                + ",\"RC\":\""+ resC + "\""*/
                + ",\"PuntajeJuego\":\""+ puntajeJuego +"\""
                + ",\"FactoresSelecionados\":" + factoresSeleccionadosJson
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
  private void connectMQTT(MqttAndroidClient mqttClient, MqttConnectOptions options, String topic, String deviceSN) {
        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    showToast("Connected to MQTT broker");
                    String timeStr = String.valueOf(System.currentTimeMillis() / 1000);     //// IMPORTANTE cambiar calve, no espacios {Device_ID}
                    String payload = "{\"TimeStamp\": \""+ timeStr +"\",\"msg\": \"Start to send-MQTT\", \"Device_SN\":\""+deviceId_SN_G+"\"}"; // deviceId_SN_G es una variable global, definida al inicio de la clase, no se lee directamente del dispositivo, esto se hace para saber si la configuracion del dispositivo corresponde al sensor. Ya que la trama que se envia lee directamente el SN del sensor, podria ser diferente

                    MqttMessage message = new MqttMessage(payload.getBytes());
                    message.setQos(1);      //REvisar si sepuede utilizar QoS 0, evitar duplicados pero se pueden perder datos
                    message.setRetained(false);
                    // Send message to the topic
                    try {
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
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    mqttRetryCount++;
                    showToast("MQTT connection failed. Attempt " + mqttRetryCount + "/" + MAX_RETRIES);

                    if (mqttRetryCount < MAX_RETRIES) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            connectMQTT(mqttClient, options, topic, deviceSN);
                        }, 3000); // espera 3 segundos antes del próximo intento
                    } else {
                        showToast("Could not connect to MQTT broker after " + MAX_RETRIES + " attempts.");
                    }
                }
            });
        } catch (MqttException e) {
            showToast("Failed to CONNECT to MQTT broker: " + e.getMessage());
            //throw new RuntimeException(e);
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
            if (sendMQTT && mqttClient.isConnected() && mqttClient != null) {
                long ts = data.getTime();
                if(ts != lastSentTimestamp){
                    lastSentTimestamp = ts;
                    sendMQTTMessage(data);
                }

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
    execute(CommandType.eraseFlash);
    execute(CommandType.eraseUserInfoFromFlash);
    solicitarExclusionBateria();

    if (mDevice == null) {
      showToast("Error: mDevice es null. Se requiere reiniciar la app.");
      finish(); // Cierra la actividad de forma segura
      return;
    }
    mBtnCuestionario.setOnClickListener(v -> {
        Intent intent = new Intent(DeviceMenuActivity.this, Cuestionario.class);
        startActivityForResult(intent, 101); // Cambia de pantalla
    });

    // Initialize MQTT client
    if (mqttClient == null) {
      mqttClient = new MqttAndroidClient(getApplicationContext(), brokerUrl, deviceId_SN_G);
    }
    mqttClient.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectComplete(boolean reconnect, String serverURI) {
          showToast("MQTT Connected" + (reconnect ? " (Reconnected)" : ""));
          btnStartSampling.setVisibility((View.VISIBLE));
          lastMqttAckAt = System.currentTimeMillis(); // conectado
          updateMqttIndicator();
      }

      @Override
      public void connectionLost(Throwable cause) {
          showToast("MQTT Disconnected: " + cause.getMessage());
          mqttRetryCount = 0; // reinicia el contador
          if (!sendMQTT || isFinishing() || isDestroyed()) {
              return;
          }
          connectMQTT(mqttClient, options, topic, deviceId_SN_G); // reintento inmediato
          lastMqttAckAt = 0L;
          updateMqttIndicator();
          //execute(CommandType.stopSampling);
          //btnStartSampling.setVisibility((View.INVISIBLE));
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) {
          // mensaje recibido
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
          // mensaje entregado
          lastMqttAckAt = System.currentTimeMillis(); // publish confirmado
          updateMqttIndicator();
      }
    });     // Maneja re-conexion.
    options.setCleanSession(true);
    options.setUserName(username_mqtt);
    options.setPassword(sasToken.toCharArray());
    options.setSocketFactory(SSLSocketFactory.getDefault());
    connectMQTT(mqttClient,options, topic, mDevice.getSn());    // Maneja re-intentos.
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
  private void initView(){
    btnStartSampling.setVisibility((View.INVISIBLE));
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
  private void safelyCloseMqttClient() {
    if (mqttClient != null) {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.close();
            mqttClient = null; // para evitar dobles intentos
        } catch (MqttException e) {
            Log.e("MQTT", "Error closing client", e);
        }
    }
  }
  @Override
  protected void onDestroy() {
      // Detener el Foreground Service
      Intent serviceIntent = new Intent(this, MqttForegroundService.class);
      stopService(serviceIntent);
      safelyCloseMqttClient();
      if (mqttTimeoutRunnable != null) {
          mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
      }
    super.onDestroy();
    mNotificationUtils = null;
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


  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    showProgressDialog("Disconnecting...");
    sendMQTT = false;
      // cancela timeout si estaba armado
      if (mqttTimeoutRunnable != null) {
          mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
      }
      // service si estaba activo
      Intent serviceIntent = new Intent(this, MqttForegroundService.class);
      stopService(serviceIntent);
      safelyCloseMqttClient();
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
    sendMQTT = true;
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
    sendMQTT = false;
     // Cancela el timer de timeout
    if (mqttTimeoutRunnable != null) {
        mqttTimeoutHandler.removeCallbacks(mqttTimeoutRunnable);
    }
    execute(CommandType.stopSampling);
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
        assert data != null;
        nombre = data.getStringExtra("Nombre");
        edad = data.getStringExtra("Edad");
        sexo = data.getStringExtra("Sexo");
        cargo = data.getStringExtra("Cargo");
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
        resA = data.getStringExtra("respuestaA");
        //resB = data.getStringExtra("respuestaB");
        //resC = data.getStringExtra("respuestaC");
        factoresSeleccionados = data.getStringArrayListExtra("FactoresSeleccionados");
        lastFormSavedAt = data.getLongExtra("saveAt", System.currentTimeMillis());
        updateLastFormTime();
    }
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
