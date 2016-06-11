package sh.calaba.json;

import android.content.ComponentName;
import android.content.Intent;

import android.net.Uri;
import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;

import sh.calaba.instrumentationbackend.json.JSONUtils;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntentTest {
    private static String json = "{\n" +
            "  \"action\": \"My Action\",\n" +
            "  \"data\": \"http://example.com/test\",\n" +
            "  \"flags\": 12345,\n" +
            "  \"component\": {\n" +
                "  \"packageName\": \"com.mypackage\",\n" +
                "  \"className\": \"MyClass\"\n" +
            "  }\n" +
            "}";

    private ObjectMapper myObjectMapper;
    private Intent expectedIntent;
    private Intent serializedIntent;

    @Before
    public void setUp() throws Exception {
        myObjectMapper = JSONUtils.calabashObjectMapper();
        expectedIntent = new Intent();
        expectedIntent.setAction("My Action");
        expectedIntent.setData(Uri.parse("http://example.com/test"));
        expectedIntent.setComponent(new ComponentName("com.mypackage", "MyClass"));
        expectedIntent.setFlags(12345);
        serializedIntent = myObjectMapper.readValue(json, Intent.class);
    }

    @Test
    public void serializationOfAction() throws Exception {
        assertEquals(expectedIntent.getAction(), serializedIntent.getAction());
    }

    @Test
    public void serializationOfData() throws Exception {
        assertEquals(expectedIntent.getData(), serializedIntent.getData());
    }

    @Test
    public void serializationOfComponent() throws Exception {
        assertEquals(expectedIntent.getComponent(), serializedIntent.getComponent());
    }

    @Test
    public void serializationOfFlags() throws Exception {
        assertEquals(expectedIntent.getFlags(), serializedIntent.getFlags());
    }

    @Test
    public void serializationOfType() throws Exception {
        Intent expectedIntent = new Intent();
        expectedIntent.setType("my-type/intent");
        Intent parsedIntent = myObjectMapper.readValue("{\"type\":\"my-type/intent\"}", Intent.class);
        assertEquals(expectedIntent.getType(), parsedIntent.getType());
    }

    @Test
    public void serializationOfSimpleExtras() throws Exception {
        Intent expectedIntent = new Intent();
        expectedIntent.putExtra("extra1", "Hello");
        expectedIntent.putExtra("extra2", 12);
        Intent parsedIntent = myObjectMapper.readValue("{\"extras\":{\"extra1\":\"Hello\",\"extra2\":12}}", Intent.class);
        assertEquals(expectedIntent.getExtras().get("extra1"), parsedIntent.getExtras().get("extra1"));
        assertEquals(expectedIntent.getExtras().get("extra2"), parsedIntent.getExtras().get("extra2"));
    }

    @Test
    public void serializationOfCompositeExtras() throws Exception {
        Intent expectedIntent = new Intent();
        expectedIntent.putExtra("extra1", new String[] {"Hello", "world"});
        expectedIntent.putExtra("extra2", new int[] {12,13,14});
        Intent parsedIntent = myObjectMapper.readValue("{\"extras\":{\"extra1\":[\"Hello\",\"world\"],\"extra2\":[12,13,14]}}", Intent.class);

        assertEquals(((Object[])expectedIntent.getExtras().get("extra1"))[0], ((Object[])parsedIntent.getExtras().get("extra1"))[0]);
        assertEquals(((Object[])expectedIntent.getExtras().get("extra1"))[1], ((Object[])parsedIntent.getExtras().get("extra1"))[1]);
        assertEquals(((int[])expectedIntent.getExtras().get("extra2"))[0], ((Object[])parsedIntent.getExtras().get("extra2"))[0]);
        assertEquals(((int[])expectedIntent.getExtras().get("extra2"))[1], ((Object[])parsedIntent.getExtras().get("extra2"))[1]);
        assertEquals(((int[])expectedIntent.getExtras().get("extra2"))[2], ((Object[])parsedIntent.getExtras().get("extra2"))[2]);
    }

    @Test
    public void serializationOfBothDataAndTypeShouldFail() throws Exception {
        try {
            myObjectMapper.readValue("{\"type\":\"foo\",\"data\":\"bar\"}", Intent.class);
        } catch (IOException e) {
            return;
        }

        throw new RuntimeException("Expected an exception");
    }

    @Test
    public void serializationOfEmptyIntent() throws Exception {
        Intent expectedIntent = new Intent();
        Intent parsedIntent = myObjectMapper.readValue("{}", Intent.class);
        assertEquals(expectedIntent.getAction(), parsedIntent.getAction());
        assertEquals(expectedIntent.getData(), parsedIntent.getData());
        assertEquals(expectedIntent.getComponent(), parsedIntent.getComponent());
        assertEquals(expectedIntent.getFlags(), parsedIntent.getFlags());
    }

}