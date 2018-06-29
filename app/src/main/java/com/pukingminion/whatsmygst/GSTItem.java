package com.pukingminion.whatsmygst;

/**
 * Created by Samarth on 28/06/18.
 */

public class GSTItem {
    String id;
    String desc;
    String cgst;
    String sgst;
    String igst;

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getCgst() {
        return cgst;
    }

    public String getSgst() {
        return sgst;
    }

    public String getIgst() {
        return igst;
    }


    public GSTItem(String[] data) {
        this.desc = data[0].replace("\"", "").toLowerCase();
        this.cgst = data[1].replace("\"", "");
        this.sgst = data[2].replace("\"", "");
        this.igst = data[3].replace("\"", "");
    }
}
