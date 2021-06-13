package com.pegadaian.vms.model;

public class LocationData {

    private String nama, alamat, telp;

    public LocationData() {
    }

    public LocationData(String nama, String alamat, String telp) {

        this.nama = nama;
        this.alamat = alamat;
        this.telp = telp;
    }

    public String getNama() { return nama; }

    public String getAlamat() { return alamat; }

    public String getTelp() { return telp; }
}
