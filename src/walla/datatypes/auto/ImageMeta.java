//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.29 at 07:28:25 PM BST 
//


package walla.datatypes.auto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OriginalFileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Format" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UserAppId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Width" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Height" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Size" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="CameraMaker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CameraModel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Aperture" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ShutterSpeed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ISO" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Orientation" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="TakenDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="TakenDateSet" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="TakenDateFile" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="TakenDateMeta" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="UploadDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="UdfChar1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UdfChar2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UdfChar3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UdfText1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UdfNum1" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="UdfNum2" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="UdfNum3" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="UdfDate1" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="UdfDate2" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="UdfDate3" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Tags">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="TagRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *                           &lt;attribute name="op" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="categoryId" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "desc",
    "originalFileName",
    "format",
    "userAppId",
    "status",
    "width",
    "height",
    "size",
    "cameraMaker",
    "cameraModel",
    "aperture",
    "shutterSpeed",
    "iso",
    "orientation",
    "takenDate",
    "takenDateSet",
    "takenDateFile",
    "takenDateMeta",
    "uploadDate",
    "udfChar1",
    "udfChar2",
    "udfChar3",
    "udfText1",
    "udfNum1",
    "udfNum2",
    "udfNum3",
    "udfDate1",
    "udfDate2",
    "udfDate3",
    "tags"
})
@XmlRootElement(name = "ImageMeta", namespace = "http://ws.fotowalla.com/ImageMeta")
public class ImageMeta {

