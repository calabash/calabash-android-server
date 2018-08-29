package sh.calaba.instrumentationbackend.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.InterruptedException;
import java.lang.Override;
import java.lang.Runnable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Build;
import dalvik.system.DexClassLoader;
import sh.calaba.instrumentationbackend.*;
import sh.calaba.instrumentationbackend.actions.version.Version;
import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient;
import sh.calaba.instrumentationbackend.json.JSONUtils;
import sh.calaba.instrumentationbackend.query.InvocationOperation;
import sh.calaba.instrumentationbackend.query.Operation;
import sh.calaba.instrumentationbackend.query.Query;
import sh.calaba.instrumentationbackend.query.QueryResult;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.org.codehaus.jackson.JsonNode;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import sh.calaba.org.codehaus.jackson.node.BooleanNode;
import sh.calaba.org.codehaus.jackson.node.ObjectNode;

public class HttpServer extends NanoHTTPD {
	private static final String TAG = "InstrumentationBackend";
	private boolean running = true;
	private boolean ready = false;

	private final Lock lock = new ReentrantLock();
	private final Condition shutdownCondition = lock.newCondition();

	private static HttpServer instance;
    private ApplicationStarter applicationStarter;
    private Collection<Class<?>> dynamicallyLoadedClasses = new ArrayList<Class<?>>();

    /**
	 * Creates and returns the singleton instance for HttpServer.
	 *
	 * Can only be called once. Otherwise, you'll get an IllegalStateException.
	 */
	public synchronized static HttpServer instantiateAndListen(int testServerPort) {
        return instantiate((Integer)testServerPort);
    }

    public synchronized static HttpServer instantiateWithoutListening() {
        return instantiate((Integer)null);
    }

