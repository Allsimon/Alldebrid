package com.malek.alldebrid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;
import com.malek.alldebrid.alldebrid.abstracted.AbstractAlldebrid;
import com.malek.alldebrid.alldebrid.ui.AlldebridFragment;

public class LoginFragment extends AlldebridFragment {
    EditText etUsername, etPassword;
    ActionProcessButton mButtonLogin;
    Button mButtonLogout;
    TextView tvCreateAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        etUsername = (EditText) view.findViewById(R.id.username);
        etPassword = (EditText) view.findViewById(R.id.password);
        tvCreateAccount = (TextView) view.findViewById(R.id.tv_createAccount);
        mButtonLogin = (ActionProcessButton) view.findViewById(R.id.loginButton);
        mButtonLogout = (Button) view.findViewById(R.id.logoutButton);
        initEditText();
        mButtonLogin.setMode(ActionProcessButton.Mode.ENDLESS);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonLogin.setProgress(1);
                mButtonLogin.setEnabled(false);
                etPassword.setEnabled(false);
                etUsername.setEnabled(false);
                doLogin();
            }
        });
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.clear();
                editor.commit();
            }
        });
        tvCreateAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initEditText() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String login = preferences.getString(AbstractAlldebrid.LOGIN, "");
        String password = preferences.getString(AbstractAlldebrid.PASSWORD, "");
        etUsername.setText(login);
        etPassword.setText(password);
        if (API_Alldebrid.getInstance().isLogged())
            mButtonLogout.setVisibility(View.VISIBLE);

    }

    public void doLogin() {
        String username = this.etUsername.getText().toString();
        String password = this.etPassword.getText().toString();
        API_Alldebrid.getInstance().login(username, password);
    }

    @Override
    public void onLinkRestrained(Link link) {
    }

    @Override
    public void onTorrentAdded(Torrent torrent) {
    }

    @Override
    public void onTorrentRemoved(Torrent torrent) {
    }

    @Override
    public void onSomethingBugged(int status, String text) {
    }

    @Override
    public void onLogin(int status) {
        if (status == AbstractAlldebrid.LOGIN_SUCCESSFUL)
            mButtonLogin.setProgress(100);
        else if (status == AbstractAlldebrid.LOGIN_FAILED) {
            mButtonLogin.setProgress(-1);
            mButtonLogin.setEnabled(true);
            etPassword.setEnabled(true);
            etUsername.setEnabled(true);
        }
    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {
    }

    @Override
    public void onLimitedHostsFetched(String[] limitedHosts) {

    }

    @Override
    public void onDownloadInformationsFetched(String[] downloadedInfos) {

    }

    @Override
    public void onLinkRestrainFailed(int status) {

    }
}