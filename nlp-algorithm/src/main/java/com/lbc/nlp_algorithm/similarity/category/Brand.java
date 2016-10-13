package com.lbc.nlp_algorithm.similarity.category;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Brand {
	private final static Logger LOG = LoggerFactory.getLogger(
			Brand.class.getName());
	
    private final String brandResource="mutable/brand.txt";
    private final String brandCateResource="mutable/brand_cate.txt";
    private final String filterBrandnameResource = "mutable/brandNameFilter.txt";
    
    private static Map<Integer, BrandItemCate> brandItemCateMap = new HashMap<>();
    private static Map<Integer, BrandItem> brands = new HashMap<>();
    private static Set<String> brandNameFilterSet = new HashSet<String>(){
    	{
			add("其他"); 
			add("其它");
            add("其他品牌");
            add("其它品牌");
		}
    };
    // 有些中文品牌有多个英文名,比如 纽曼 有英文名“Newsmy”和“Newmine”
    private static Map<String, Set<String>> brandEnLowers = new HashMap<>();
    // 品牌词（小写）对应的具体品牌集
    private static Map<String, Set<BrandItem>> nameBrandItems = new HashMap<>();
    private static Set<String> validBrandNameLowcases = new HashSet<>();
    
    private static Boolean isLoading = false;
    private static Brand brand = null;
    
    private Brand(){
    	try {
			init();
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
    }

    public static Brand getInstance(){
    	if(brand == null){
    		brand = new Brand();
    	}
    	return brand;
    }
    
    private void init() throws IOException {
    	if(!isLoading){
    		loadBrandCate();
        	loadBrandName();
        	loadBrandResource();
            initValidBrandNames();
            
            isLoading = true;
    	}
    	
    }
    
    private void loadBrandCate() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(brandCateResource)));
        for(String line = br.readLine(); line!=null; line = br.readLine()){
            if(line.startsWith("#")) continue;
            String[] arr = line.split("\t");
            if(arr.length != 6) continue; // bid, name, secondcateenum, warenum, maxsecondcateradio, maxsecondcate
            BrandItemCate brandItemCate = new BrandItemCate(Integer.valueOf(arr[0]), Integer.valueOf(arr[2]),
                    Integer.valueOf(arr[3]), Float.valueOf(arr[4]), Integer.valueOf(arr[5]));
            brandItemCateMap.put(brandItemCate.brandid, brandItemCate);
        }
        br.close();
    }
    
    private void loadBrandName() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filterBrandnameResource)));
        for(String line = br.readLine(); line!=null; line = br.readLine()){
            brandNameFilterSet.add(line.toLowerCase());
        }
        br.close();
    }
    
    private void loadBrandResource() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(brandResource)));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] arr = line.split("\t");
            if (arr.length >= 4) {
                int bid = Integer.valueOf(arr[0]);
                BrandItemCate brandItemCate = brandItemCateMap.get(bid);
                if (brandItemCate == null) {
                    // 商品名中无此品牌
                    continue;
                }
                String name = arr[1];
                String ename = arr[2];
                String cname = arr[3];
                BrandItem bitem = new BrandItem(bid, name, cname, ename, brandItemCate);
                for (int i = 4; i < arr.length; i++) {
                    String[] tmp = arr[i].split(" ");
                    int cid = Integer.valueOf(tmp[0]);
                    bitem.addCate(cid, tmp[1]);
                }
                brands.put(bid, bitem);

                Set<String> enbrands = brandEnLowers.get(cname);
                if (CollectionUtils.isEmpty(enbrands)) {
                    enbrands = new HashSet<>();
                    enbrands.add(ename.toLowerCase());
                    brandEnLowers.put(cname, enbrands);
                } else {
                    enbrands.add(ename.toLowerCase());
                }
            } else {
            	LOG.warn("brand format error: " + line);
            }
        }
        br.close();
    }

    public Set<String> getEnbrandLowers(String brand_cname){
        return brandEnLowers.get(brand_cname);
    }

    /**
     * 由于品牌词典的数据太多有杂质,过滤掉部分品牌
     * 过滤:中英文相同的且不在不任何商品三级分类的不返回.
     *
     * 如果相同的品牌名多次出现,则只选择出现在三级分类多的.处理这类badcase: 43829   Mi（我的）  Mi  我的    5266 创意礼品
     * @return
     */
    public List<BrandItem> getAllBrands(){
        return new ArrayList<>(brands.values());
    }

    /**
     * 慎用此方法
     * @return 返回的是所有brand.txt配置中的品牌名,很多是不普遍的,慎用!
     */
    public Set<String> getAllBrandNames(){
        Set<String> ret = new HashSet<>(brands.size());
        for (BrandItem brandItem : brands.values()) {
            ret.add(brandItem.cname);
            ret.add(brandItem.ename);
            ret.add(brandItem.name);
        }
        return ret;
    }

    private void initValidBrandNames(){
        validBrandNameLowcases.clear();
        for(BrandItem brandItem : brands.values()) {
            String cname = brandItem.cname.toLowerCase();
            String ename = brandItem.ename.toLowerCase();
            if(!brandNameFilterSet.contains(cname) && cname.length() > 1){
                validBrandNameLowcases.add(cname);
            }
            if(!brandNameFilterSet.contains(ename) && ename.length() > 1){
                validBrandNameLowcases.add(ename);
            }

            // 中文品牌名
            Set<BrandItem> brandItems = nameBrandItems.get(cname);
            addBrand(brandItems, cname, brandItem);

            // 英文品牌名
            brandItems = nameBrandItems.get(ename);
            addBrand(brandItems, ename, brandItem);
        }
    }
    
    private void addBrand(Set<BrandItem> brandItems, String name, BrandItem brandItem) {
    	if(CollectionUtils.isEmpty(brandItems)){
            brandItems = new HashSet<>();
            nameBrandItems.put(name, brandItems);
        }
        brandItems.add(brandItem);
    }

    public Set<String> getFilteredBrandNameLowercases(){
        return validBrandNameLowcases;
    }
    
    public BrandItem getBrand(Integer brand_id){
        return brands.get(brand_id);
    }

    /**
     * 返回品牌词对应的品牌集合
     * @param brandname
     * @return
     */
    public Set<BrandItem> getBrandnameItems(String brandname){
        brandname = brandname.toLowerCase();
        if(validBrandNameLowcases.contains(brandname)){
            return nameBrandItems.get(brandname);
        }
        return null;
    }

    public static void main(String[] args) {
	}
}
