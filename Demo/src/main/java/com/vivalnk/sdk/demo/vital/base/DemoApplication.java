package com.vivalnk.sdk.demo.vital.base;

import android.content.Context;
import androidx.multidex.MultiDex;
import android.text.TextUtils;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.demo.base.app.BaseApplication;
import com.vivalnk.sdk.demo.base.utils.SPUtils;
import com.vivalnk.sdk.common.exception.VivalnkCrashHandler;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Created by JakeMo on 18-1-30.
 */

public class DemoApplication extends BaseApplication {

  private static String APPID = "9b29392c19";

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SPUtils.getInstance();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }

    if(com.vivalnk.sdk.demo.vital.BuildConfig.openLeakCanary){
      LeakCanary.install(this);
      // Normal app init code...
    }

    DeviceManager.getInstance().init(this);
    VitalClient.getInstance().openLog();
    VitalClient.getInstance().allowWriteToFile(true);

    DemoApplication.initBugly(this.getApplicationContext());

  }

  public static void initBugly(Context context) {
    CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
    strategy.setAppVersion(com.vivalnk.sdk.demo.vital.BuildConfig.VERSION_NAME);
    strategy.setAppPackageName(context.getPackageName());
    // 获取当前包名
    String packageName = context.getPackageName();
    // 获取当前进程名
    String processName = getProcessName(android.os.Process.myPid());
    strategy.setUploadProcess(processName == null || processName.equals(packageName));
    strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
      @Override
      public synchronized Map<String, String> onCrashHandleStart(int i, String s, String s1,
          String s2) {
        return VivalnkCrashHandler.getInstance().collectDeviceInfosMap();
      }
    });

    //设置测试设备
    CrashReport.setIsDevelopmentDevice(context, true);

    CrashReport.putUserData(context, "appPackageName", context.getPackageName());
    CrashReport.putUserData(context, "appVersionName", com.vivalnk.sdk.demo.vital.BuildConfig.VERSION_NAME);
    CrashReport.putUserData(context, "appVersionCode", "" + com.vivalnk.sdk.demo.vital.BuildConfig.VERSION_CODE);
    CrashReport.putUserData(context, "sdkVersionName", com.vivalnk.sdk.BuildConfig.VERSION_NAME);
    CrashReport.putUserData(context, "sdkVersionCode", "" + com.vivalnk.sdk.BuildConfig.VERSION_CODE);
    CrashReport.putUserData(context, "sdkCommonName", "" + com.vivalnk.sdk.common.BuildConfig.VERSION_NAME);
    CrashReport.putUserData(context, "sdkCommonCode", "" + com.vivalnk.sdk.common.BuildConfig.VERSION_CODE);
    CrashReport.initCrashReport(context, APPID, true, strategy);
  }

  private static String getProcessName(int pid) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
      String processName = reader.readLine();
      if (!TextUtils.isEmpty(processName)) {
        processName = processName.trim();
      }
      return processName;
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
    return null;
  }

}
