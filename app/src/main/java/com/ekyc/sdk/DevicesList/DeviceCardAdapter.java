package com.ekyc.sdk.DevicesList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.ekyc.sdk.KYCDetails.BiomatricVerification;
import com.ekyc.sdk.R;

import java.util.List;

public class DeviceCardAdapter extends Adapter<DeviceCardAdapter.ViewHolder> {
    Context context;
    List<DevicesItems> devicesItems;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textview_device_name;

        ViewHolder(View view) {
            super(view);
            this.textview_device_name = (TextView) view.findViewById(R.id.textview_device_name);

            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    DevicesItems devicesItems = (DevicesItems) DeviceCardAdapter.this.devicesItems.get(ViewHolder.this.getAdapterPosition());

                    if (devicesItems.getFragment_type().equalsIgnoreCase("bio"))
                    {
                        ((BiomatricVerification)context).mGetData(devicesItems);
                    }
                }
            });
        }
    }

    public DeviceCardAdapter(Context context2, List<DevicesItems> list) {
        this.context = context2;
        this.devicesItems = list;
    }

    public int getItemCount() {
        List<DevicesItems> list = this.devicesItems;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.textview_device_name.setText(((DevicesItems) this.devicesItems.get(i)).getName());
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.devices_item, viewGroup, false));
    }
}
