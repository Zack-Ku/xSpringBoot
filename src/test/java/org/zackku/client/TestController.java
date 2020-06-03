package org.zackku.client;

import org.zackku.annotaion.XPathParam;
import org.zackku.annotaion.XRequestBody;
import org.zackku.annotaion.XRequestMapping;
import org.zackku.annotaion.XRestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack
 * @date 2020/6/3
 */
@XRestController
@XRequestMapping("/test")
public class TestController {

    @XRequestMapping("/a")
    public String a() {
        return "hello a";
    }

    @XRequestMapping("/b")
    public String b() {
        return "hello b";
    }

    @XRequestMapping("/c")
    public Map<String, Object> c(@XRequestBody TestReq req, @XPathParam TestReq req2) {
        Map<String, Object> t = new HashMap<>();
        t.put("kkk", 1);
        t.put("sssa", 42);
        if (req != null) {
            t.put("req.a", req.getA());
        }
        if (req2 != null) {
            t.put("req.b", req2.getB());
        }
        return t;
    }
}
