//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.22 at 06:56:23 PM GMT 
//


package walla.datatypes.auto;

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
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LastChanged" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="SystemOwned" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Images">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ImageRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                           &lt;attribute name="categoryId" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="desc" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="uploadDate" type="{http://www.w3.org/2001/XMLSchema}date" />
 *                           &lt;attribute name="takenDate" type="{http://www.w3.org/2001/XMLSchema}date" />
 *                           &lt;attribute name="metaVersion" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="shotSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="fileSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
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
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sectionId" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="imageCursor" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="imageCount" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="sectionImageCount" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="totalImageCount" type="{http://www.w3.org/2001/XMLSchema}int" />
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
    "lastChanged",
    "systemOwned",
    "images"
})
@XmlRootElement(name = "ImageList", namespace = "http://www.example.org/ImageList")
public class ImageList {

    @XmlElement(name = "Name", namespace = "http://www.example.org/ImageList", required = true)
    protected String name;
    @XmlElement(name = "Desc", namespace = "http://www.example.org/ImageList", required = true)
    protected String desc;
    @XmlElement(name = "LastChanged", namespace = "http://www.example.org/ImageList", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastChanged;
    @XmlElement(name = "SystemOwned", namespace = "http://www.example.org/ImageList")
    protected boolean systemOwned;
    @XmlElement(name = "Images", namespace = "http://www.example.org/ImageList", required = true)
    protected ImageList.Images images;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "sectionId")
    protected Long sectionId;
    @XmlAttribute(name = "version")
    protected Integer version;
    @XmlAttribute(name = "imageCursor")
    protected Integer imageCursor;
    @XmlAttribute(name = "imageCount")
    protected Integer imageCount;
    @XmlAttribute(name = "sectionImageCount")
    protected Integer sectionImageCount;
    @XmlAttribute(name = "totalImageCount")
    protected Integer totalImageCount;

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
     * Gets the value of the lastChanged property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the value of the lastChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastChanged(XMLGregorianCalendar value) {
        this.lastChanged = value;
    }

    /**
     * Gets the value of the systemOwned property.
     * 
     */
    public boolean isSystemOwned() {
        return systemOwned;
    }

    /**
     * Sets the value of the systemOwned property.
     * 
     */
    public void setSystemOwned(boolean value) {
        this.systemOwned = value;
    }

    /**
     * Gets the value of the images property.
     * 
     * @return
     *     possible object is
     *     {@link ImageList.Images }
     *     
     */
    public ImageList.Images getImages() {
        return images;
    }

    /**
     * Sets the value of the images property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImageList.Images }
     *     
     */
    public void setImages(ImageList.Images value) {
        this.images = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the sectionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSectionId() {
        return sectionId;
    }

    /**
     * Sets the value of the sectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSectionId(Long value) {
        this.sectionId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVersion() {
        return version;
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
     * Gets the value of the imageCursor property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageCursor() {
        return imageCursor;
    }

    /**
     * Sets the value of the imageCursor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageCursor(Integer value) {
        this.imageCursor = value;
    }

    /**
     * Gets the value of the imageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageCount() {
        return imageCount;
    }

    /**
     * Sets the value of the imageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageCount(Integer value) {
        this.imageCount = value;
    }

    /**
     * Gets the value of the sectionImageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSectionImageCount() {
        return sectionImageCount;
    }

    /**
     * Sets the value of the sectionImageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSectionImageCount(Integer value) {
        this.sectionImageCount = value;
    }

    /**
     * Gets the value of the totalImageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalImageCount() {
        return totalImageCount;
    }

    /**
     * Sets the value of the totalImageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalImageCount(Integer value) {
        this.totalImageCount = value;
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
     *         &lt;element name="ImageRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
     *                 &lt;attribute name="categoryId" type="{http://www.w3.org/2001/XMLSchema}long" />
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="desc" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="uploadDate" type="{http://www.w3.org/2001/XMLSchema}date" />
     *                 &lt;attribute name="takenDate" type="{http://www.w3.org/2001/XMLSchema}date" />
     *                 &lt;attribute name="metaVersion" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="shotSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="fileSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
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
        "imageRef"
    })
    public static class Images {

        @XmlElement(name = "ImageRef", namespace = "http://www.example.org/ImageList")
        protected List<ImageList.Images.ImageRef> imageRef;

        /**
         * Gets the value of the imageRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the imageRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getImageRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ImageList.Images.ImageRef }
         * 
         * 
         */
        public List<ImageList.Images.ImageRef> getImageRef() {
            if (imageRef == null) {
                imageRef = new ArrayList<ImageList.Images.ImageRef>();
            }
            return this.imageRef;
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
         *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
         *       &lt;attribute name="categoryId" type="{http://www.w3.org/2001/XMLSchema}long" />
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="desc" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="uploadDate" type="{http://www.w3.org/2001/XMLSchema}date" />
         *       &lt;attribute name="takenDate" type="{http://www.w3.org/2001/XMLSchema}date" />
         *       &lt;attribute name="metaVersion" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="shotSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="fileSummary" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ImageRef {

            @XmlAttribute(name = "id")
            protected Long id;
            @XmlAttribute(name = "categoryId")
            protected Long categoryId;
            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "desc")
            protected String desc;
            @XmlAttribute(name = "uploadDate")
            @XmlSchemaType(name = "date")
            protected XMLGregorianCalendar uploadDate;
            @XmlAttribute(name = "takenDate")
            @XmlSchemaType(name = "date")
            protected XMLGregorianCalendar takenDate;
            @XmlAttribute(name = "metaVersion")
            protected Integer metaVersion;
            @XmlAttribute(name = "shotSummary")
            protected String shotSummary;
            @XmlAttribute(name = "fileSummary")
            protected String fileSummary;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link Long }
             *     
             */
            public Long getId() {
                return id;
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
             * Gets the value of the categoryId property.
             * 
             * @return
             *     possible object is
             *     {@link Long }
             *     
             */
            public Long getCategoryId() {
                return categoryId;
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
             * Gets the value of the metaVersion property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getMetaVersion() {
                return metaVersion;
            }

            /**
             * Sets the value of the metaVersion property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setMetaVersion(Integer value) {
                this.metaVersion = value;
            }

            /**
             * Gets the value of the shotSummary property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShotSummary() {
                return shotSummary;
            }

            /**
             * Sets the value of the shotSummary property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShotSummary(String value) {
                this.shotSummary = value;
            }

            /**
             * Gets the value of the fileSummary property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFileSummary() {
                return fileSummary;
            }

            /**
             * Sets the value of the fileSummary property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFileSummary(String value) {
                this.fileSummary = value;
            }

        }

    }

}