    @XmlElement(name = "Name", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String name;
    @XmlElement(name = "Desc", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String desc;
    @XmlElement(name = "OriginalFileName", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String originalFileName;
    @XmlElement(name = "Format", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String format;
    @XmlElement(name = "UserAppId", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected Long userAppId;
    @XmlElement(name = "Status", namespace = "http://ws.fotowalla.com/ImageMeta", defaultValue = "0")
    protected Integer status;
    @XmlElement(name = "Width", namespace = "http://ws.fotowalla.com/ImageMeta", defaultValue = "0")
    protected Integer width;
    @XmlElement(name = "Height", namespace = "http://ws.fotowalla.com/ImageMeta", defaultValue = "0")
    protected Integer height;
    @XmlElement(name = "Size", namespace = "http://ws.fotowalla.com/ImageMeta", defaultValue = "0")
    protected Long size;
    @XmlElement(name = "CameraMaker", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String cameraMaker;
    @XmlElement(name = "CameraModel", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String cameraModel;
    @XmlElement(name = "Aperture", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String aperture;
    @XmlElement(name = "ShutterSpeed", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String shutterSpeed;
    @XmlElement(name = "ISO", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected Integer iso;
    @XmlElement(name = "Orientation", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected Integer orientation;
    @XmlElement(name = "TakenDate", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar takenDate;
    @XmlElement(name = "TakenDateSet", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected Boolean takenDateSet;
    @XmlElement(name = "TakenDateFile", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar takenDateFile;
    @XmlElement(name = "TakenDateMeta", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar takenDateMeta;
    @XmlElement(name = "UploadDate", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar uploadDate;
    @XmlElement(name = "UdfChar1", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String udfChar1;
    @XmlElement(name = "UdfChar2", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String udfChar2;
    @XmlElement(name = "UdfChar3", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String udfChar3;
    @XmlElement(name = "UdfText1", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected String udfText1;
    @XmlElement(name = "UdfNum1", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected BigDecimal udfNum1;
    @XmlElement(name = "UdfNum2", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected BigDecimal udfNum2;
    @XmlElement(name = "UdfNum3", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected BigDecimal udfNum3;
    @XmlElement(name = "UdfDate1", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar udfDate1;
    @XmlElement(name = "UdfDate2", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar udfDate2;
    @XmlElement(name = "UdfDate3", namespace = "http://ws.fotowalla.com/ImageMeta")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar udfDate3;
    @XmlElement(name = "Tags", namespace = "http://ws.fotowalla.com/ImageMeta")
    protected ImageMeta.Tags tags;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "version")
    protected Integer version;
    @XmlAttribute(name = "categoryId")
    protected Long categoryId;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the originalFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Sets the value of the originalFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalFileName(String value) {
        this.originalFileName = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the userAppId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUserAppId() {
        return userAppId;
    }

    /**
     * Sets the value of the userAppId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUserAppId(Long value) {
        this.userAppId = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStatus(Integer value) {
        this.status = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWidth(Integer value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHeight(Integer value) {
        this.height = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSize(Long value) {
        this.size = value;
    }

    /**
     * Gets the value of the cameraMaker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCameraMaker() {
        return cameraMaker;
    }

    /**
     * Sets the value of the cameraMaker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCameraMaker(String value) {
        this.cameraMaker = value;
    }

    /**
     * Gets the value of the cameraModel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCameraModel() {
        return cameraModel;
    }

    /**
     * Sets the value of the cameraModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCameraModel(String value) {
        this.cameraModel = value;
    }

    /**
     * Gets the value of the aperture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAperture() {
        return aperture;
    }

    /**
     * Sets the value of the aperture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAperture(String value) {
        this.aperture = value;
    }

    /**
     * Gets the value of the shutterSpeed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShutterSpeed() {
        return shutterSpeed;
    }

    /**
     * Sets the value of the shutterSpeed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShutterSpeed(String value) {
        this.shutterSpeed = value;
    }

    /**
     * Gets the value of the iso property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getISO() {
        return iso;
    }

    /**
     * Sets the value of the iso property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setISO(Integer value) {
        this.iso = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrientation(Integer value) {
        this.orientation = value;
    }

    /**
     * Gets the value of the takenDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTakenDate() {
        return takenDate;
    }

    /**
     * Sets the value of the takenDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTakenDate(XMLGregorianCalendar value) {
        this.takenDate = value;
    }

    /**
     * Gets the value of the takenDateSet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTakenDateSet() {
        return takenDateSet;
    }

    /**
     * Sets the value of the takenDateSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTakenDateSet(Boolean value) {
        this.takenDateSet = value;
    }

    /**
     * Gets the value of the takenDateFile property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTakenDateFile() {
        return takenDateFile;
    }

    /**
     * Sets the value of the takenDateFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTakenDateFile(XMLGregorianCalendar value) {
        this.takenDateFile = value;
    }

    /**
     * Gets the value of the takenDateMeta property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTakenDateMeta() {
        return takenDateMeta;
    }

    /**
     * Sets the value of the takenDateMeta property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTakenDateMeta(XMLGregorianCalendar value) {
        this.takenDateMeta = value;
    }

    /**
     * Gets the value of the uploadDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUploadDate() {
        return uploadDate;
    }

    /**
     * Sets the value of the uploadDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUploadDate(XMLGregorianCalendar value) {
        this.uploadDate = value;
    }

    /**
     * Gets the value of the udfChar1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUdfChar1() {
        return udfChar1;
    }

    /**
     * Sets the value of the udfChar1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUdfChar1(String value) {
        this.udfChar1 = value;
    }

    /**
     * Gets the value of the udfChar2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUdfChar2() {
        return udfChar2;
    }

    /**
     * Sets the value of the udfChar2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUdfChar2(String value) {
        this.udfChar2 = value;
    }

    /**
     * Gets the value of the udfChar3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUdfChar3() {
        return udfChar3;
    }

    /**
     * Sets the value of the udfChar3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUdfChar3(String value) {
        this.udfChar3 = value;
    }

    /**
     * Gets the value of the udfText1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUdfText1() {
        return udfText1;
    }

    /**
     * Sets the value of the udfText1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUdfText1(String value) {
        this.udfText1 = value;
    }

    /**
     * Gets the value of the udfNum1 property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getUdfNum1() {
        return udfNum1;
    }

    /**
     * Sets the value of the udfNum1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setUdfNum1(BigDecimal value) {
        this.udfNum1 = value;
    }

    /**
     * Gets the value of the udfNum2 property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getUdfNum2() {
        return udfNum2;
    }

    /**
     * Sets the value of the udfNum2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setUdfNum2(BigDecimal value) {
        this.udfNum2 = value;
    }

    /**
     * Gets the value of the udfNum3 property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getUdfNum3() {
        return udfNum3;
    }

    /**
     * Sets the value of the udfNum3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setUdfNum3(BigDecimal value) {
        this.udfNum3 = value;
    }

    /**
     * Gets the value of the udfDate1 property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUdfDate1() {
        return udfDate1;
    }

    /**
     * Sets the value of the udfDate1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUdfDate1(XMLGregorianCalendar value) {
        this.udfDate1 = value;
    }

    /**
     * Gets the value of the udfDate2 property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUdfDate2() {
        return udfDate2;
    }

    /**
     * Sets the value of the udfDate2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUdfDate2(XMLGregorianCalendar value) {
        this.udfDate2 = value;
    }

    /**
     * Gets the value of the udfDate3 property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUdfDate3() {
        return udfDate3;
    }

    /**
     * Sets the value of the udfDate3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUdfDate3(XMLGregorianCalendar value) {
        this.udfDate3 = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link ImageMeta.Tags }
     *     
     */
    public ImageMeta.Tags getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImageMeta.Tags }
     *     
     */
    public void setTags(ImageMeta.Tags value) {
        this.tags = value;
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

    /**
     * Gets the value of the categoryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCategoryId() {
        if (categoryId == null) {
            return  0L;
        } else {
            return categoryId;
        }
    }

    /**
     * Sets the value of the categoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCategoryId(Long value) {
        this.categoryId = value;
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
     *         &lt;element name="TagRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
     *                 &lt;attribute name="op" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
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
        "tagRef"
    })
    public static class Tags {

        @XmlElement(name = "TagRef", namespace = "http://ws.fotowalla.com/ImageMeta")
        protected List<ImageMeta.Tags.TagRef> tagRef;

        /**
         * Gets the value of the tagRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the tagRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTagRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ImageMeta.Tags.TagRef }
         * 
         * 
         */
        public List<ImageMeta.Tags.TagRef> getTagRef() {
            if (tagRef == null) {
                tagRef = new ArrayList<ImageMeta.Tags.TagRef>();
            }
            return this.tagRef;
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
         *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
         *       &lt;attribute name="op" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class TagRef {

            @XmlAttribute(name = "id")
            protected Long id;
            @XmlAttribute(name = "op")
            protected String op;
            @XmlAttribute(name = "name")
            protected String name;

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
             * Gets the value of the op property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOp() {
                return op;
            }

            /**
             * Sets the value of the op property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOp(String value) {
                this.op = value;
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

        }

    }

}
