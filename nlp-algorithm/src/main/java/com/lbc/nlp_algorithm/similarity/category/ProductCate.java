package com.lbc.nlp_algorithm.similarity.category;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCate {
	private final static Logger LOG = LoggerFactory.getLogger(
			ProductCate.class.getName());

    private final String productCateResource="mutable/productCate.txt";
    private final String cateNameFilterResource="mutable/productCateNamefilter.txt";
    private final String cateNameSyncResource= "mutable/productCateNameSync.txt";
    private final String cateFilterResource= "mutable/productCateFilter.txt";   // 人工在标商品品类词时,顺便把部分不适合作为商品分类的类别也标出了
    
    private static Map<Integer, Set<Integer>> firstSecond = new HashMap<>();
    private static Map<Integer, Set<Integer>> secondThird = new HashMap<>();
    private static Set<Integer> thirdCates = new HashSet<>();
    private static Map<Integer, ProductCateItem> productCateItemMap = new HashMap<>();
    private static Set<String> cateNameFilter = new HashSet<>();
    private static Map<String, List<Integer>> nameThirdCateIds = new HashMap<>();
    
    private static Boolean isLoad = false;
    private static ProductCate productCate= null;
    
    private ProductCate(){
    	try {
			init();
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
    }

    public static ProductCate getInstance(){
    	if(productCate == null){
    		productCate = new ProductCate();
    	}
    	return productCate;
    }

    public void init() throws IOException {
    	if(!isLoad){
    		loadProductCateNameFilter();
        	loadProductCate();
        	loadProductCateFilter();
            loadCateNameIds();
            
            isLoad = true;
    	}
    }
    
    private void loadProductCateNameFilter() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(cateNameFilterResource)));
        for (String line = br.readLine(); line!=null; line=br.readLine()) {
            if(!line.startsWith("#")) {
            	cateNameFilter.add(line.trim().toLowerCase());
            }
        }
        br.close();
    }
    
    private void loadProductCate() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(productCateResource)));
        for (String line = br.readLine(); line!=null; line=br.readLine()) {
            line = StringUtils.trimToEmpty(line);
            if(line.startsWith("#") || StringUtils.isEmpty(line)) {
                continue;
            }

            String[] arr = line.split("\t");
            if(arr.length == 6) {
                Integer first = Integer.valueOf(arr[0]);
                Integer second = Integer.valueOf(arr[1]);
                Integer third = Integer.valueOf(arr[2]);
                String first_s = arr[3];
                String second_s = arr[4];
                String third_s = arr[5];
                thirdCates.add(third);
                Set<Integer> sc = firstSecond.get(first);
                if(CollectionUtils.isEmpty(sc)){
                    sc = new HashSet<>();
                    firstSecond.put(first, sc);
                }
                sc.add(second);

                Set<Integer> tc = secondThird.get(second);
                if(CollectionUtils.isEmpty(tc)){
                    tc = new HashSet<>();
                    secondThird.put(second, tc);
                }
                tc.add(third);

                ProductCateItem firstCateItem = productCateItemMap.get(first);
                if(firstCateItem == null){
                    firstCateItem = new ProductCateItem(ProductCateType.First, first, null, first_s);
                    productCateItemMap.put(first, firstCateItem);
                } else if(!firstCateItem.equals(new ProductCateItem(ProductCateType.First, first, null, first_s))){
                    throw new InvalidPropertiesFormatException(line + "\tfirst cate conflict with " + firstCateItem);
                }

                ProductCateItem secondCateItem = productCateItemMap.get(second);
                if(secondCateItem == null){
                    secondCateItem = new ProductCateItem(ProductCateType.Second, second, firstCateItem, second_s);
                    productCateItemMap.put(second, secondCateItem);
                } else if(!secondCateItem.equals(new ProductCateItem(ProductCateType.Second, second, firstCateItem, second_s))){
                    throw new InvalidPropertiesFormatException(line + "\tsecond cate conflict with " + secondCateItem);
                }

                ProductCateItem thirdCateItem = productCateItemMap.get(third);
                if(thirdCateItem == null){
                    thirdCateItem = new ProductCateItem(ProductCateType.Third, third, secondCateItem, third_s);
                    productCateItemMap.put(third, thirdCateItem);
                } else {
                    throw new InvalidPropertiesFormatException(line + "\tduplicate with " + thirdCateItem);
                }
            } else {
                throw new InvalidPropertiesFormatException("format error: " + line);
            }
        }
        br.close();
    }
    
    private void loadProductCateFilter() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(cateFilterResource)));
        for (String line = br.readLine(); line!=null; line=br.readLine()) {
            if(line.startsWith("#")) continue;
            int id = Integer.valueOf(line.split("\t")[0]);
            ProductCateItem productCateItem = productCateItemMap.get(id);
            if(productCateItem != null) {
                for (String word : productCateItem.cateName.split("/")){
                    cateNameFilter.add(word.toLowerCase());
                }
            }
        }
        br.close();
    }
    
    private void loadProductCateNameSync() throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(cateNameSyncResource)));
        for (String line = br.readLine(); line!=null; line=br.readLine()) {
            if(line.startsWith("#")) continue;
            String[] arr = line.split("\t");
            if(arr.length==3){
                int id = Integer.valueOf(arr[0]);
                for(String sync : arr[2].split(" ")){
                    if(cateNameFilter.contains(sync.toLowerCase())) continue;
                    List<Integer> ids = nameThirdCateIds.get(sync);
                    if(ids==null){
                        ids = new ArrayList<>();
                        nameThirdCateIds.put(sync, ids);
                    }
                    ids.add(id);
                }
            }
        }
        br.close();
    }

    private void loadCateNameIds() throws IOException {
        // 先从品类中加载品类名
        for(ProductCateItem item : getProductCateItems()){
            if(item.cateType.equals(ProductCateType.Third) ){
                for(String name : item.getCateName().split("/")) {
                    if(!cateNameFilter.contains(name.toLowerCase())){
                        List<Integer> ids = nameThirdCateIds.get(name);
                        if(CollectionUtils.isEmpty(ids)){
                            ids = new ArrayList<>();
                            nameThirdCateIds.put(name, ids);
                        }
                        ids.add(item.getCateId());
                    }
                }
            }
        }
        
     // 再从品类同义词中加载品类名
        loadProductCateNameSync();
    }

    public ProductCateItem getProductCateItem(int cateId){
        return productCateItemMap.get(cateId);
    }

    public Collection<ProductCateItem> getProductCateItems(){
        return productCateItemMap.values();
    }

    public Set<String> getValidCateNames(){
        return nameThirdCateIds.keySet();
    }

    public boolean hasValidWareCatename(ProductCateItem productCateItem){
        for(String name : productCateItem.getCateName().split("/")) {
            if(!cateNameFilter.contains(name))
                return true;
        }
        return false;
    }

    public Map<String, List<Integer>> getNameThirdCateids(){
        return nameThirdCateIds;
    }

    public List<Integer> getThirdCateids(String catename){
        return nameThirdCateIds.get(catename);
    }

    public static void main(String[] args) throws Exception {
        ProductCate productCate = new ProductCate();
        productCate.init();
    }
}