    private synchronized static HttpServer instantiate(Integer testServerPort) {
		if (instance != null) {
			throw new IllegalStateException("Can only instantiate once!");
		}
		try {
            if (testServerPort == null) {
                Logger.info("Instantiating http server NOT listening to any port");
                instance = new HttpServer();
            } else {
                Logger.info("Instantiating http server at " + testServerPort);
                instance = new HttpServer(testServerPort);
            }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return instance;
	}

	public synchronized static HttpServer getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Must be initialized!");
		}
		return instance;
	}

	private HttpServer(int testServerPort) throws IOException {
		super(testServerPort, new File("/"));
	}

    /* Internal: Don't listen to any port */
    private HttpServer() throws IOException {
        super(new File("/"));
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response serve(final String uri, String method, Properties header,
                          Properties params, Properties files) {
		System.out.println("URI: " + uri);
		System.out.println("params: " + params);

		if (uri.endsWith("/ping")) {
			return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "pong");

		}
        else if (uri.endsWith("/version")) {
            String result = toJson(new Version().execute());
            System.out.println("result:" + result);

            return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, result);
        }
        else if (uri.endsWith("/start-application")) {
            try {
                String json = params.getProperty("json");

                ObjectMapper mapper = JSONUtils.calabashObjectMapper();
                JsonNode jsonNode = mapper.readTree(mapper.getJsonFactory().createJsonParser(json));

                if (!(jsonNode instanceof ObjectNode)) {
                    throw new RuntimeException("Invalid json, not an object node");
                }

                ObjectNode objectNode = (ObjectNode) jsonNode;

                Intent startIntent = null;

                if (objectNode.has("intent")) {
                    startIntent = mapper.readValue(objectNode.get("intent"), Intent.class);
                }

                this.applicationStarter.startApplication(startIntent);

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                        FranklyResult.emptyResult().asJson());
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(HTTP_INTERNALERROR, "application/json;charset=utf-8",
                        FranklyResult.fromThrowable(e).asJson());
            }
        }
        else if (uri.endsWith("/instrument") || uri.endsWith("/start-activity")) {
            try {
                String json = params.getProperty("json");

                ObjectMapper mapper = JSONUtils.calabashObjectMapper();
                JsonNode jsonNode = mapper.readTree(mapper.getJsonFactory().createJsonParser(json));

                if (!(jsonNode instanceof ObjectNode)) {
                    throw new RuntimeException("Invalid json, not an object node");
                }

                ObjectNode objectNode = (ObjectNode) jsonNode;

                if (!objectNode.has("intent")) {
                    throw new RuntimeException("No intent given");
                }

                final Intent intent = mapper.readValue(objectNode.get("intent"), Intent.class);

                final Context context = InstrumentationBackend.instrumentation.getContext();

                boolean runInNewThread = false;

                if (objectNode.has("async") && objectNode.get("async") instanceof BooleanNode) {
                    runInNewThread = (objectNode.get("async")).asBoolean();
                }

                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (uri.endsWith("/instrument")) {
                            context.startInstrumentation(intent.getComponent(), null, intent.getExtras());
                        } else if (uri.endsWith("/start-activity")) {
                            context.startActivity(intent);
                        }
                    }
                };

                if (runInNewThread) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            runnable.run();
                        }
                    }).start();
                } else {
                    runnable.run();
                }

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                        FranklyResult.emptyResult().asJson());
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(HTTP_INTERNALERROR, "application/json;charset=utf-8",
                        FranklyResult.fromThrowable(e).asJson());
            }
        }
		else if (uri.endsWith("/dump")) {
			FranklyResult errorResult = null;
			try {


				String json = params.getProperty("json");


				if (json == null)
				{
					Map<?,?> dumpTree = new ViewDump().dumpWithoutElements();
					return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", JSONUtils.asJson(dumpTree));
				}
				else
				{
					ObjectMapper mapper = new ObjectMapper();
					Map dumpSpec = mapper.readValue(json, Map.class);

					List<Integer> path = (List<Integer>) dumpSpec.get("path");
					if (path == null)
					{
						Map<?,?> dumpTree = new ViewDump().dumpWithoutElements();
						return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", JSONUtils.asJson(dumpTree));
					}
					Map<?,?> dumpTree = new ViewDump().dumpPathWithoutElements(path);
					if (dumpTree == null) {
						return new NanoHTTPD.Response(HTTP_NOTFOUND, "application/json;charset=utf-8", "{}");
					}
					else {
						return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", JSONUtils.asJson(dumpTree));
					}


				}


			} catch (Exception e ) {
				e.printStackTrace();
                errorResult = FranklyResult.fromThrowable(e);
            }
            return new NanoHTTPD.Response(HTTP_INTERNALERROR, "application/json;charset=utf-8", errorResult.asJson());
		}
        else if (uri.endsWith("/broadcast-intent")) {
            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Intent intent = mapper.readValue(json, Intent.class);

                Activity activity = InstrumentationBackend.getCurrentActivity();
                activity.sendBroadcast(intent);

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", "");
            } catch (Exception e) {
                e.printStackTrace();
                Exception ex = new Exception("Could not invoke method", e);

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", FranklyResult.fromThrowable(ex).asJson());
            }
        }
        else if (uri.endsWith("/file-exists")) {
            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map data = mapper.readValue(json, Map.class);

                String fileName = (String) data.get("fileName");
                File file = new File(fileName);

                String result;

                if (file.exists()) {
                    result = "true";
                } else {
                    result = "false";
                }

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", result);
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(HTTP_BADREQUEST, "application/json;charset=utf-8", FranklyResult.fromThrowable(e).asJson());
            }
        }
        else if (uri.endsWith("/read-file")) {
            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map data = mapper.readValue(json, Map.class);

                String fileName = (String) data.get("fileName");

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", new FileInputStream(fileName));
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(HTTP_BADREQUEST, "application/json;charset=utf-8", FranklyResult.fromThrowable(e).asJson());
            }
        }
        else if (uri.endsWith("/add-file")) {
            // NOTE: There is a PUT hack in NanoHTTPD that stores PUTs in a tmp file,
            //       we need that!
            if (!"PUT".equals(method)) {
                return new Response(HTTP_BADREQUEST, "test/plain;charset=utf-8", "Only PUT supported for this endpoint, not '" + method + "'");
            }

            try {
                String tmpFilePath = files.getProperty("content");
                Context targetContext = InstrumentationBackend.instrumentation.getTargetContext();

                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String fileName = timeStamp + ".out";

                OutputStream fileOutputStream = targetContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                InputStream fileInputStream = new FileInputStream(tmpFilePath);

                Utils.copyContents(fileInputStream, fileOutputStream);

                fileInputStream.close();
                fileOutputStream.close();

                // context.openFileOutput will place the file in /data/data/<pkg>/files/<file>
                File outFile = new File(targetContext.getFilesDir(), fileName);

                return new Response(HTTP_OK, "application/octet-stream", outFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (uri.endsWith("/last-broadcast-intent")) {
            List<Intent> intents = InstrumentationBackend.intents;
            Map franklyResult = new HashMap<String, String>();
            ObjectMapper mapper = JSONUtils.calabashObjectMapper();

            if (intents.isEmpty()) {
                Map<String, Object> result = new HashMap<String, Object>();

                result.put("index", -1);
                result.put("intent", null);

                try {
                    franklyResult.put("outcome", "SUCCESS");
                    franklyResult.put("result", mapper.writeValueAsString(result));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    int index = intents.size() - 1;
                    Map<String, Object> result = new HashMap<String, Object>();
                    Intent intent = intents.get(index);

                    result.put("index", index);
                    result.put("intent", intent);

                    franklyResult.put("outcome", "SUCCESS");
                    franklyResult.put("result", mapper.writeValueAsString(result));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                return new Response(HTTP_OK, "application/json;charset=utf-8",
                        mapper.writeValueAsString(franklyResult));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (uri.endsWith("/backdoor")) {
            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map backdoorMethod = mapper.readValue(json, Map.class);

                String methodName = (String) backdoorMethod.get("method_name");
                List arguments = (List) backdoorMethod.get("arguments");
                Operation operation = new InvocationOperation(methodName, arguments);

                Application application = InstrumentationBackend.getDefaultCalabashAutomation().getCurrentApplication();
                Object invocationResult;

                invocationResult = operation.apply(application);

                if (invocationResult instanceof Map && ((Map) invocationResult).containsKey("error")) {
                    Context context = InstrumentationBackend.getDefaultCalabashAutomation().getCurrentActivity();
                    invocationResult = operation.apply(context);
                }
                Map<String, String> result = new HashMap<String, String>();

                if (invocationResult instanceof Map && ((Map) invocationResult).containsKey("error")) {
                    result.put("outcome", "ERROR");
                    result.put("result", (String) ((Map) invocationResult).get("error"));
                    result.put("details", invocationResult.toString());
                } else {
                    result.put("outcome", "SUCCESS");
                    result.put("result", String.valueOf(invocationResult));
                }

                ObjectMapper resultMapper = new ObjectMapper();

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", resultMapper.writeValueAsString(result));
            } catch (Exception e) {
                e.printStackTrace();
                Exception ex = new Exception("Could not invoke method", e);

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", FranklyResult.fromThrowable(ex).asJson());
            }
        }
		else if (uri.endsWith("/map")) {
			FranklyResult errorResult = null;
			try {
				String commandString = params.getProperty("json");
				ObjectMapper mapper = new ObjectMapper();
				Map command = mapper.readValue(commandString, Map.class);

				String uiQuery = (String) command.get("query");
				uiQuery = uiQuery.trim();
				Map op = (Map) command.get("operation");
                String methodName = (String) op.get("method_name");
                List arguments = (List) op.get("arguments");

                if (methodName.equals("flash")) {
                    QueryResult queryResult = new Query(uiQuery, java.util.Collections.emptyList()).executeQuery();
                    List<?> queryResultList = queryResult.asList();

                    if (queryResultList.isEmpty()) {
                        return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                                FranklyResult.failedResult("Could not find view to flash", "").asJson());
                    }

                    for (Object o : queryResultList) {
                        if (!(o instanceof View)) {
                            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                                    FranklyResult.failedResult("Only views can be flashed", "").asJson());
                        }
                    }

                    for (final Object o : queryResultList) {
                        final View view = (View) o;

                        UIQueryUtils.runOnViewThread(view, new Runnable() {
                            @Override
                            public void run() {
                                Animation animation = new AlphaAnimation(1, 0);
                                animation.setRepeatMode(Animation.REVERSE);
                                animation.setDuration(200);
                                animation.setRepeatCount(5);
                                view.startAnimation(animation);
                            }
                        });

                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                                    FranklyResult.failedResult("Interrupted while flashing", "").asJson());
                        }
                    }

                    return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                            FranklyResult.successResult(queryResult).asJson());
                }
                else if (methodName.equals("execute-javascript")) {
                    String javascript = (String) command.get("javascript");
                    QueryResult queryResult = new Query(uiQuery).executeQuery();
                    List<CalabashChromeClient.WebFuture> webFutures = new ArrayList<CalabashChromeClient.WebFuture>();

                    List<String> webFutureResults = new ArrayList<String>(webFutures.size());
                    boolean catchAllJavaScriptExceptions = true;
                    boolean success = true;

                    for (Object object : queryResult.asList()) {
                        String result;

                        if (object instanceof View) {
                            if (WebContainer.isValidWebContainer((View) object)) {
                                WebContainer webContainer = new WebContainer((View) object);

                                try {
                                    result = webContainer.evaluateSyncJavaScript(javascript, catchAllJavaScriptExceptions);
                                    success = true;
                                } catch (ExecutionException e) {
                                    result = e.getMessage();
                                    success = false;
                                }
                            } else {
                                result = "Error: " + object.getClass().getCanonicalName() + " is not recognized a valid web view.";
                                success = false;
                            }
                        } else {
                            result = "Error: will only call javascript on views, not " + object.getClass().getSimpleName();
                            success = false;
                        }

                        webFutureResults.add(result);
                    }

                    QueryResult jsQueryResultsList = new QueryResult(webFutureResults);
                    FranklyResult result = new FranklyResult(success, jsQueryResultsList, "", "");

                    return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                            result.asJson());
                }
               else {
                    QueryResult queryResult = new Query(uiQuery,arguments).executeQuery();

                    return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                            FranklyResult.successResult(queryResult).asJson());
                }
			} catch (Exception e ) {
				e.printStackTrace();
                errorResult = FranklyResult.fromThrowable(e);
            }
            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", errorResult.asJson());
		} else if (uri.endsWith("/query")) {
			return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT,
					"/query endpoint is discontinued - use /map with operation query");
        } else if (uri.endsWith("/gesture")) {
            FranklyResult errorResult;

            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map gesture = mapper.readValue(json, Map.class);

                MultiTouchGesture multiTouchGesture = new MultiTouchGesture(gesture);
                multiTouchGesture.perform();


                List<Map<String, Map<Object, Object>>> list = new ArrayList<Map<String, Map<Object, Object>>>(1);
                list.add(multiTouchGesture.getEvaluatedQueries());

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8",
                        FranklyResult.successResult(new QueryResult(list)).asJson());

            } catch (Exception e ) {
                e.printStackTrace();
                errorResult = FranklyResult.fromThrowable(e);
            }

            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", errorResult.asJson());
        } else if (uri.endsWith("/kill")) {
			lock.lock();
			try {
				System.out.println("Stopping test server");
				stop();
				running = false;

				shutdownCondition.signal();
				return new NanoHTTPD.Response(HTTP_OK, MIME_HTML,
						"Affirmative!");
			} finally {
				lock.unlock();
			}

		} else if (uri.endsWith("/ready")) {
			return new Response(HTTP_OK, MIME_HTML, Boolean.toString(ready));

		} else if (uri.endsWith("/screenshot")) {
			try {
				Bitmap bitmap;

                UIObject uiObject = InstrumentationBackend.getRootViews().iterator().next();

                if (uiObject.getObject() instanceof View) {
                    View rootView = (View) uiObject.getObject();
                    rootView.setDrawingCacheEnabled(true);
                    rootView.buildDrawingCache(true);
                    bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
                    rootView.setDrawingCacheEnabled(false);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    return new NanoHTTPD.Response(HTTP_OK, "image/png",
                            new ByteArrayInputStream(out.toByteArray()));
                } else {
                    throw new RuntimeException("Invalid rootView '" + uiObject.getObject() + "'");
                }
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				return new NanoHTTPD.Response(HTTP_INTERNALERROR, null,
						sw.toString());
			}

		}
        else if (uri.endsWith("/add-file")) {
            // NOTE: There is a PUT hack in NanoHTTPD that stores PUTs in a tmp file,
            //       we need that!
            if (!"PUT".equals(method)) {
                return new Response(HTTP_BADREQUEST, "test/plain;charset=utf-8", "Only PUT supported for this endpoint, not '" + method + "'");
            }

            String tmpFilePath = files.getProperty("content");

            try {
                File out = File.createTempFile("calabash", ".upload");

                FileInputStream fileInputStream = new FileInputStream(new File(tmpFilePath));
                FileOutputStream fileOutputStream = new FileOutputStream(out);

                Utils.copyContents(fileInputStream, fileOutputStream);

                fileInputStream.close();
                fileOutputStream.close();

                return new Response(HTTP_OK, "application/octet-stream", out.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", FranklyResult.fromThrowable(e).asJson());
            }
        }
        else if (uri.endsWith("/move-cache-file-to-public")) {
            FranklyResult errorResult;

            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map data = mapper.readValue(json, Map.class);
                final String from = (String) data.get("from");
                final String name = (String) data.get("name");

                ContextWrapper contextWrapper = new ContextWrapper(InstrumentationBackend.instrumentation.getTargetContext());

                FileInputStream fileInputStream = new FileInputStream(new File(from));

                FileOutputStream fileOutputStream;

                File resultingFile = new File(contextWrapper.getFilesDir(), name);

                if (Build.VERSION.SDK_INT < 23) {
                    fileOutputStream = contextWrapper.openFileOutput(name, Context.MODE_WORLD_READABLE);
                    Utils.copyContents(fileInputStream, fileOutputStream);
                } else {
                    // Marshmallow removed MODE_WORLD_READABLE
                    fileOutputStream = contextWrapper.openFileOutput(name, Context.MODE_PRIVATE);
                    Utils.copyContents(fileInputStream, fileOutputStream);

                    // Instead, we modify the file permissions using the Java file API
                    if (!resultingFile.setReadable(true, false)) {
                        throw new RuntimeException("Failed to set file to world readable");
                    }
                }

                fileInputStream.close();
                fileOutputStream.close();

                new File(from).delete();

                return new Response(HTTP_OK, "application/octet-stream", resultingFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                errorResult = FranklyResult.fromThrowable(e);
            }

            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", errorResult.asJson());
        }
        else if (uri.endsWith("/load-dylib")) {
            FranklyResult errorResult;

            try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map data = mapper.readValue(json, Map.class);
                final String path = (String) data.get("path");
                final List<String>classes = (ArrayList<String>) data.get("classes");

                System.out.println("PATH: " + path);
                System.out.println("CLASSES: " + classes);

                final File optimizedDirectory = InstrumentationBackend.instrumentation.getTargetContext().getDir("calabash_optimized_dex", 0);

                if (!new File(path).exists()) {
                    throw new RuntimeException("Path '" + path + "' does not exist");
                }

                DexClassLoader dexClassLoader = new DexClassLoader(path, optimizedDirectory.getAbsolutePath(), null, getClass().getClassLoader());

                for (String className : classes) {
                    dexClassLoader.loadClass(className);

                    // Load the class, which also fires static initializers
                    Class loadedClass = Class.forName(className, true, dexClassLoader);

                    dynamicallyLoadedClasses.add(loadedClass);
                }

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", FranklyResult.emptyResult().asJson());
            } catch (Exception e) {
                e.printStackTrace();
                errorResult = FranklyResult.fromThrowable(e);
            }

            return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", errorResult.asJson());
        } else if (uri.endsWith("/invoke-method-on-dyloaded-classes")) {
		    try {
                String json = params.getProperty("json");
                ObjectMapper mapper = new ObjectMapper();
                Map data = mapper.readValue(json, Map.class);

                final String className = (String) data.get("className");
                final String methodName = (String) data.get("methodName");
                final List<?> arguments = (List<?>) data.get("arguments");

                InvocationOperation invocationOperation =
                        new InvocationOperation(methodName, arguments);

                for (Class<?> dynamicallyLoadedClass : dynamicallyLoadedClasses) {
                    if (dynamicallyLoadedClass.getName().equals(className)) {
                        System.out.println("Invoking on " + dynamicallyLoadedClass);

                        InvocationOperation.MethodWithArguments foundMethod =
                                invocationOperation.findCompatibleMethod(dynamicallyLoadedClass);

                        Map<String, String> result = new HashMap<String, String>();

                        if (foundMethod == null) {
                            result.put("outcome", "ERROR");
                            result.put("result", "No such method '" + methodName + "' for '" + dynamicallyLoadedClass + "'");
                            result.put("details", "");
                        } else {
                            result.put("outcome", "SUCCESS");
                            result.put("result", String.valueOf(foundMethod.invoke(null)));
                        }

                        ObjectMapper resultMapper = new ObjectMapper();

                        return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", resultMapper.writeValueAsString(result));
                    }
                }

                throw new RuntimeException("No such class '" + className + "'");
            } catch (Exception e) {
                e.printStackTrace();

                return new NanoHTTPD.Response(HTTP_OK, "application/json;charset=utf-8", FranklyResult.fromThrowable(e).asJson());
            }

        }

		System.out.println("header: " + header);
		System.out.println("params: " + params);
		Enumeration<String> propertyNames = (Enumeration<String>) params.propertyNames();
		while (propertyNames.hasMoreElements())
		{
			String s = propertyNames.nextElement();
			System.out.println("ProP "+s+" = "+params.getProperty(s));
		}
		System.out.println("files: " + files);

		String commandString = params.getProperty("json");
		System.out.println("command: " + commandString);
		String result = toJson(runCommand(commandString));
		System.out.println("result:" + result);

		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, result);
	}

	private String toJson(Result result) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Result runCommand(String commandString) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Command command = mapper.readValue(commandString, Command.class);
			log("Got command:'" + command);
			return command.execute();
		} catch (Throwable t) {
			t.printStackTrace();
			return Result.fromThrowable(t);
		}
	}

	public void waitUntilShutdown() throws InterruptedException {
		lock.lock();
		try {
			while (running) {
				shutdownCondition.await();
            }
		} finally {
			lock.unlock();
		}
	}

    public boolean isRunning() {
        return running;
    }

	public static void log(String message) {
		Log.i(TAG, message);
	}

	public void setReady() {
		ready = true;
	}

    public void setApplicationStarter(ApplicationStarter applicationStarter) {
        this.applicationStarter = applicationStarter;
    }

    public static abstract class ApplicationStarter {
        public abstract void startApplication(Intent startIntent);
    }
}
