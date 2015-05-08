package com.malek.alldebrid.API.pojo;

public class Link {
    protected String name, weight, host, unrestrainedLink, originalLink;
    boolean bugged;

    public boolean isBugged() {
        return bugged;
    }

    public void setBugged(boolean bugged) {
        this.bugged = bugged;
    }

    public Link bugged() {
        bugged = true;
        return this;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public String getUnrestrainedLink() {
        return unrestrainedLink;
    }

    public void setUnrestrainedLink(String unrestrainedLink) {
        this.unrestrainedLink = unrestrainedLink;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        try {
            double w = Double.parseDouble(weight);
            int a = (int) ((w / 1024) / 1000) * 100;
            w = a / 100;
            weight = w + " Mo";
        } catch (Exception ignored) {
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Link{" +
                "name='" + name + '\'' +
                ", weight='" + weight + '\'' +
                ", host='" + host + '\'' +
                ", unrestrainedLink='" + unrestrainedLink + '\'' +
                '}';
    }
}
