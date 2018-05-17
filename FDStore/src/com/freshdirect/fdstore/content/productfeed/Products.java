//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.13 at 04:44:06 PM EDT 
//


package com.freshdirect.fdstore.content.productfeed;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="product" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="attributes" type="{}attributes" minOccurs="0"/>
 *                   &lt;element name="prices" type="{}prices" minOccurs="0"/>
 *                   &lt;element name="groupPrices" type="{}groupPrices" minOccurs="0"/>
 *                   &lt;element name="saleUnits" type="{}saleUnits" minOccurs="0"/>
 *                   &lt;element name="nutritionInfo" type="{}nutritionInfo" minOccurs="0"/>
 *                   &lt;element name="configurations" type="{}configurations" minOccurs="0"/>
 *                   &lt;element name="ratings" type="{}ratings" minOccurs="0"/>
 *                   &lt;element name="inventories" type="{}inventories" minOccurs="0"/>
 *                   &lt;element name="images" type="{}images" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="skuCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="upc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="materialNum" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="prodId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="prodName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="prodUrl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="catId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="subCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="parentCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="rootCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="deptId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="prodStatus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="minQuantity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="maxQuantity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="qtyIncrement" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "product"
})
@XmlRootElement(name = "products")
public class Products {

    @XmlElement(required = true)
    protected List<Products.Product> product;

