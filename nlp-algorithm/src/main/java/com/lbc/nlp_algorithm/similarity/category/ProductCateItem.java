package com.lbc.nlp_algorithm.similarity.category;

public class ProductCateItem {

    Integer cateId;
    ProductCateItem parentItem;
    ProductCateType cateType;
    String cateName;

    public Integer getCateId() {
        return cateId;
    }

    public ProductCateItem getParentItem() {
        return parentItem;
    }

    public ProductCateType getCateType() {
        return cateType;
    }

    public String getCateName() {
        return cateName;
    }

    public ProductCateItem(ProductCateType cateType, Integer cateId, ProductCateItem parentItem, String cateName){
        this.cateId=cateId;
        this.cateType=cateType;
        this.parentItem=parentItem;
        this.cateName=cateName;
    }

    @Override
    public String toString(){
        if(cateType.equals(ProductCateType.First)){
            return String.format("%s\tname:%s\tid:%d", cateType.toString(), cateName, cateId);
        } else if(cateType.equals(ProductCateType.Second)){
            return String.format("%s\t%s\tname:%s\tid:%d", parentItem.toString(), cateType.toString(), cateName, cateId);
        } else if(cateType.equals(ProductCateType.Third)){
            ProductCateItem second = getParentItem();
            ProductCateItem first = second.getParentItem();
            return String.format("Thirdcate\t%s-%s-%s\t%d %d %d", first.cateName, second.cateName, cateName, first.cateId, second.cateId, cateId);
        }
        return null;
    }

    public boolean equals(ProductCateItem ob) {
        return ob.cateType.equals(cateType)
                && ob.cateName.equals(cateName)
                && ob.cateId.equals(cateId)
                && (ob.parentItem==null && parentItem==null || ob.parentItem.equals(parentItem));
    }
}
