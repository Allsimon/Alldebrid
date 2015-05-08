package com.malek.alldebrid.ui.fragment;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.abstracted.AbstractAlldebrid;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_login)
public class LoginFragment extends AlldebridFragment {
    @ViewById(R.id.username)
    EditText etUsername;
    @ViewById(R.id.password)
    EditText etPassword;
    @ViewById(R.id.loginButton)
    ActionProcessButton mButtonLogin;
    @ViewById(R.id.logoutButton)
    Button mButtonLogout;
    @ViewById(R.id.tv_createAccount)
    TextView tvCreateAccount;


    @Click(R.id.loginButton)
    public void onLoginButtonClicked() {
        mButtonLogin.setProgress(1);
        mButtonLogin.setEnabled(false);
        etPassword.setEnabled(false);
        etUsername.setEnabled(false);
        doLogin();
    }

    @Click(R.id.logoutButton)
    public void onLogoutButtonClicked() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.clear();
        editor.apply();
    }


    @AfterViews
    public void initEditText() {
        mButtonLogin.setMode(ActionProcessButton.Mode.ENDLESS);
        tvCreateAccount.setMovementMethod(LinkMovementMethod.getInstance());
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
    public void onLinkRestrainFailed(Link link, String error) {

    }
}