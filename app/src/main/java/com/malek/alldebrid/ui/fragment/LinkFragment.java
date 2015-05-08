package com.malek.alldebrid.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.R;
import com.malek.alldebrid.ui.adapter.LinkAdapter;
import com.malek.alldebrid.ui.adapter.SimpleAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_link)
@OptionsMenu(R.menu.fragment_link)
public class LinkFragment extends AlldebridFragment {
    public static final String ARG_LINKS = "argLinks";
    public static final String SAVESTATE_LINKS = "savestate_links";
    @ViewById(R.id.etUnrestrainLink)
    EditText etLinks;
    @ViewById(R.id.lvLinks)
    ListView lvLinks;
    @ViewById(R.id.lvLimitedHosts)
    ListView lvLimitedHosts;
    @ViewById(R.id.lvDownloadedInfos)
    ListView lvDownloadedInfos;
    @ViewById(R.id.bUnrestrainLink)
    ActionProcessButton mButtonDebrid;
    int linkToUnrestrain = 0;
    int linkRestrained = 0;
    List<Link> links;
    LinkAdapter linkAdapter;
    SharedPreferences preferences;


    @OptionsItem(R.id.action_remove_link)
    public void removeLink() {
        if (etLinks.getText().toString().length() > 2) {
            etLinks.setText("");
            mButtonDebrid.setProgress(0);
        } else {
            links = new ArrayList<>();
            refreshAdapter();
        }
    }


    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        links = new ArrayList<>();
        lvLinks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                API_Alldebrid.getInstance().saveFile(links.get(position));
            }
        });
        if (getArguments() != null) {
            String argLinks = getArguments().getString(ARG_LINKS);
            if (argLinks != null) {
                etLinks.setText(argLinks);
            }
        }
    }

    @Click(R.id.bUnrestrainLink)
    void unrestrainedLinkClicked() {
        String[] links = etLinks.getText().toString().trim().split(" |\n");
        mButtonDebrid.setProgress(1);
        for (String link : links) {
            if (link.length() > 2) {
                linkToUnrestrain++;
                API_Alldebrid.getInstance().unrestrainLink(link);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLargeLandscapeScreen()) {
            API_Alldebrid.getInstance().getLimitedHostsAndDownloadInfos();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean("autoCopyPaste", false) && getArguments() == null)
            etLinks.setText(getClipboard());
    }

    @Override
    public void onLinkRestrained(Link link) {
        linkRestrained++;
        if (linkToUnrestrain != 0 && mButtonDebrid.getProgress() != -1) {
            mButtonDebrid.setProgress(linkRestrained * 100 / linkToUnrestrain);
            if (linkRestrained / linkToUnrestrain == 1) {
                linkToUnrestrain = 0;
                linkRestrained = 0;
            }
        }
        links.add(link);
        refreshAdapter();

        if (preferences.getBoolean("autoDownload", false))
            API_Alldebrid.getInstance().saveFile(link);
    }

    private void refreshAdapter() {
        try {
            linkAdapter = new LinkAdapter(getActivity(), links);
            lvLinks.setAdapter(linkAdapter);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVESTATE_LINKS, etLinks.getText().toString());
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

    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {

    }

    @Override
    public void onLimitedHostsFetched(String[] limitedHosts) {
        if (isLargeLandscapeScreen()) {
            SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), limitedHosts);
            lvLimitedHosts.setAdapter(simpleAdapter);
        }
    }

    @Override
    public void onDownloadInformationsFetched(String[] downloadedInfos) {
        if (isLargeLandscapeScreen()) {
            SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), downloadedInfos);
            lvDownloadedInfos.setAdapter(simpleAdapter);
        }
    }

    @Override
    public void onLinkRestrainFailed(Link link, String error) {
        onLinkRestrained(link);
        link.setWeight(error);
    }
}
