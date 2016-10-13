package com.lbc.nlp_algorithm.similarity.category;

public class BrandItemCate {
    int brandid;
    int secondcatenum;
    int warenum;
    float maxsecondcateradio;
    int maxsecondcate;

    public int getBrandid() {
        return brandid;
    }

    public int getSecondcatenum() {
        return secondcatenum;
    }

    public int getWarenum() {
        return warenum;
    }

    public float getMaxsecondcateradio() {
        return maxsecondcateradio;
    }

    public int getMaxsecondcate() {
        return maxsecondcate;
    }

    public BrandItemCate(int brandid, int secondcatenum, int warenum, float maxsecondcateradio, int maxsecondcate){
        this.brandid = brandid;
        this.secondcatenum = secondcatenum;
        this.warenum = warenum;
        this.maxsecondcateradio = maxsecondcateradio;
        this.maxsecondcate = maxsecondcate;
    }
}
