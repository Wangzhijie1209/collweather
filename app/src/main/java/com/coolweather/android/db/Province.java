package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;
//使用Litepar2.0及其以后不再继承DataSupport 而是继承LitePalSupport
//省的实体类
public class Province extends LitePalSupport {
    private int id;
    private String provinceName;//省的名字
    private int provinceCode;//省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
