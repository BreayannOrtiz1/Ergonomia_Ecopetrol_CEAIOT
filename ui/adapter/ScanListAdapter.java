package com.vivalnk.sdk.demo.vital.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by JakeMo on 18-5-2.
 */
public class ScanListAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

  private List<StatusDevice> mDeviceArrayList;
  private OnItemClickListener listener;

  private Comparator comparator = new Comparator<StatusDevice>() {
    @Override
    public int compare(StatusDevice o1, StatusDevice o2) {
      int rssi1 = o1.device.getRssi();
      int rssi2 = o2.device.getRssi();
      if (o1.connect) {
        rssi1 = Math.abs(rssi1);
      }

      if (o2.connect) {
        rssi2 = Math.abs(rssi2);
      }

      if (o1.connect && o2.connect) {
        return rssi1 - rssi2;
      } else {
        return rssi2 - rssi1;
      }
    }
  };

  public ScanListAdapter(List<StatusDevice> deviceArrayList,
      OnItemClickListener listener) {
    mDeviceArrayList = deviceArrayList;
    this.listener = listener;
  }

  public void updateConnectStatus(boolean connect, Device device) {
    for (StatusDevice temp : mDeviceArrayList) {
      if (device.getId().equalsIgnoreCase(temp.device.getId())) {
        temp.connect = connect;
      }
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
    View itemView = mInflater.inflate(R.layout.item_scan_result, parent, false);
    return new DeviceViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
    holder.bind(position, mDeviceArrayList.get(position), listener);
  }

  @Override
  public int getItemCount() {
    return mDeviceArrayList.size();
  }

  public static class StatusDevice {

    public boolean connect;
    public Device device;

    public StatusDevice(Device device) {
      this.device = device;
      this.connect = false;
    }

    public StatusDevice(Device device, boolean connect) {
      this.device = device;
      this.connect = connect;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof StatusDevice)) return false;
      StatusDevice that = (StatusDevice) o;
      return  Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {
      return Objects.hash(device);
    }
  }
}
