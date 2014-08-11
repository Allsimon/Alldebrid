package com.malek.alldebrid.alldebrid.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.view.Surface;
import android.view.WindowManager;

import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.abstracted.AlldebridObserver;

public abstract class AlldebridFragment extends Fragment implements AlldebridObserver {
    public FragmentChanger mFragmentChanger;

    public boolean isLargeScreen() {
        try {
            int screenSize = getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK;
            return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLandscape() {
        try {
            int orientation = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            return orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        API_Alldebrid.getInstance().registerObserver(this);
        mFragmentChanger = (FragmentChanger) getActivity();
    }

    public String getClipboard() {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                return clipboard.getText().toString();
            } else {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = clipboard.getPrimaryClip();
                return clip.getItemAt(0).coerceToText(getActivity()).toString();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public boolean copyToClipboard(String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public interface FragmentChanger {
        public void changeFragment(Fragment fragment);
    }
}
