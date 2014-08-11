package com.malek.alldebrid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;
import com.malek.alldebrid.alldebrid.ui.AlldebridFragment;
import com.malek.alldebrid.ui.adapter.LinkAdapter;
import com.malek.alldebrid.ui.adapter.SimpleAdapter;
import com.malek.alldebrid.utils.Logg;

import java.util.ArrayList;
import java.util.List;

public class LinkFragment extends AlldebridFragment {
    EditText etLinks;
    ListView lvLinks, lvLimitedHosts, lvDownloadedInfos;
    public static final String ARG_LINKS = "argLinks";
    public static final String SAVESTATE_LINKS = "savestate_links";

    ActionProcessButton mButtonDebrid;
    int linkToUnrestrain = 0;
    int linkRestrained = 0;
    List<Link> links;
    LinkAdapter linkAdapter;
    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_link, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_link, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove_link:
                if (etLinks.getText().toString().length() > 2) {
                    etLinks.setText("");
                    mButtonDebrid.setProgress(0);
                } else {
                    links = new ArrayList<Link>();
                    refreshAdapter();
                }
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        links = new ArrayList<Link>();
        etLinks = (EditText) view.findViewById(R.id.etUnrestrainLink);
        mButtonDebrid = (ActionProcessButton) view.findViewById(R.id.bUnrestrainLink);
        mButtonDebrid.setMode(ActionProcessButton.Mode.PROGRESS);
        lvLinks = (ListView) view.findViewById(R.id.lvLinks);
        if (isLargeScreen() && isLandscape()) {
            lvDownloadedInfos = (ListView) view.findViewById(R.id.lvDownloadedInfos);
            lvLimitedHosts = (ListView) view.findViewById(R.id.lvLimitedHosts);
        }

        mButtonDebrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] links = etLinks.getText().toString().trim().split(" |\n");
                mButtonDebrid.setProgress(1);
                for (String link : links) {
                    if (link.length() > 2) {
                        linkToUnrestrain++;
                        API_Alldebrid.getInstance().unrestrainLink(link);
                    }
                }
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        if (isLandscape() && isLargeScreen()) {
            API_Alldebrid.getInstance().getLimitedHostsAndDownloadInfos();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean("autoCopyPaste", false))
            etLinks.setText(getClipboard());

    }

    @Override
    public void onLinkRestrained(Link link) {
        linkRestrained++;
        if (linkToUnrestrain != 0) {
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
            Logg.e(links.size());
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
        if (isLargeScreen() && isLandscape()) {
            SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), limitedHosts);
            lvLimitedHosts.setAdapter(simpleAdapter);
        }
    }

    @Override
    public void onDownloadInformationsFetched(String[] downloadedInfos) {
        if (isLargeScreen() && isLandscape()) {
            SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), downloadedInfos);
            lvDownloadedInfos.setAdapter(simpleAdapter);
        }
    }

    @Override
    public void onLinkRestrainFailed(int status) {
        mButtonDebrid.setProgress(-1);
    }
}
