
package com.easyinsight.datafeeds.netsuite.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ManufacturingRoutingSearchRow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ManufacturingRoutingSearchRow">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:core_2014_1.platform.webservices.netsuite.com}SearchRow">
 *       &lt;sequence>
 *         &lt;element name="basic" type="{urn:common_2014_1.platform.webservices.netsuite.com}ManufacturingRoutingSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="itemJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}ItemSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="locationJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}LocationSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="manufacturingCostTemplateJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}ManufacturingCostTemplateSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="manufacturingWorkCenterJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}EntityGroupSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="userJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}EmployeeSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="customSearchJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}CustomSearchRowBasic" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManufacturingRoutingSearchRow", namespace = "urn:supplychain_2014_1.lists.webservices.netsuite.com", propOrder = {
    "basic",
    "itemJoin",
    "locationJoin",
    "manufacturingCostTemplateJoin",
    "manufacturingWorkCenterJoin",
    "userJoin",
    "customSearchJoin"
})
public class ManufacturingRoutingSearchRow
    extends SearchRow
{

    protected ManufacturingRoutingSearchRowBasic basic;
    protected ItemSearchRowBasic itemJoin;
    protected LocationSearchRowBasic locationJoin;
    protected ManufacturingCostTemplateSearchRowBasic manufacturingCostTemplateJoin;
    protected EntityGroupSearchRowBasic manufacturingWorkCenterJoin;
    protected EmployeeSearchRowBasic userJoin;
    protected List<CustomSearchRowBasic> customSearchJoin;

    /**
     * Gets the value of the basic property.
     * 
     * @return
     *     possible object is
     *     {@link ManufacturingRoutingSearchRowBasic }
     *     
     */
    public ManufacturingRoutingSearchRowBasic getBasic() {
        return basic;
    }

    /**
     * Sets the value of the basic property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManufacturingRoutingSearchRowBasic }
     *     
     */
    public void setBasic(ManufacturingRoutingSearchRowBasic value) {
        this.basic = value;
    }

    /**
     * Gets the value of the itemJoin property.
     * 
     * @return
     *     possible object is
     *     {@link ItemSearchRowBasic }
     *     
     */
    public ItemSearchRowBasic getItemJoin() {
        return itemJoin;
    }

    /**
     * Sets the value of the itemJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemSearchRowBasic }
     *     
     */
    public void setItemJoin(ItemSearchRowBasic value) {
        this.itemJoin = value;
    }

    /**
     * Gets the value of the locationJoin property.
     * 
     * @return
     *     possible object is
     *     {@link LocationSearchRowBasic }
     *     
     */
    public LocationSearchRowBasic getLocationJoin() {
        return locationJoin;
    }

    /**
     * Sets the value of the locationJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationSearchRowBasic }
     *     
     */
    public void setLocationJoin(LocationSearchRowBasic value) {
        this.locationJoin = value;
    }

    /**
     * Gets the value of the manufacturingCostTemplateJoin property.
     * 
     * @return
     *     possible object is
     *     {@link ManufacturingCostTemplateSearchRowBasic }
     *     
     */
    public ManufacturingCostTemplateSearchRowBasic getManufacturingCostTemplateJoin() {
        return manufacturingCostTemplateJoin;
    }

    /**
     * Sets the value of the manufacturingCostTemplateJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManufacturingCostTemplateSearchRowBasic }
     *     
     */
    public void setManufacturingCostTemplateJoin(ManufacturingCostTemplateSearchRowBasic value) {
        this.manufacturingCostTemplateJoin = value;
    }

    /**
     * Gets the value of the manufacturingWorkCenterJoin property.
     * 
     * @return
     *     possible object is
     *     {@link EntityGroupSearchRowBasic }
     *     
     */
    public EntityGroupSearchRowBasic getManufacturingWorkCenterJoin() {
        return manufacturingWorkCenterJoin;
    }

    /**
     * Sets the value of the manufacturingWorkCenterJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityGroupSearchRowBasic }
     *     
     */
    public void setManufacturingWorkCenterJoin(EntityGroupSearchRowBasic value) {
        this.manufacturingWorkCenterJoin = value;
    }

    /**
     * Gets the value of the userJoin property.
     * 
     * @return
     *     possible object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public EmployeeSearchRowBasic getUserJoin() {
        return userJoin;
    }

    /**
     * Sets the value of the userJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public void setUserJoin(EmployeeSearchRowBasic value) {
        this.userJoin = value;
    }

    /**
     * Gets the value of the customSearchJoin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customSearchJoin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomSearchJoin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomSearchRowBasic }
     * 
     * 
     */
    public List<CustomSearchRowBasic> getCustomSearchJoin() {
        if (customSearchJoin == null) {
            customSearchJoin = new ArrayList<CustomSearchRowBasic>();
        }
        return this.customSearchJoin;
    }

}
