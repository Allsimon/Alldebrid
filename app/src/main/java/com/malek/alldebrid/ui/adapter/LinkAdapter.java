package com.malek.alldebrid.ui.adapter;


import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.R;
import com.malek.alldebrid.ui.listener.LinkOnMenuItemClickListener;

import java.util.List;

public class LinkAdapter extends BaseAdapter {

    private final Context mContext;
    List<Link> links;
    LayoutInflater inflater;

    public LinkAdapter(Context context, List<Link> link) {
        inflater = LayoutInflater.from(context);
        this.links = link;
        mContext = context;
    }

    public int getCount() {
        return links.size();
    }

    public Object getItem(int position) {
        return links.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View cv, ViewGroup parent) {
        ViewHolder holder;
        final Link link = links.get(position);

        if (cv == null) {
            holder = new ViewHolder();
            cv = inflater.inflate(R.layout.adapter_links, parent, false);
            holder.tvName = getTextView(R.id.tv_name, cv);
            holder.tvWeight = getTextView(R.id.tv_weight, cv);
            holder.tvHost = getTextView(R.id.tv_host, cv);
            holder.ibOverflow = (ImageButton) cv.findViewById(R.id.ibOverflow);
            cv.setTag(holder);
        } else {
            holder = (ViewHolder) cv.getTag();
        }
        if (link.isBugged()) {
            // Something went wrong
            holder.tvHost.setText(link.getOriginalLink());
            if (!"".equals(link.getWeight()))
                holder.tvName.setText(link.getWeight());
        } else {
            holder.tvName.setText(link.getName());
            holder.tvWeight.setText(link.getWeight());
            holder.tvHost.setText(link.getHost());
        }
        holder.ibOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view, link);
            }
        });
        return cv;
    }

    public void showPopup(View v, final Link link) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.action_link, popup.getMenu());
        popup.show();
        try {
            popup.setOnMenuItemClickListener(new LinkOnMenuItemClickListener() {
                @Override
                public ClipboardManager getClipboardService() {
                    return (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                }

                @Override
                public Link getLink() {
                    return link;
                }
            });
        } catch (VerifyError ignored) {
            Toast.makeText(mContext, "Sorry, this doesn't work for Android <4  :'(", Toast.LENGTH_SHORT).show();

        }
    }

    private TextView getTextView(int id, View cv) {
        return (TextView) cv.findViewById(id);
    }

    private class ViewHolder {
        TextView tvName, tvWeight, tvHost;
        ImageButton ibOverflow;
    }
}