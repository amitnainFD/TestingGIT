//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.13 at 04:44:06 PM EDT 
//


package com.freshdirect.fdstore.content.productfeed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for configurations complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="configurations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="configuration" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="variationOption" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="charValueName" type="{}LimitedString30"/>
 *                             &lt;element name="charValueDesc" type="{}LimitedString40"/>
 *                             &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                             &lt;element name="pricingUnit" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="charName" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "configurations", propOrder = {
    "configuration"
})
public class Configurations {

    protected List<Configurations.Configuration> configuration;

    /**
     * Gets the value of the configuration property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the configuration property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConfiguration().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Configurations.Configuration }
     * 
     * 
     */
    public List<Configurations.Configuration> getConfiguration() {
        if (configuration == null) {
            configuration = new ArrayList<Configurations.Configuration>();
        }
        return this.configuration;
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
     *         &lt;element name="variationOption" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="charValueName" type="{}LimitedString30"/>
     *                   &lt;element name="charValueDesc" type="{}LimitedString40"/>
     *                   &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *                   &lt;element name="pricingUnit" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="charName" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "variationOption"
    })
    public static class Configuration {

        protected List<Configurations.Configuration.VariationOption> variationOption;
        @XmlAttribute
        protected String charName;

        /**
         * Gets the value of the variationOption property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the variationOption property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVariationOption().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Configurations.Configuration.VariationOption }
         * 
         * 
         */
        public List<Configurations.Configuration.VariationOption> getVariationOption() {
            if (variationOption == null) {
                variationOption = new ArrayList<Configurations.Configuration.VariationOption>();
            }
            return this.variationOption;
        }

        /**
         * Gets the value of the charName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCharName() {
            return charName;
        }

        /**
         * Sets the value of the charName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCharName(String value) {
            this.charName = value;
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
         *         &lt;element name="charValueName" type="{}LimitedString30"/>
         *         &lt;element name="charValueDesc" type="{}LimitedString40"/>
         *         &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
         *         &lt;element name="pricingUnit" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
            "charValueName",
            "charValueDesc",
            "price",
            "pricingUnit"
        })
        public static class VariationOption {

            @XmlElement(required = true)
            protected String charValueName;
            @XmlElement(required = true)
            protected String charValueDesc;
            @XmlElement(required = true)
            protected BigDecimal price;
            @XmlElement(required = true)
            protected String pricingUnit;

            /**
             * Gets the value of the charValueName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCharValueName() {
                return charValueName;
            }

            /**
             * Sets the value of the charValueName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCharValueName(String value) {
                this.charValueName = value;
            }

            /**
             * Gets the value of the charValueDesc property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCharValueDesc() {
                return charValueDesc;
            }

            /**
             * Sets the value of the charValueDesc property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCharValueDesc(String value) {
                this.charValueDesc = value;
            }

            /**
             * Gets the value of the price property.
             * 
             * @return
             *     possible object is
             *     {@link BigDecimal }
             *     
             */
            public BigDecimal getPrice() {
                return price;
            }

            /**
             * Sets the value of the price property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigDecimal }
             *     
             */
            public void setPrice(BigDecimal value) {
                this.price = value;
            }

            /**
             * Gets the value of the pricingUnit property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPricingUnit() {
                return pricingUnit;
            }

            /**
             * Sets the value of the pricingUnit property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPricingUnit(String value) {
                this.pricingUnit = value;
            }

        }

    }

}
