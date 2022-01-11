package sh.calaba.instrumentationbackend.json;

import android.content.ComponentName;
import android.content.Intent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;

import sh.calaba.instrumentationbackend.query.InvocationOperation;
import sh.calaba.org.codehaus.jackson.JsonParser;
import sh.calaba.org.codehaus.jackson.JsonProcessingException;
import sh.calaba.org.codehaus.jackson.map.DeserializationContext;
import sh.calaba.org.codehaus.jackson.map.JsonDeserializer;

public class IntentDeserializer extends JsonDeserializer<Intent> {
    @Override
    public Intent deserialize(JsonParser jp, DeserializationContext ctxt)
          throws IOException, JsonProcessingException {
        Map map = jp.readValueAs(Map.class);
        Intent intent = new Intent();

        if (map.containsKey("type") && map.containsKey("data")) {
            throw new IOException("IntentJSON cannot contain both type and data");
        }

        if (map.containsKey("action")) {
            intent.setAction((String) map.get("action"));
        }

        if (map.containsKey("data")) {
            intent.setData(Uri.parse((String) map.get("data")));
        }

        if (map.containsKey("flags")) {
            intent.setFlags((Integer) map.get("flags"));
        }

        if (map.containsKey("type")) {
            intent.setType((String) map.get("type"));
        }

        if (map.containsKey("package")) {
            intent.setPackage((String) map.get("package"));
        }

        if (map.containsKey("extras") && map.get("extras") != null) {
            Map extrasMap = (Map) map.get("extras");

            for (Object keyO : extrasMap.keySet()) {
                String key = (String) keyO;
                Object value = extrasMap.get(key);

                if (value == null) {
                    intent.putExtra(key, (String) null);
                } else {
                    // Prefer integers and doubles
                    if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Double) {
                        intent.putExtra(key, (Double) value);
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    } else {
                        // Prefer arrays over lists
                        if (value instanceof List) {
                            value = ((List) value).toArray();
                        }

                        List<Object> listOfArguments = new ArrayList<Object>();
                        listOfArguments.add(key);
                        listOfArguments.add(value);
                        InvocationOperation invocationOperation = new InvocationOperation("putExtra", listOfArguments);

                        InvocationOperation.MethodWithArguments method = null;
                        try {
                            method = invocationOperation.findCompatibleMethod(intent);
                        }catch (Exception exception){
                            System.out.println("MRT: listOfArguments" + listOfArguments);
                            System.out.println("MRT: Exception occurred:" + exception.toString());
                        }

                        if (method == null) {
                            throw new IOException("Cannot add '" + value + "' of type '" + value.getClass() + "'");
                        } else {
                            try {
                                method.invoke(intent);
                            } catch (InvocationTargetException e) {
                                throw new IOException(e.getMessage());
                            } catch (IllegalAccessException e) {
                                throw new IOException(e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        if (map.containsKey("component")) {
            Map componentMap = (Map) map.get("component");
            intent.setComponent(
                  new ComponentName((String) componentMap.get("packageName"), (String) componentMap.get("className")));
        }

        return intent;
    }
}
