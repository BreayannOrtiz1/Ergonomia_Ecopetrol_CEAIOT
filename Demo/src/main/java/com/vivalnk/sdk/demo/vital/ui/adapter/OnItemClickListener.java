package com.vivalnk.sdk.demo.vital.ui.adapter;

import android.view.View;
import com.vivalnk.sdk.model.Device;

/**
 * Created by JakeMo on 18-5-2.
 */
public interface OnItemClickListener {
  void onItemClick(View itemView, int position, Device device);
}
