package com.malek.alldebrid.ui.fragment;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.abstracted.AlldebridObserver;
import com.malek.alldebrid.R;


public abstract class AlldebridFragment extends Fragment implements AlldebridObserver {
    public FragmentChanger mFragmentChanger;

    public boolean isLargeLandscapeScreen() {
        return getResources().getBoolean(R.bool.large_landscape);
    }

    @Override
    public void onResume() {
        super.onResume();
        API_Alldebrid.getInstance().registerObserver(this);
        mFragmentChanger = (FragmentChanger) getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        API_Alldebrid.getInstance().removeObserver(this);
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
                ClipboardManager clipboard = (ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", text);
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
