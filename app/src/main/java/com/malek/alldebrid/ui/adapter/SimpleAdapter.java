package com.malek.alldebrid.ui.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.malek.alldebrid.R;

public class SimpleAdapter extends BaseAdapter {

    String[] limitedHosts;
    LayoutInflater inflater;

    public SimpleAdapter(Context context, String[] limitedHosts) {
        inflater = LayoutInflater.from(context);
        this.limitedHosts = limitedHosts;
    }

    public int getCount() {
        return limitedHosts.length;
    }

    public Object getItem(int position) {
        return limitedHosts[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View cv, ViewGroup parent) {
        ViewHolder holder;
        String limitedHost = limitedHosts[position];

        if (cv == null) {
            holder = new ViewHolder();
            cv = inflater.inflate(R.layout.adapter_limited_hosts, parent, false);
            holder.tvName = getTextView(R.id.tv_name, cv);
            cv.setTag(holder);
        } else {
            holder = (ViewHolder) cv.getTag();
        }
        holder.tvName.setText(limitedHost);
        return cv;
    }

    private TextView getTextView(int id, View cv) {
        return (TextView) cv.findViewById(id);
    }

    private class ViewHolder {
        TextView tvName;

    }
}