package com.vivalnk.sdk.demo.vital.ble;


public class BluetoothConsts {


  /**
   * Found device
   */
  public static final int MESSAGE_FLAG_ON_ECG_DEVICE_FOUND = 10001;

  // Mode zero data
  public static final int MESSAGE_FLAG_ON_ECGACC_Zero_DATA_UPDATED = 10002;

  //Mode one data
  public static final int MESSAGE_FLAG_ON_RRIACC_One_DATA_UPDATED = 10003;

  //Mode two data
  public static final int MESSAGE_FLAG_ON_RRIECG_Two_DATA_UPDATED = 10016;

  //Mode three data
  public static final int MESSAGE_FLAG_ON_RRI_Three_DATA_UPDATED = 10004;

  public static final int MESSAGE_FLAG_ON_DATA_UPDATED = 20005;

  //RWL data updated in mode one
  public static final int MESSSAGE_FALG_ON_RWL_DATA_UPDATED = 10017;


  public static final int MESSAGE_ETE_DATA_UPDATED = 10021;

  // device connected
  public static final int MESSAGE_FLAG_ON_ECG_CONNECTED = 10005;

  //device connnected
  public static final int MESSAGE_FLAG_ON_ECG_DISCONNECTED = 10006;

  // device rssi updated
  public static final int MESSAGE_FLAG_ON_ECG_REMOTE_RSSI_UPDATED = 10007;


  // filterd ecg data updated
  public static final int MESSAGE_FLAG_ON_CALCULATED_ECG_DATA_UPDATED = 10009;

  // date info and lead on/off status
  public static final int MESSAGE_FLAG_ON_DATE_ONOFF_STATUS_UPDATED = 10010;

  //battery status
  public static final int MESSAGE_FLAG_ON_BATTERY_STATUS_UPDATED = 10011;

  //selftest word and battery status
  public static final int MESSAGE_FLAG_ON_SELFTEST_WORD_AND_BATTERY_SATTUS = 10012;

  //Ack commands string
  public static final int MESSAGE_FLAG_ON_ACK_COMMANDS_STRING_UPDATED = 10013;

  //fw version and hw version
  public static final int MESSAGE_FLAG_ON_VERSION_INFO = 10014;

  //the flash quantity number value updated
  public static final int MESSAGE_FLAG_ON_FLASH_QUANTITY_NUMBER_UPDATED = 10015;

  // user info
  public static final int MESSAGE_FLAG_ON_FLASH_USER_INFO_UPDATED = 10018;

  //serial info
  public static final int MESSAGE_FLAG_ON_SN_INFO_UPDATED = 10019;


  public static final int MESSAGE_FLAG_ON_FLASH_UPLOAD_FINISH = 10020;

  public static final int MESSAGE_FLAG_ON_UPDATE_NAME = 10021;

}
