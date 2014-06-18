
package com.easyinsight.datafeeds.oracle.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="getFolderContentsReturn" type="{http://xmlns.oracle.com/oxp/service/PublicReportService}CatalogContents"/>
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
    "getFolderContentsReturn"
})
@XmlRootElement(name = "getFolderContentsResponse")
public class GetFolderContentsResponse {

    @XmlElement(required = true)
    protected CatalogContents getFolderContentsReturn;

    /**
     * Gets the value of the getFolderContentsReturn property.
     * 
     * @return
     *     possible object is
     *     {@link CatalogContents }
     *     
     */
    public CatalogContents getGetFolderContentsReturn() {
        return getFolderContentsReturn;
    }

    /**
     * Sets the value of the getFolderContentsReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CatalogContents }
     *     
     */
    public void setGetFolderContentsReturn(CatalogContents value) {
        this.getFolderContentsReturn = value;
    }

}
