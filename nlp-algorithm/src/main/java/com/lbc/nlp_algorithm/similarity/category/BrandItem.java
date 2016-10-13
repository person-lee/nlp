package com.lbc.nlp_algorithm.similarity.category;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BrandItem {
    int bid;
    String name;

    public int getBid() {
        return bid;
    }

    public String getName() {
        return name;
    }

    public String getCname() {
        return cname;
    }

    public String getEname() {
        return ename;
    }

    public Collection<Integer> getThirdCates(){
        return cates.keySet();
    }

    public BrandItemCate getBrandItemCate(){
        return brandItemCate;
    }

    String cname;
    String ename;
    Map<Integer, String> cates;
    // 此品牌商品最集中的二级商品品类
    BrandItemCate brandItemCate;

    public BrandItem(int bid, String name, String cname, String ename, BrandItemCate brandItemCate){
        this.bid = bid;
        this.name = name;
        this.cname = cname;
        this.ename = ename;
        this.brandItemCate = brandItemCate;
        this.cates = new HashMap<Integer, String>();
    }

    protected void addCate(Integer cid, String cate){
        cates.put(cid, cate);
    }
}
