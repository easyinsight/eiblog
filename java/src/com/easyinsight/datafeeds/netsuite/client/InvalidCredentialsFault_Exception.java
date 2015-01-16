
package com.easyinsight.datafeeds.netsuite.client;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "invalidCredentialsFault", targetNamespace = "urn:faults_2014_1.platform.webservices.netsuite.com")
public class InvalidCredentialsFault_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private InvalidCredentialsFault faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public InvalidCredentialsFault_Exception(String message, InvalidCredentialsFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public InvalidCredentialsFault_Exception(String message, InvalidCredentialsFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.easyinsight.datafeeds.netsuite.client.InvalidCredentialsFault
     */
    public InvalidCredentialsFault getFaultInfo() {
        return faultInfo;
    }

}
