//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.13 at 07:25:44 PM BST 
//


package walla.datatypes.auto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="AppId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="PlatformId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MachineName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LastUsed" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="TagId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="UserAppCategoryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="UserDefaultCategoryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="GalleryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="ThumbCacheSizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MainCopyCacheSizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="FetchSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MainCopyFolder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AutoUploadFolder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AutoUpload" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "appId",
    "platformId",
    "machineName",
    "lastUsed",
    "tagId",
    "userAppCategoryId",
    "userDefaultCategoryId",
    "galleryId",
    "thumbCacheSizeMB",
    "mainCopyCacheSizeMB",
    "fetchSize",
    "mainCopyFolder",
    "autoUploadFolder",
    "autoUpload"
})
@XmlRootElement(name = "UserApp", namespace = "http://www.example.org/UserApp")
public class UserApp {

    @XmlElement(name = "AppId", namespace = "http://www.example.org/UserApp")
    protected Integer appId;
    @XmlElement(name = "PlatformId", namespace = "http://www.example.org/UserApp")
    protected Integer platformId;
    @XmlElement(name = "MachineName", namespace = "http://www.example.org/UserApp")
    protected String machineName;
    @XmlElement(name = "LastUsed", namespace = "http://www.example.org/UserApp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastUsed;
    @XmlElement(name = "TagId", namespace = "http://www.example.org/UserApp", defaultValue = "0")
    protected Long tagId;
    @XmlElement(name = "UserAppCategoryId", namespace = "http://www.example.org/UserApp", defaultValue = "0")
    protected Long userAppCategoryId;
    @XmlElement(name = "UserDefaultCategoryId", namespace = "http://www.example.org/UserApp", defaultValue = "0")
    protected Long userDefaultCategoryId;
    @XmlElement(name = "GalleryId", namespace = "http://www.example.org/UserApp", defaultValue = "0")
    protected Long galleryId;
    @XmlElement(name = "ThumbCacheSizeMB", namespace = "http://www.example.org/UserApp")
    protected Integer thumbCacheSizeMB;
    @XmlElement(name = "MainCopyCacheSizeMB", namespace = "http://www.example.org/UserApp")
    protected Integer mainCopyCacheSizeMB;
    @XmlElement(name = "FetchSize", namespace = "http://www.example.org/UserApp")
    protected Integer fetchSize;
    @XmlElement(name = "MainCopyFolder", namespace = "http://www.example.org/UserApp")
    protected String mainCopyFolder;
    @XmlElement(name = "AutoUploadFolder", namespace = "http://www.example.org/UserApp")
    protected String autoUploadFolder;
    @XmlElement(name = "AutoUpload", namespace = "http://www.example.org/UserApp")
    protected Boolean autoUpload;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "version")
    protected Integer version;

    /**
     * Gets the value of the appId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAppId() {
        return appId;
    }

    /**
     * Sets the value of the appId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAppId(Integer value) {
        this.appId = value;
    }

    /**
     * Gets the value of the platformId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPlatformId() {
        return platformId;
    }

    /**
     * Sets the value of the platformId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPlatformId(Integer value) {
        this.platformId = value;
    }

    /**
     * Gets the value of the machineName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMachineName() {
        return machineName;
    }

    /**
     * Sets the value of the machineName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMachineName(String value) {
        this.machineName = value;
    }

    /**
     * Gets the value of the lastUsed property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastUsed() {
        return lastUsed;
    }

    /**
     * Sets the value of the lastUsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastUsed(XMLGregorianCalendar value) {
        this.lastUsed = value;
    }

    /**
     * Gets the value of the tagId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTagId() {
        return tagId;
    }

    /**
     * Sets the value of the tagId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTagId(Long value) {
        this.tagId = value;
    }

    /**
     * Gets the value of the userAppCategoryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUserAppCategoryId() {
        return userAppCategoryId;
    }

    /**
     * Sets the value of the userAppCategoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUserAppCategoryId(Long value) {
        this.userAppCategoryId = value;
    }

    /**
     * Gets the value of the userDefaultCategoryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUserDefaultCategoryId() {
        return userDefaultCategoryId;
    }

    /**
     * Sets the value of the userDefaultCategoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUserDefaultCategoryId(Long value) {
        this.userDefaultCategoryId = value;
    }

    /**
     * Gets the value of the galleryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getGalleryId() {
        return galleryId;
    }

    /**
     * Sets the value of the galleryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setGalleryId(Long value) {
        this.galleryId = value;
    }

    /**
     * Gets the value of the thumbCacheSizeMB property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getThumbCacheSizeMB() {
        return thumbCacheSizeMB;
    }

    /**
     * Sets the value of the thumbCacheSizeMB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setThumbCacheSizeMB(Integer value) {
        this.thumbCacheSizeMB = value;
    }

    /**
     * Gets the value of the mainCopyCacheSizeMB property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMainCopyCacheSizeMB() {
        return mainCopyCacheSizeMB;
    }

    /**
     * Sets the value of the mainCopyCacheSizeMB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMainCopyCacheSizeMB(Integer value) {
        this.mainCopyCacheSizeMB = value;
    }

    /**
     * Gets the value of the fetchSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFetchSize() {
        return fetchSize;
    }

    /**
     * Sets the value of the fetchSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFetchSize(Integer value) {
        this.fetchSize = value;
    }

    /**
     * Gets the value of the mainCopyFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainCopyFolder() {
        return mainCopyFolder;
    }

    /**
     * Sets the value of the mainCopyFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainCopyFolder(String value) {
        this.mainCopyFolder = value;
    }

    /**
     * Gets the value of the autoUploadFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutoUploadFolder() {
        return autoUploadFolder;
    }

    /**
     * Sets the value of the autoUploadFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoUploadFolder(String value) {
        this.autoUploadFolder = value;
    }

    /**
     * Gets the value of the autoUpload property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoUpload() {
        return autoUpload;
    }

    /**
     * Sets the value of the autoUpload property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoUpload(Boolean value) {
        this.autoUpload = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getId() {
        if (id == null) {
            return  0L;
        } else {
            return id;
        }
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getVersion() {
        if (version == null) {
            return  0;
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

}
