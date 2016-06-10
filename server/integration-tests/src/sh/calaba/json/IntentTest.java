package sh.calaba.json;

import android.content.ComponentName;
import android.content.Intent;

import android.net.Uri;
import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;

import sh.calaba.instrumentationbackend.json.JSONUtils;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class IntentTest {
    private static String json = "{\n" +
            "  \"action\": \"My Action\",\n" +
            "  \"data\": \"http://example.com/test\",\n" +
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
    public void serializationOfEmptyIntent() throws Exception {
        Intent expectedIntent = new Intent();
        Intent parsedIntent = myObjectMapper.readValue("{}", Intent.class);
        assertEquals(expectedIntent.getAction(), parsedIntent.getAction());
        assertEquals(expectedIntent.getData(), parsedIntent.getData());
        assertEquals(expectedIntent.getComponent(), parsedIntent.getComponent());
    }

}