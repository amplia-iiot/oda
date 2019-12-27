package es.amplia.oda.core.commons.entities;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContentTypeTest {

    @Test
    public void testGetContentTypeUsingInnerContentTypeValueOf() {
        assertEquals(ContentType.CBOR, ContentType.getContentType("CBOR"));
    }

    @Test
    public void testGetContentTypeWithMessagePack() {
        assertEquals(ContentType.MESSAGE_PACK, ContentType.getContentType("messagePack"));
    }

    @Test
    public void testGetContentTypeWithMsgPack() {
        assertEquals(ContentType.MESSAGE_PACK, ContentType.getContentType("msgPack"));
    }

    @Test
    public void testGetContentTypeWithLowerCaseDescription() {
        assertEquals(ContentType.JSON, ContentType.getContentType("json"));
    }
}