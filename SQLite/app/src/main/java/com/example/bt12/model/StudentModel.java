package com.example.bt12.model;

public class StudentModel {

    private int MSSV;
    private String hoTen;
    private String ngaySinh;
    private String email;
    private String queQuan;

    public StudentModel(int MSSV, String hoTen, String ngaySinh, String email, String queQuan) {
        this.MSSV = MSSV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.email = email;
        this.queQuan = queQuan;
    }

    public int getMSSV() {
        return MSSV;
    }

    public void setMSSV(int MSSV) {
        this.MSSV = MSSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }
}
