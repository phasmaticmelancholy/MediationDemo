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

    private JAXBContext oldContext;
    private JAXBContext newContext;

    private Schema oldSchema;
    private Schema newSchema;

    public SchemaMediator()
    {
        initialize();
    }

    /**
     * Initializes necessary context and schema objects.
     */
    private void initialize()
    {
        newSchema = loadSchema(Newschema.class, "/NewSchema.xsd");
        oldSchema = loadSchema(Oldschema.class, "/OldSchema.xsd");

        newContext = loadContext(com.example.schema.ObjectFactory.class, Newschema.class);
        oldContext = loadContext(com.example.schema.ObjectFactory.class, Oldschema.class);
    }

    /**
     * Instantiates an XML file to a JAXBObject.
     * @param anOldStyleXML File containing an old schema style'd XML
     * @return {@link Oldschema}
     * @throws MediationException A schema validation error occurred
     */
    protected Oldschema unmarshal(File anOldStyleXML) throws MediationException
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
     * Transforms a JAXB object in the new schema style to a String that's then written to console.
     * @param newStyleXML {@link Newschema}
     * @throws MediationException A schema validation error occurred
     */
    protected void marshal(Newschema newStyleXML) throws MediationException
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
     * Takes a provided schema class and relative location to schema file and instantiates a Schema object.
     * @param schemaClass The class containing the actual schema definitions
     * @param schemaFilePath Relative location in resources directory of the XSD
     * @return {@link Schema}
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
     * Instantiates JAXBContext from given ObjectFactory classes.
     * @param classes ObjectFactories to use
     * @return {@link JAXBContext}
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
     * Mediates old style complex object to the new format.
     * @param anOldStyleData {@link Oldschema.Olddata} complex type
     * @return {@link Newschema.Newdata} complex type
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
            newStyleData.setNewvalue(anOldStyleData.getOldvalue().toLowerCase());
        }

        return newStyleData;
    }

    /**
     * Transfers old schema style'd JAXB object data into a new schema styled JAXB object.
     * @param anOldStyleXML {@link Oldschema} old data
     * @return {@link Newschema} new data
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
     * Main runner for program; expects 1 argument containing the path to a file containing an XML that uses the
     * OldSchema.xsd.
     *
     * //TODO Make this into a runnable jar
     * //TODO Make exceptions clearer
     * //TODO Add ability to take folder in as input
     * //TODO Add Apache Karaf functionality
     * @param args Commandline arguments
     * @throws MediationException A schema validation exception has occurred.
     */
    public static void main(String[] args) throws MediationException {

        if(args.length > 0) {
            SchemaMediator sm = new SchemaMediator();

            File oldStyleXMLFile = Paths.get(args[0]).toFile();
            Oldschema oldStyleXML = sm.unmarshal(oldStyleXMLFile);
            Newschema newStyleXML = mediate(oldStyleXML);
            sm.marshal(newStyleXML);
        }
        else
        {
            System.out.println("Please provide the path to an XML file to mediate.");
        }
    }
}