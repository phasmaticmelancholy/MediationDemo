package com.example;

import com.example.schema.Newschema;
import com.example.schema.Oldschema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SchemaMediatorTest {

    Oldschema testXML;

    @Before
    public void setUp()
    {
        testXML = new Oldschema();
        testXML.setOldname("TEST");
        testXML.setOldid("1234");
    }

    @Test
    public void mediate()
    {
        Newschema actualResult = SchemaMediator.mediate(testXML);

        Assert.assertNull(actualResult.getNewdata());
        Assert.assertEquals(actualResult.getNewname(), "TEST");
        Assert.assertEquals(actualResult.getNewid(), "1234");
    }
}