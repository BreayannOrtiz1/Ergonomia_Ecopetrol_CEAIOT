package com.vivalnk.sdk.demo.vital.ui;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.widget.DragEcgView;
import com.vivalnk.sdk.demo.base.widget.EcgPoint;
import com.vivalnk.sdk.demo.repository.database.DatabaseManager;
import com.vivalnk.sdk.demo.repository.database.VitalData;
import com.vivalnk.sdk.common.utils.ListUtils;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.utils.DateFormat;
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
//import io.reactivex.rxjava3.core.Single;
//import io.reactivex.rxjava3.core.SingleObserver;
//import io.reactivex.rxjava3.core.SingleOnSubscribe;
//import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

public class HistoryActivity extends ConnectedActivity {

  private static final String TAG = "MotionGraphicActivity";

  @BindView(R.id.dragEcgView)
  DragEcgView dragEcgView;

  @BindView(R.id.btnTimePicker)
  Button btnTimePicker;

  @BindView(R.id.btnSwitchGain)
  Button btnSwitchGain;

  @BindView(R.id.btnRevert)
  Button btnRevert;

  //@BindView(R.id.btnNoisy)
  //Button btnNoisy;

  private boolean loaded = false;

  private List<VitalData> rawDataList;

  private float timePerPoint;

  //private volatile boolean denoisy;
  private volatile boolean revert;

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //btnNoisy.setText(denoisy ? "Close Denoising" : "Open Denoising");
    btnRevert.setText(revert ? "De-Revert" : "Revert");

  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_history);
  }


  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    if (null != mDisposable && !mDisposable.isDisposed()) {
      mDisposable.dispose();
    }
    super.onDestroy();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (!loaded) {
      getHistoryData(System.currentTimeMillis() - 1000 * 60 * 60 * 1, System.currentTimeMillis());
      loaded = true;
    }
  }

  Disposable mDisposable;
  public void getHistoryData(long startTime, long endTime) {

    final String startDate = DateFormatUtils.format(startTime, DateFormat.sPattern);
    final String endDate = DateFormatUtils.format(endTime, DateFormat.sPattern);

    showProgressDialog();

    Single.create((SingleOnSubscribe<List<VitalData>>) emitter -> {
      List<VitalData> source = DatabaseManager.getInstance()
          .getDataDAO()
          .query(mDevice.getId(), startTime, endTime);
      if (source == null) {
        source = new ArrayList<>();
      } else {
        VitalLog.e("empty data list");
      }
      rawDataList = source;
      showEcgViewList(rawDataList);
      emitter.onSuccess(source);
    })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<List<VitalData>>() {
          @Override
          public void onSubscribe(Disposable d) {
            mDisposable = d;
          }

          @Override
          public void onSuccess(List<VitalData> data) {
            dismissProgressDialog();
            if (data.size() <= 0) {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  showToast("There is no data between " + startDate + " - " + endDate);
                }
              });
            }
          }

          @Override
          public void onError(Throwable e) {
            dismissProgressDialog();
            e.printStackTrace();
          }
        });
  }

  private void showEcgViewList(List<VitalData> dataList) {

    if (ListUtils.isEmpty(dataList)) {
      return;
    }

    int sampleRate = dataList.get(0).getECG().length;
    System.out.println("sampleRate = " + sampleRate);
    dragEcgView.setSampleRate(sampleRate);
    timePerPoint = 1040f / sampleRate;

    ArrayList<EcgPoint> ecgPoints = new ArrayList<>();
    for (int i = 0; i < dataList.size(); i++) {
      VitalData data = dataList.get(i);
      ecgPoints.addAll(getEcgList(data.time, data.getECG(), data.getMagnification()));
    }

    if (ecgPoints.size() > 0) {
      dragEcgView.clearDataList();
      dragEcgView.addEcgPointList(ecgPoints);
    }

  }

  public ArrayList<EcgPoint> getEcgList(long startTime, int[] ecg, int magnification) {
    int color = getResources().getColor(R.color.black);
    ArrayList<EcgPoint> points = new ArrayList<>();
    for (int i = 0; i < ecg.length; i++) {
      EcgPoint ecgPoint = new EcgPoint();
      ecgPoint.time = (long) (startTime * 1.0D + timePerPoint * i);
      ecgPoint.mv = ecg[i] / (magnification * 1.0F);
      ecgPoint.color = color;
      points.add(ecgPoint);
    }
    return points;
  }

  @OnClick(R.id.btnRevert)
  public void clickBtnRevert() {
    revert = !revert;
    dragEcgView.revert(revert);
    btnRevert.setText(revert ? "De-Revert" : "Revert");
  }

  private Calendar currentDate = Calendar.getInstance();
  @OnClick(R.id.btnTimePicker)
  public void clickTimePicker() {
    //时间选择器
    TimePickerView pvTime = new TimePickerBuilder(HistoryActivity.this,
        new OnTimeSelectListener() {
          @Override
          public void onTimeSelect(final Date date, View v) {
            currentDate.setTime(date);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                showToast(DateFormatUtils.format(date, DateFormat.sPattern));
              }
            });
            getHistoryData(date.getTime() - 1000 * 60 * 60 * 1, date.getTime());
          }
        })
        .setDecorView(getWindow().getDecorView().findViewById(android.R.id.content))
        .setType(new boolean[]{true, true, true, true, true, true})
        .build();
    pvTime.setKeyBackCancelable(true);
    pvTime.setDate(currentDate);
    pvTime.show();
  }

  @OnClick(R.id.btnSwitchGain)
  public void clickSwitchGain() {
    dragEcgView.switchGain();
  }



}
