package com.vivalnk.sdk.demo.vital.ui;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.DeviceModel;

public class MotionGraphicActivity extends ConnectedActivity {

  private static final String TAG = "MotionGraphicActivity";

  NavController controller;
  Bundle extras;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
    initView();
  }

  private void init() {

  }

  private void initView() {
    controller = Navigation.findNavController(this, R.id.nav_host_fragment);

    extras = new Bundle();
    extras.putSerializable("device", mDevice);

    controller.setGraph(R.navigation.nav_graph, extras);
    //controller.navigate(R.id.ecgFragment);
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_graphic);
  }


  @Override
  protected void onStop() {
    super.onStop();
    DeviceManager.getInstance().openHistoryData(mDevice);
    DeviceManager.getInstance().closeRTSData(mDevice);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (mDevice.getModel() != DeviceModel.Checkme_O2) {
      DeviceManager.getInstance().closeHistoryData(mDevice);
      DeviceManager.getInstance().openRTSData(mDevice);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.graphics, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_switch_view) {
      NavDestination destination = controller.getCurrentDestination();
      int currID = destination.getId();
      if (currID == R.id.ecgFragment) {
        NavOptions navOptions = new NavOptions.Builder().setLaunchSingleTop(true).build();
        controller.navigate(R.id.accFragment, extras, navOptions);
      } else if (currID == R.id.accFragment) {
        NavOptions navOptions = new NavOptions.Builder().setLaunchSingleTop(true).build();
        controller.navigate(R.id.ecgFragment, extras, navOptions);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    finish();
    return;
  }
}