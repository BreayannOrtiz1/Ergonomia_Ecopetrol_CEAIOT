package com.vivalnk.sdk.demo.vital.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import java.util.List;

/**
 * Created by JakeMo on 18-5-2.
 */
public class DeviceConnectedListAdapter extends RecyclerView.Adapter<DeviceConnectedViewHolder> {

  private List<Device> deviceArrayList;
  private OnItemClickListener listener;
  private Context context;

  public DeviceConnectedListAdapter(Context context, List<Device> deviceArrayList,
      OnItemClickListener listener) {
    this.context = context;
    this.deviceArrayList = deviceArrayList;
    this.listener = listener;
  }

  @NonNull
  @Override
  public DeviceConnectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
    View itemView = mInflater.inflate(R.layout.item_connected_result, parent, false);
    return new DeviceConnectedViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull DeviceConnectedViewHolder holder, int position) {
    holder.bind(context, position, deviceArrayList.get(position), listener);
  }

  @Override
  public int getItemCount() {
    return deviceArrayList.size();
  }
}
