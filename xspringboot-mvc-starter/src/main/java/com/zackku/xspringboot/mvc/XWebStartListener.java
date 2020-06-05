package com.zackku.xspringboot.mvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zackku.xspringboot.mvc.annotaion.XPathParam;
import com.zackku.xspringboot.mvc.annotaion.XRequestBody;
import com.zackku.xspringboot.mvc.annotaion.XRequestMapping;
import com.zackku.xspringboot.mvc.annotaion.XRestController;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack
 * @date 2020/6/3
 */
@Slf4j
public class XWebStartListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final int DEFAULT_PORT = 9099;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    Map<String, Method> pathMethodMap = new HashMap<>(30);
    Map<String, HttpMethod[]> pathHttpMethodsMap = new HashMap<>(30);
    Map<Method, Object> methodObjectMap = new HashMap<>(30);

    public void onApplicationEvent(ApplicationReadyEvent event) {
        Map<String, Object> xControllerMap = applicationContext.getBeansWithAnnotation(XRestController.class);
        if (xControllerMap.isEmpty()) {
            log.warn("no XRestController here! xSpringBoot web start fail");
            return;
        }
        // process each controller
        for (Object controller : xControllerMap.values()) {
            Class clz = controller.getClass();
            // find controller path
            XRequestMapping controllerRM = (XRequestMapping) clz.getDeclaredAnnotation(XRequestMapping.class);
            String[] controllerPaths = controllerRM == null ? new String[]{""} : controllerRM.value();

            // find method path
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                methodObjectMap.put(method, controller);
                buildMap(controllerPaths, method);
            }

        }
        Vertx vertx = Vertx.vertx();
        Router routers = Router.router(vertx);
        routers.route().handler(BodyHandler.create());

        for (String path : pathMethodMap.keySet()) {
            Method method = pathMethodMap.get(path);
            Object object = methodObjectMap.get(method);
//            log.info("build rest api. path: {} handler: {}.{}", path, object.getClass().getName(), method.getName());
            log.info("XSPRINGBOOT build rest api. path: {} handler: {}.{}", path, object.getClass().getName(), method.getName());
            HttpMethod[] httpMethods = pathHttpMethodsMap.get(path);
            Route r = routers.route(path);
            if (httpMethods.length != 0) {
                for (HttpMethod httpMethod : httpMethods) {
                    r = r.method(httpMethod);
                }
            }
            r.handler(routingContext -> {
                StopWatch sw = new StopWatch();
                sw.start();
                HttpServerResponse response = routingContext.response();
                try {
                    Object result = invokeMethod(routingContext, method, object);
                    String resultStr = objectMapper.writeValueAsString(result);
                    response.end(resultStr);
                } catch (Exception e) {
                    log.error("method invoke", e);
                    throw new RuntimeException();
                }
                sw.stop();
                long cost = sw.getTotalTimeNanos() / 1000;
                if (cost > 1000) {
                    log.info("XSPRINGBOOT process path: {}  cost:{} ms", path, cost / 1000);
                } else {
                    log.info("XSPRINGBOOT process path: {}  cost:{} μs", path, cost);
                }
            });
        }
        HttpServer server = vertx.createHttpServer();

        String port = applicationContext.getEnvironment().getProperty("xserver.port");
        int portInt = port == null ? DEFAULT_PORT : Integer.parseInt(port);
        server.requestHandler(routers).listen(portInt);
        log.info("XSPRINGBOOT start succeed on port: {} !!", portInt);
    }

    private Object invokeMethod(RoutingContext routingContext, Method method, Object object) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getDeclaredAnnotation(XRequestBody.class) != null) {
                // find XRequestBody
                JsonObject bodyJson = routingContext.getBodyAsJson();
                args[i] = bodyJson != null ? bodyJson.mapTo(parameters[i].getType()) : null;
            } else if (parameters[i].getDeclaredAnnotation(XPathParam.class) != null) {
                // find XPathParam
                MultiMap pathParamMap = routingContext.request().params();
                ObjectNode root = objectMapper.createObjectNode();
                pathParamMap.entries().forEach(l -> root.put(l.getKey(), l.getValue()));
                args[i] = objectMapper.treeToValue(root, parameters[i].getType());
            } else {
                args[i] = null;
            }
        }
        return method.invoke(object, args);
    }

    private void buildMap(String[] controllerPaths, Method method) {
        XRequestMapping methodRM = method.getDeclaredAnnotation(XRequestMapping.class);
        String[] methodPaths = methodRM == null ? new String[]{""} : methodRM.value();
        for (String controllerPath : controllerPaths) {
            for (String methodPath : methodPaths) {
                String path = controllerPath + methodPath;
                if (pathMethodMap.containsKey(path)) {
                    throw new RuntimeException();
                }
                pathMethodMap.putIfAbsent(path, method);
                pathHttpMethodsMap.put(path, methodRM != null ? methodRM.method() : new HttpMethod[0]);
            }
        }
    }
}
