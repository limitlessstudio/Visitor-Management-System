package com.pegadaian.vms.model;

public class RfidData {

    private String uid, tgl, status;

    public RfidData() {
    }

    public RfidData(String uid, String tgl, String status) {

        this.uid = uid;
        this.tgl = tgl;
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public String getTgl() {
        return tgl;
    }

    public String getStatus() {
        return status;
    }
}
