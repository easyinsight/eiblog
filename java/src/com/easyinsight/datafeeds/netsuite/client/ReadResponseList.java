
package com.easyinsight.datafeeds.netsuite.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReadResponseList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReadResponseList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="readResponse" type="{urn:messages_2014_1.platform.webservices.netsuite.com}ReadResponse" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReadResponseList", namespace = "urn:messages_2014_1.platform.webservices.netsuite.com", propOrder = {
    "readResponse"
})
public class ReadResponseList {

    protected List<ReadResponse> readResponse;

    /**
     * Gets the value of the readResponse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the readResponse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReadResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReadResponse }
     * 
     * 
     */
    public List<ReadResponse> getReadResponse() {
        if (readResponse == null) {
            readResponse = new ArrayList<ReadResponse>();
        }
        return this.readResponse;
    }

}
