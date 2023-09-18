package com.example;

import com.example.exception.MediationException;
import com.example.schema.Newschema;
import com.example.schema.Oldschema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.Instant;

public class SchemaMediatorTest {

    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();

    SchemaMediator mediator;
    Oldschema mediateTestXML;
    Oldschema failToMarshalMissingId;
    Oldschema failToMarshalInvalidData;

    public SchemaMediatorTest() throws DatatypeConfigurationException {
    }

    @Before
    public void setUp()
    {
        mediator = new SchemaMediator();

        mediateTestXML = new Oldschema();
        mediateTestXML.setOldname("TEST");
        mediateTestXML.setOldid("1234");
        mediateTestXML.setOlddata(new Oldschema.Olddata());
        mediateTestXML.getOlddata().setOldtime(
                datatypeFactory.newXMLGregorianCalendar(Instant.now().toString()));
        mediateTestXML.getOlddata().setOldvalue("ABCDEF");

        failToMarshalMissingId = new Oldschema();
        failToMarshalMissingId.setOldname("TEST");

        failToMarshalInvalidData = new Oldschema();
        failToMarshalInvalidData.setOldname("TEST");
        failToMarshalInvalidData.setOldid("1234");
        failToMarshalInvalidData.setOlddata(new Oldschema.Olddata());
        failToMarshalInvalidData.getOlddata().setOldtime(
                datatypeFactory.newXMLGregorianCalendar(Instant.now().toString()));
        failToMarshalInvalidData.getOlddata().setOldvalue("INVALID_VALUE");
    }

    @Test
    public void mediate()
    {
        Newschema actualResult = SchemaMediator.mediate(mediateTestXML);

        Assert.assertEquals("Newname was not mediated expectedly", actualResult.getNewname(), "TEST");
        Assert.assertEquals("Newid was not mediated expectedly", actualResult.getNewid(), "1234");
        Assert.assertNotNull("Newdata is missing", actualResult.getNewdata());
        Assert.assertEquals("Newdata Newvalue was not mediated expectedly",
                actualResult.getNewdata().getNewvalue(), "abcdef");

        try
        {
            mediator.marshal(actualResult);
        }
        catch (MediationException e)
        {
            Assert.fail("Failed to marshal valid message");
        }
    }

    @Test
    public void failToMarshal()
    {
        // NewSchema has Newid set as a required value
        Newschema actualResultMissingId = SchemaMediator.mediate(failToMarshalMissingId);
        Assert.assertThrows("Marshaller did not catch missing id",
                MediationException.class, () -> mediator.marshal(actualResultMissingId));

        // "INVALID_VALUE" does not match pattern [a-f]{6}
        Newschema actualResultInvalidData = SchemaMediator.mediate(failToMarshalInvalidData);
        Assert.assertThrows("Marshaller did not catch invalid Newdata Newvalue",
                MediationException.class, () -> mediator.marshal(actualResultInvalidData));
    }
}