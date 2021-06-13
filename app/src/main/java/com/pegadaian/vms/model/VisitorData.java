package com.pegadaian.vms.model;

public class VisitorData {

    private String nama, instansi, telp, email, host, tujuan, checkin, checkout, status, qr, foto, rfid, key;

    public VisitorData() {
    }

    public VisitorData(String nama, String instansi, String telp, String email, String host, String tujuan, 
                       String checkin, String checkout, String status, String qr, String foto, String rfid) {

        this.nama = nama;
        this.instansi = instansi;
        this.telp = telp;
        this.email = email;
        this.host = host;
        this.tujuan = tujuan;
        this.checkin = checkin;
        this.checkout = checkout;
        this.status = status;
        this.qr = qr;
        this.foto = foto;
        this.rfid = rfid;
    }

    public String getNama() {
        return nama;
    }

    public String getInstansi() {
        return instansi;
    }

    public String getTelp() {
        return telp;
    }

    public String getEmail() {
        return email;
    }

    public String getHost() {
        return host;
    }

    public String getTujuan() {
        return tujuan;
    }

    public String getCheckin() {
        return checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public String getStatus() {
        return status;
    }

    public String getQr() {
        return qr;
    }

    public String getFoto() {
        return foto;
    }
    
    public String getRfid() {
        return rfid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
