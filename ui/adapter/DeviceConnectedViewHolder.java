package com.vivalnk.sdk.demo.vital.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceInfoUtils;

/**
 * Created by JakeMo on 18-5-2.
 */
public class DeviceConnectedViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.tvDeviceName)
  public TextView tvDeviceName;
  @BindView(R.id.tvRSSI)
  public TextView tvRSSI;
  @BindView(R.id.tvDeviceMac)
  public TextView tvDeviceMac;

  @BindView(R.id.tvFWVersion)
  public TextView tvFWVersion;

  @BindView(R.id.tvHWVersion)
  public TextView tvHWVersion;

  @BindView(R.id.tvDeviceInfo)
  public TextView tvDeviceInfo;

  public DeviceConnectedViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void bind(Context context, final int position, final Device device,
      final OnItemClickListener listener) {
    tvDeviceName.setText(device.getName());
    tvRSSI.setText(String.valueOf(device.getRssi()));
    tvDeviceMac.setText(device.getId());

    String fw = context.getString(R.string.text_fw_version);
    String hw = context.getString(R.string.text_hw_version);
    String deviceinfo = context.getString(R.string.text_device_info);
    setSpannableString(tvFWVersion, fw.length(), fw + DeviceInfoUtils.getFwVersion(device));
    setSpannableString(tvHWVersion, hw.length(),
        hw + DeviceInfoUtils.getHwVersion(device));
    setSpannableString(tvDeviceInfo, deviceinfo.length(),
        deviceinfo + ((device.getExtras() == null) ? "null"
            : device.getExtras().toString()));
    itemView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(v, position, device);
      }
    });
  }

  private void setSpannableString(TextView textView, int length, String text) {
    SpannableString spannableString = new SpannableString(text);
    ForegroundColorSpan colorSpan = new ForegroundColorSpan(
        itemView.getContext().getResources().getColor(R.color.colorAccent));
    spannableString.setSpan(colorSpan, 0, length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    textView.setText(spannableString);
  }
}
