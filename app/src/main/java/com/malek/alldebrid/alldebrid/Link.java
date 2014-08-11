package com.malek.alldebrid.alldebrid;

public class Link {
    protected String name, weight, host, link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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
        } catch (Exception e) {
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Link{" +
                "name='" + name + '\'' +
                ", weight='" + weight + '\'' +
                ", host='" + host + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
