package com.malek.alldebrid.ui.listener;

import android.content.ClipboardManager;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;

import com.malek.alldebrid.API.abstracted.SingletonHolder;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.R;

public abstract class LinkOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_copy_links:
                copyToClipboard(getLink().getUnrestrainedLink());
                break;
            case R.id.action_redebrid:
                SingletonHolder.SINGLETON.getDebrider().unrestrainLink(getLink().getOriginalLink());
                break;
        }
        return true;
    }

    public boolean copyToClipboard(String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = getClipboardService();
                clipboard.setText(text);
            } else {
                ClipboardManager clipboard = getClipboardService();
                android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public abstract ClipboardManager getClipboardService();

    public abstract Link getLink();
}