    /**
     * Gets the value of the product property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the product property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProduct().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Products.Product }
     * 
     * 
     */
    public List<Products.Product> getProduct() {
        if (product == null) {
            product = new ArrayList<Products.Product>();
        }
        return this.product;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="attributes" type="{}attributes" minOccurs="0"/>
     *         &lt;element name="prices" type="{}prices" minOccurs="0"/>
     *         &lt;element name="groupPrices" type="{}groupPrices" minOccurs="0"/>
     *         &lt;element name="saleUnits" type="{}saleUnits" minOccurs="0"/>
     *         &lt;element name="nutritionInfo" type="{}nutritionInfo" minOccurs="0"/>
     *         &lt;element name="configurations" type="{}configurations" minOccurs="0"/>
     *         &lt;element name="ratings" type="{}ratings" minOccurs="0"/>
     *         &lt;element name="inventories" type="{}inventories" minOccurs="0"/>
     *         &lt;element name="images" type="{}images" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="skuCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="upc" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="materialNum" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="prodId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="prodName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="prodUrl" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="catId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="subCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="parentCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="rootCatId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="deptId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="prodStatus" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="minQuantity" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="maxQuantity" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="qtyIncrement" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "attributes",
        "prices",
        "groupPrices",
        "saleUnits",
        "nutritionInfo",
        "configurations",
        "ratings",
        "inventories",
        "images"
    })
    public static class Product {

        protected Attributes attributes;
        protected Prices prices;
        protected GroupPrices groupPrices;
        protected SaleUnits saleUnits;
        protected NutritionInfo nutritionInfo;
        protected Configurations configurations;
        protected Ratings ratings;
        protected Inventories inventories;
        protected Images images;
        @XmlAttribute(required = true)
        protected String skuCode;
        @XmlAttribute(required = true)
        protected String upc;
        @XmlAttribute(required = true)
        protected String materialNum;
        @XmlAttribute(required = true)
        protected String prodId;
        @XmlAttribute(required = true)
        protected String prodName;
        @XmlAttribute
        protected String prodUrl;
        @XmlAttribute
        protected String catId;
        @XmlAttribute
        protected String subCatId;
        @XmlAttribute
        protected String parentCatId;
        @XmlAttribute
        protected String rootCatId;
        @XmlAttribute
        protected String deptId;
        @XmlAttribute
        protected String prodStatus;
        @XmlAttribute
        protected String minQuantity;
        @XmlAttribute
        protected String maxQuantity;
        @XmlAttribute
        protected String qtyIncrement;

        /**
         * Gets the value of the attributes property.
         * 
         * @return
         *     possible object is
         *     {@link Attributes }
         *     
         */
        public Attributes getAttributes() {
            return attributes;
        }

        /**
         * Sets the value of the attributes property.
         * 
         * @param value
         *     allowed object is
         *     {@link Attributes }
         *     
         */
        public void setAttributes(Attributes value) {
            this.attributes = value;
        }

        /**
         * Gets the value of the prices property.
         * 
         * @return
         *     possible object is
         *     {@link Prices }
         *     
         */
        public Prices getPrices() {
            return prices;
        }

        /**
         * Sets the value of the prices property.
         * 
         * @param value
         *     allowed object is
         *     {@link Prices }
         *     
         */
        public void setPrices(Prices value) {
            this.prices = value;
        }

        /**
         * Gets the value of the groupPrices property.
         * 
         * @return
         *     possible object is
         *     {@link GroupPrices }
         *     
         */
        public GroupPrices getGroupPrices() {
            return groupPrices;
        }

        /**
         * Sets the value of the groupPrices property.
         * 
         * @param value
         *     allowed object is
         *     {@link GroupPrices }
         *     
         */
        public void setGroupPrices(GroupPrices value) {
            this.groupPrices = value;
        }

        /**
         * Gets the value of the saleUnits property.
         * 
         * @return
         *     possible object is
         *     {@link SaleUnits }
         *     
         */
        public SaleUnits getSaleUnits() {
            return saleUnits;
        }

        /**
         * Sets the value of the saleUnits property.
         * 
         * @param value
         *     allowed object is
         *     {@link SaleUnits }
         *     
         */
        public void setSaleUnits(SaleUnits value) {
            this.saleUnits = value;
        }

        /**
         * Gets the value of the nutritionInfo property.
         * 
         * @return
         *     possible object is
         *     {@link NutritionInfo }
         *     
         */
        public NutritionInfo getNutritionInfo() {
            return nutritionInfo;
        }

        /**
         * Sets the value of the nutritionInfo property.
         * 
         * @param value
         *     allowed object is
         *     {@link NutritionInfo }
         *     
         */
        public void setNutritionInfo(NutritionInfo value) {
            this.nutritionInfo = value;
        }

        /**
         * Gets the value of the configurations property.
         * 
         * @return
         *     possible object is
         *     {@link Configurations }
         *     
         */
        public Configurations getConfigurations() {
            return configurations;
        }

        /**
         * Sets the value of the configurations property.
         * 
         * @param value
         *     allowed object is
         *     {@link Configurations }
         *     
         */
        public void setConfigurations(Configurations value) {
            this.configurations = value;
        }

        /**
         * Gets the value of the ratings property.
         * 
         * @return
         *     possible object is
         *     {@link Ratings }
         *     
         */
        public Ratings getRatings() {
            return ratings;
        }

        /**
         * Sets the value of the ratings property.
         * 
         * @param value
         *     allowed object is
         *     {@link Ratings }
         *     
         */
        public void setRatings(Ratings value) {
            this.ratings = value;
        }

        /**
         * Gets the value of the inventories property.
         * 
         * @return
         *     possible object is
         *     {@link Inventories }
         *     
         */
        public Inventories getInventories() {
            return inventories;
        }

        /**
         * Sets the value of the inventories property.
         * 
         * @param value
         *     allowed object is
         *     {@link Inventories }
         *     
         */
        public void setInventories(Inventories value) {
            this.inventories = value;
        }

        /**
         * Gets the value of the images property.
         * 
         * @return
         *     possible object is
         *     {@link Images }
         *     
         */
        public Images getImages() {
            return images;
        }

        /**
         * Sets the value of the images property.
         * 
         * @param value
         *     allowed object is
         *     {@link Images }
         *     
         */
        public void setImages(Images value) {
            this.images = value;
        }

        /**
         * Gets the value of the skuCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSkuCode() {
            return skuCode;
        }

        /**
         * Sets the value of the skuCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSkuCode(String value) {
            this.skuCode = value;
        }

        /**
         * Gets the value of the upc property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUpc() {
            return upc;
        }

        /**
         * Sets the value of the upc property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUpc(String value) {
            this.upc = value;
        }

        /**
         * Gets the value of the materialNum property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMaterialNum() {
            return materialNum;
        }

        /**
         * Sets the value of the materialNum property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMaterialNum(String value) {
            this.materialNum = value;
        }

        /**
         * Gets the value of the prodId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProdId() {
            return prodId;
        }

        /**
         * Sets the value of the prodId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProdId(String value) {
            this.prodId = value;
        }

        /**
         * Gets the value of the prodName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProdName() {
            return prodName;
        }

        /**
         * Sets the value of the prodName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProdName(String value) {
            this.prodName = value;
        }

        /**
         * Gets the value of the prodUrl property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProdUrl() {
            return prodUrl;
        }

        /**
         * Sets the value of the prodUrl property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProdUrl(String value) {
            this.prodUrl = value;
        }

        /**
         * Gets the value of the catId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCatId() {
            return catId;
        }

        /**
         * Sets the value of the catId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCatId(String value) {
            this.catId = value;
        }

        /**
         * Gets the value of the subCatId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubCatId() {
            return subCatId;
        }

        /**
         * Sets the value of the subCatId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubCatId(String value) {
            this.subCatId = value;
        }

        /**
         * Gets the value of the parentCatId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getParentCatId() {
            return parentCatId;
        }

        /**
         * Sets the value of the parentCatId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setParentCatId(String value) {
            this.parentCatId = value;
        }

        /**
         * Gets the value of the rootCatId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRootCatId() {
            return rootCatId;
        }

        /**
         * Sets the value of the rootCatId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRootCatId(String value) {
            this.rootCatId = value;
        }

        /**
         * Gets the value of the deptId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDeptId() {
            return deptId;
        }

        /**
         * Sets the value of the deptId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDeptId(String value) {
            this.deptId = value;
        }

        /**
         * Gets the value of the prodStatus property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProdStatus() {
            return prodStatus;
        }

        /**
         * Sets the value of the prodStatus property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProdStatus(String value) {
            this.prodStatus = value;
        }

        /**
         * Gets the value of the minQuantity property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMinQuantity() {
            return minQuantity;
        }

        /**
         * Sets the value of the minQuantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMinQuantity(String value) {
            this.minQuantity = value;
        }

        /**
         * Gets the value of the maxQuantity property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMaxQuantity() {
            return maxQuantity;
        }

        /**
         * Sets the value of the maxQuantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMaxQuantity(String value) {
            this.maxQuantity = value;
        }

        /**
         * Gets the value of the qtyIncrement property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getQtyIncrement() {
            return qtyIncrement;
        }

        /**
         * Sets the value of the qtyIncrement property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setQtyIncrement(String value) {
            this.qtyIncrement = value;
        }

    }

}
