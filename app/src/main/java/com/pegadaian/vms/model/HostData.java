package com.pegadaian.vms.model;

public class HostData {

    private String nama, jabatan, email, telp;

    public HostData() {
    }

    public HostData(String nama, String jabatan, String email, String telp) {

        this.nama = nama;
        this.jabatan = jabatan;
        this.email = email;
        this.telp = telp;
    }

    public String getNama() {
        return nama;
    }

    public String getJabatan() {
        return jabatan;
    }

    public String getEmail() {
        return email;
    }

    public String getTelp() {
        return telp;
    }
}
