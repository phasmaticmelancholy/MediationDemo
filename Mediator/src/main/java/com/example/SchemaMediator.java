package com.example;

import com.example.exception.MediationException;
import com.example.schema.Oldschema;
import com.example.schema.Newschema;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.nio.file.Paths;

/**
 * Mediates XML using OldSchema style to NewSchema style, performs schema validation on both sides.
 */
public class SchemaMediator {

    private static JAXBContext oldContext;
    private static JAXBContext newContext;

    private static Schema oldSchema;
    private static Schema newSchema;

    /**
     *
     * @param schemaClass
     * @param schemaFilePath
     * @return
     */
    private static Schema loadSchema(Class schemaClass, String schemaFilePath)
    {
        Schema schema = null;

        try
        {
            schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaClass.getResource(schemaFilePath));
        }
        catch (SAXException e)
        {
            System.out.println("Schema failed to instantiate:" + e.getMessage());
        }

        return schema;
    }

    /**
     *
     * @param classes
     * @return
     */
    private static JAXBContext loadContext(Class... classes)
    {
        JAXBContext jaxbContext = null;

        try {
            jaxbContext = JAXBContext.newInstance(classes);
        } catch (JAXBException e)
        {
            System.out.println("JAXBContext failed to instantiate:" + e.getMessage());
        }

        return jaxbContext;
    }

    /**
     *
     * @param anOldStyleData
     * @return
     */
    private static Newschema.Newdata mediate(Oldschema.Olddata anOldStyleData)
    {
        Newschema.Newdata newStyleData = new Newschema.Newdata();

        if(anOldStyleData.getOldvalue() != null)
        {
            newStyleData.setNewtime(anOldStyleData.getOldtime());
        }

        if(anOldStyleData.getOldvalue() != null)
        {
            newStyleData.setNewvalue(anOldStyleData.getOldvalue());
        }

        return newStyleData;
    }

    /**
     *
     * @param anOldStyleXML
     * @return
     */
    public static Newschema mediate(Oldschema anOldStyleXML)
    {
        Newschema newStyleXML = new Newschema();
        if(anOldStyleXML.getOldid() != null)
        {
            newStyleXML.setNewid(anOldStyleXML.getOldid());
        }

        if(anOldStyleXML.getOldname() != null)
        {
            newStyleXML.setNewname(anOldStyleXML.getOldname());
        }

        if(anOldStyleXML.getOlddata() != null)
        {
            newStyleXML.setNewdata(mediate(anOldStyleXML.getOlddata()));
        }

        return newStyleXML;
    }

    /**
     *
     */
    private static void initialize()
    {
        newSchema = loadSchema(Newschema.class, "/NewSchema.xsd");
        oldSchema = loadSchema(Oldschema.class, "/OldSchema.xsd");

        newContext = loadContext(com.example.schema.ObjectFactory.class, Newschema.class);
        oldContext = loadContext(com.example.schema.ObjectFactory.class, Oldschema.class);
    }

    /**
     *
     * @param anOldStyleXML
     * @return
     * @throws MediationException
     */
    private static Oldschema unmarshal(File anOldStyleXML) throws MediationException
    {
        try
        {
            Unmarshaller oldSchemaUnmarshaller = oldContext.createUnmarshaller();
            oldSchemaUnmarshaller.setSchema(oldSchema);

            return (Oldschema) oldSchemaUnmarshaller.unmarshal(anOldStyleXML);
        }
        catch (JAXBException e)
        {
            throw new MediationException();
        }
    }

    /**
     *
     * @param newStyleXML
     * @throws MediationException
     */
    private static void marshal(Newschema newStyleXML) throws MediationException
    {
        try
        {
            Marshaller marshaller = newContext.createMarshaller();
            marshaller.setSchema(newSchema);
            marshaller.marshal(newStyleXML, System.out);
        }
        catch (JAXBException e)
        {
            throw new MediationException();
        }
    }

    /**
     *
     * @param args
     * @throws MediationException
     */
    public static void main(String[] args) throws MediationException {

        if(args.length > 0) {
            initialize();

            File oldStyleXMLFile = Paths.get(args[0]).toFile();
            Oldschema oldStyleXML = unmarshal(oldStyleXMLFile);
            Newschema newStyleXML = mediate(oldStyleXML);
            marshal(newStyleXML);
        }
        else
        {
            System.out.println("Please provide the path to an XML file to mediate.");
        }
    }
}