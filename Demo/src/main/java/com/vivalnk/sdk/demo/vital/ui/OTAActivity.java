package com.vivalnk.sdk.demo.vital.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import android.view.WindowManager;
import android.widget.TextView;
import butterknife.BindView;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.demo.base.app.BaseToolbarActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.ble.ota.OTAListener;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import java.io.File;

/**
 * OTA
 *
 * @author Aslan
 * @date 2019/3/15
 */
public class OTAActivity extends BaseToolbarActivity {

  private static final String TAG_DEVICE = "device";
  private static final String TAG_FILE_PATH = "FILE_PATH";

  public static Intent newIntent(Context context, Device device, String filePath) {
    Intent intent = new Intent(context, OTAActivity.class);
    intent.putExtra(TAG_DEVICE, device);
    intent.putExtra(TAG_FILE_PATH, filePath);
    return intent;
  }

  private Device device;
  private String filePath;

  @BindView(R.id.tv)
  TextView tv;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_ota);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    acquireLock();
    device = (Device) getIntent().getSerializableExtra(TAG_DEVICE);
    filePath = getIntent().getStringExtra(TAG_FILE_PATH);
    startOTA(filePath, device);
  }

  PowerManager.WakeLock wakeLock;
  private void acquireLock() {
    try {
      PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
          "MyApp::MyWakelockTag");
      wakeLock.acquire(60 * 1000);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    releaseLock();
    super.onDestroy();
  }

  private void releaseLock() {
    try {
      if (wakeLock != null) {
        wakeLock.release();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void startOTA(String filePath, Device mDevice) {
    VitalClient.getInstance().startOTA(mDevice, new File(filePath),
        new OTAListener() {
          @Override
          public void onStart(Device device) {
            tv.setText(R.string.ota_start);
          }

          @Override
          public void onProgressChanged(Device device, int percent) {
            tv.setText(getString(R.string.ota_process, percent));
          }

          @Override
          public void onComplete(Device device) {
            showToast(R.string.ota_completed);

            finishOTA();
          }

          @Override
          public void onCancel(Device device) {
            tv.setText(R.string.ota_completed);

            finishOTA();
          }

          @Override
          public void onError(Device device, int code, String msg) {
            tv.setText(getString(R.string.ota_error, code, msg));

            showAlertDialog(getResources().getString(R.string.ota_error_title),
                String.format("code:%d msg:%s", code, msg)
                , new OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    finishOTA();
                    dialog.dismiss();
                  }
                }, new OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    finishOTA();
                    dialog.dismiss();
                  }
                });

          }
        });
  }

  private void finishOTA() {
    setResult(RESULT_OK);
    OTAActivity.this.finish();
  }

  @Override
  public void onBackPressed() {

  }
}
