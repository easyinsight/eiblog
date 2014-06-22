
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
 *         &lt;element name="reportAbsPath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="newReportDefn" type="{http://xmlns.oracle.com/oxp/service/PublicReportService}ReportDefinition"/>
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
    "reportAbsPath",
    "newReportDefn"
})
@XmlRootElement(name = "updateReportDefinition")
public class UpdateReportDefinition {

    @XmlElement(required = true)
    protected String reportAbsPath;
    @XmlElement(required = true)
    protected ReportDefinition newReportDefn;

    /**
     * Gets the value of the reportAbsPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportAbsPath() {
        return reportAbsPath;
    }

    /**
     * Sets the value of the reportAbsPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportAbsPath(String value) {
        this.reportAbsPath = value;
    }

    /**
     * Gets the value of the newReportDefn property.
     * 
     * @return
     *     possible object is
     *     {@link ReportDefinition }
     *     
     */
    public ReportDefinition getNewReportDefn() {
        return newReportDefn;
    }

    /**
     * Sets the value of the newReportDefn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportDefinition }
     *     
     */
    public void setNewReportDefn(ReportDefinition value) {
        this.newReportDefn = value;
    }

}
