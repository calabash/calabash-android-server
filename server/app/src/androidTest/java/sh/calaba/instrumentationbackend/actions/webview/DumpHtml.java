package sh.calaba.instrumentationbackend.actions.webview;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient.WebFuture;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import android.webkit.WebView;

public class DumpHtml implements Action {

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Result execute(String... args) {
    	
    	List<WebFuture> webResults = (List<WebFuture>) UIQueryUtils.evaluateSyncInMainThread(new Callable() {			
			
			public Object call() throws Exception {
				
				List<WebFuture> webResults = new ArrayList();
				for (CalabashChromeClient ccc : CalabashChromeClient.findAndPrepareWebViews()) {
		    		final WebView webView = ccc.getWebView();
		            webView.loadUrl("javascript:(function() {" +
		                    "prompt('calabash:' + document.body.parentNode.innerHTML);" +
		                    "})()");		            
		            webResults.add(ccc.getResult());
		        }
				return webResults;
				
			}
		});
    	
    	List<String> allResults = new ArrayList<String>(webResults.size());
    	for (WebFuture f : webResults) {
    		allResults.add(f.getAsString());			    		
    	}
    		
    	if (allResults.size() == 0) {
    		return new Result(false, "No WebView found");	
    	}
    	else {
    		return new Result(true, allResults);
    	}

    }

    @Override
    public String key() {
        return "dump_html";
    }
}
