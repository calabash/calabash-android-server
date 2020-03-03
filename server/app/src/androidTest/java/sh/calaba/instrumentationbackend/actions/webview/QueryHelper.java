package sh.calaba.instrumentationbackend.actions.webview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient.WebFuture;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class QueryHelper {


	@SuppressWarnings("unchecked")
	public static Map<String, Object> findFirstVisibleRectangle(List<HashMap<String,Object>> elements) {
		return (Map<String, Object>)findFirstVisibleElement(elements).get("rect");
	}

	public static Map<String, Object> findFirstVisibleElement(List<HashMap<String,Object>> elements) {
		//TODO: Should do something more intelligent
		return (Map<String, Object>)elements.get(0);
	}

	public static String toJsonString(Object o) {
		//http://www.mkyong.com/java/how-to-convert-java-map-to-from-json-jackson/
		try {
			return new ObjectMapper().writeValueAsString(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


    private static String readJavascriptFromAsset(String scriptPath) {
    	StringBuffer script = new StringBuffer();
		try {
			InputStream is = InstrumentationBackend.instrumentation.getContext().getResources().getAssets().open(scriptPath);
			BufferedReader input =  new BufferedReader(new InputStreamReader(is));
			String line = null;
			while (( line = input.readLine()) != null){
				script.append(line);
				script.append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return script.toString();
    }

	public static WebFuture executeAsyncJavascriptInWebContainer(WebContainer webContainer,
		String scriptPath, String selector, String type, int[] javaScriptElementIds) {

		String script = readJavascriptFromAsset(scriptPath);

		script = script.replaceFirst("exp = '%@'", "exp = '" + selector + "'");
		script = script.replaceFirst("queryType = '%@'", "queryType = '" + type + "'");

		if (javaScriptElementIds != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");

			for (int i = 0; i < javaScriptElementIds.length; i++) {
				sb.append(javaScriptElementIds[i]);

				if (i != javaScriptElementIds.length - 1) {
					sb.append(", ");
				}
			}

			sb.append("]");

			script = script.replaceFirst("inElement = '%@'", "inElement = " + sb.toString());
		}

        return webContainer.evaluateAsyncJavaScript(script);
	}
}
