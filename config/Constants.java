package com.vivalnk.sdk.demo.vital.config;

import android.os.Environment;

/**
 * Created by JakeMo on 18-8-1.
 */
public class Constants {

  public static final String ROOT_DIR =
      Environment.getExternalStorageDirectory().getPath() + "/PL_DATA/finaltest";
  public static final String OTAPath = ROOT_DIR + "/OTA";
  public static final String fwPath = OTAPath + "/FW";
  public static final String blPath = OTAPath + "/BL";
  public static final String PREF_NAME = "vivalnk_finaltest_sp";

  public static final String FG_Operation_Name = "VS_PL_FG_Operation.txt";
  public static final String FG_Operation = ROOT_DIR + "/" + FG_Operation_Name;
  public static final String FG_ECG_Name = "VS_PL_FG_ECG.txt";
  public static final String FG_ECG = ROOT_DIR + "/" + FG_ECG_Name;
  public static final String FG_ACC_Name = "VS_PL_FG_ACC.txt";
  public static final String FG_ACC = ROOT_DIR + "/" + FG_ACC_Name;

  public static final String BACKUP_FOLDER = ROOT_DIR + "/Backup";

  public static final String GoldenECGFolder = ROOT_DIR + "/" + "GoldenECG";
  public static final String ECGFolder = ROOT_DIR + "/" + "TestECG";

  public static final int startSamplingStableTime = 6 * 1000;

}