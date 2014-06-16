//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.16 at 11:26:31 AM BST 
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
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="Presentation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PresentationRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="presentationId" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="jspName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="cssExtension" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="maxSections" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="maxImagesInSection" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Style">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="StyleRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="styleId" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="cssFolder" type="{http://www.w3.org/2001/XMLSchema}string" />
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
 *       &lt;attribute name="lastChanged" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "presentation",
    "style"
})
@XmlRootElement(name = "GalleryOptions", namespace = "http://ws.fotowalla.com/GalleryOptions")
public class GalleryOptions {

    @XmlElement(name = "Presentation", namespace = "http://ws.fotowalla.com/GalleryOptions")
    protected GalleryOptions.Presentation presentation;
    @XmlElement(name = "Style", namespace = "http://ws.fotowalla.com/GalleryOptions")
    protected GalleryOptions.Style style;
    @XmlAttribute(name = "lastChanged")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastChanged;

    /**
     * Gets the value of the presentation property.
     * 
     * @return
     *     possible object is
     *     {@link GalleryOptions.Presentation }
     *     
     */
    public GalleryOptions.Presentation getPresentation() {
        return presentation;
    }

    /**
     * Sets the value of the presentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link GalleryOptions.Presentation }
     *     
     */
    public void setPresentation(GalleryOptions.Presentation value) {
        this.presentation = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link GalleryOptions.Style }
     *     
     */
    public GalleryOptions.Style getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link GalleryOptions.Style }
     *     
     */
    public void setStyle(GalleryOptions.Style value) {
        this.style = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="PresentationRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="presentationId" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="jspName" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="cssExtension" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="maxSections" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="maxImagesInSection" type="{http://www.w3.org/2001/XMLSchema}int" />
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
        "presentationRef"
    })
    public static class Presentation {

        @XmlElement(name = "PresentationRef", namespace = "http://ws.fotowalla.com/GalleryOptions")
        protected List<GalleryOptions.Presentation.PresentationRef> presentationRef;

        /**
         * Gets the value of the presentationRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the presentationRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPresentationRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GalleryOptions.Presentation.PresentationRef }
         * 
         * 
         */
        public List<GalleryOptions.Presentation.PresentationRef> getPresentationRef() {
            if (presentationRef == null) {
                presentationRef = new ArrayList<GalleryOptions.Presentation.PresentationRef>();
            }
            return this.presentationRef;
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
         *       &lt;attribute name="presentationId" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="jspName" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="cssExtension" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="maxSections" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="maxImagesInSection" type="{http://www.w3.org/2001/XMLSchema}int" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class PresentationRef {

            @XmlAttribute(name = "presentationId")
            protected Integer presentationId;
            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "description")
            protected String description;
            @XmlAttribute(name = "jspName")
            protected String jspName;
            @XmlAttribute(name = "cssExtension")
            protected String cssExtension;
            @XmlAttribute(name = "maxSections")
            protected Integer maxSections;
            @XmlAttribute(name = "maxImagesInSection")
            protected Integer maxImagesInSection;

            /**
             * Gets the value of the presentationId property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getPresentationId() {
                return presentationId;
            }

            /**
             * Sets the value of the presentationId property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setPresentationId(Integer value) {
                this.presentationId = value;
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
             * Gets the value of the description property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDescription() {
                return description;
            }

            /**
             * Sets the value of the description property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDescription(String value) {
                this.description = value;
            }

            /**
             * Gets the value of the jspName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getJspName() {
                return jspName;
            }

            /**
             * Sets the value of the jspName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setJspName(String value) {
                this.jspName = value;
            }

            /**
             * Gets the value of the cssExtension property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCssExtension() {
                return cssExtension;
            }

            /**
             * Sets the value of the cssExtension property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCssExtension(String value) {
                this.cssExtension = value;
            }

            /**
             * Gets the value of the maxSections property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getMaxSections() {
                return maxSections;
            }

            /**
             * Sets the value of the maxSections property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setMaxSections(Integer value) {
                this.maxSections = value;
            }

            /**
             * Gets the value of the maxImagesInSection property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getMaxImagesInSection() {
                return maxImagesInSection;
            }

            /**
             * Sets the value of the maxImagesInSection property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setMaxImagesInSection(Integer value) {
                this.maxImagesInSection = value;
            }

        }

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
     *         &lt;element name="StyleRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="styleId" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="cssFolder" type="{http://www.w3.org/2001/XMLSchema}string" />
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
        "styleRef"
    })
    public static class Style {

        @XmlElement(name = "StyleRef", namespace = "http://ws.fotowalla.com/GalleryOptions")
        protected List<GalleryOptions.Style.StyleRef> styleRef;

        /**
         * Gets the value of the styleRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the styleRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStyleRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GalleryOptions.Style.StyleRef }
         * 
         * 
         */
        public List<GalleryOptions.Style.StyleRef> getStyleRef() {
            if (styleRef == null) {
                styleRef = new ArrayList<GalleryOptions.Style.StyleRef>();
            }
            return this.styleRef;
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
         *       &lt;attribute name="styleId" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="cssFolder" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class StyleRef {

            @XmlAttribute(name = "styleId")
            protected Integer styleId;
            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "description")
            protected String description;
            @XmlAttribute(name = "cssFolder")
            protected String cssFolder;

            /**
             * Gets the value of the styleId property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getStyleId() {
                return styleId;
            }

            /**
             * Sets the value of the styleId property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setStyleId(Integer value) {
                this.styleId = value;
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
             * Gets the value of the description property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDescription() {
                return description;
            }

            /**
             * Sets the value of the description property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDescription(String value) {
                this.description = value;
            }

            /**
             * Gets the value of the cssFolder property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCssFolder() {
                return cssFolder;
            }

            /**
             * Sets the value of the cssFolder property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCssFolder(String value) {
                this.cssFolder = value;
            }

        }

    }

}
