package com.malek.alldebrid.alldebrid;

public class Torrent extends Link {
    private int id;
    private String link, downloaded, status, downloadSpeed, seederNumber;

    @Override
    public String toString() {
        return "Torrent [" + (name != null ? "name=" + name + ", " : "")
                + (weight != null ? "weight=" + weight + ", " : "") + "id="
                + id + ", " + (link != null ? "link=" + link + ", " : "")
                + (downloaded != null ? "downloaded=" + downloaded + ", " : "")
                + (status != null ? "status=" + status : "") + "]";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link.replaceAll("\\\\", "");
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed.replaceAll("\\\\", "");
    }

    public String getSeederNumber() {
        return seederNumber;
    }

    public void setSeederNumber(String seederNumber) {
        this.seederNumber = seederNumber;
    }
}
