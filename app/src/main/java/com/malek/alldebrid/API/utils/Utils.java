package com.malek.alldebrid.API.utils;

import com.malek.alldebrid.API.pojo.Torrent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", Locale.US);

    /**
     * @return Approximation of the number of days between 2 dates
     */
    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static Date parse(String date) {
        try {
            return format.parse(date);
        } catch (ParseException ignored) {
            ignored.printStackTrace();
            return new Date();
        }
    }

    public static String format(Date date) {
        return format.format(date);
    }

    public static Torrent[] parseTorrentsLink(String link) {
        if ("Invalid cookie.".equals(link))
            return null;
        String[] links = link.replace("[[", "").replace("]]", "")
                .split("\\],\\[");
        List<Torrent> torrents = new ArrayList<>();
        Torrent torrent;
        for (String link_temp : links) {
            String test[] = link_temp.split("\",\"");
            if (test.length > 1) {
                torrent = new Torrent();
                torrent.setId(Integer.parseInt(test[1].replace("\"", "")));
                torrent.setName(test[3]);
                torrent.setStatus(test[4]);
                torrent.setDownloaded(test[5]);
                torrent.setWeight(test[6]);
                torrent.setSeederNumber(test[7]);
                torrent.setDownloadSpeed(test[8]);
                if (test[4].contains("finished"))
                    torrent.setUnrestrainedLink(test[10].replace(",;,", "\n"));
                torrents.add(torrent);
            }
        }
        return torrents.toArray(new Torrent[torrents.size()]);
    }

    public static String parseByte(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
