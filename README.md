# xSpringBoot
A simple and efficient async web framework. Just use Spring code style to write Vertx project which base on netty and has a set of facilities!


# Quick Start
1. Import jar in maven. 
````
    <dependency>
        <groupId>com.zackku</groupId>
        <artifactId>xspringboot-mvc-starter</artifactId>
        <version>${xspringboot.version}</version>
    </dependency>
````

2. Create a SpringApplication.
````
@SpringBootApplication(scanBasePackages = "org.test")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
} 
````

3. Create a XRestController as similar with Spring RestController but with the `X annotations`.
````
@XRestController
@XRequestMapping("/xspringboot")
public class TestController {

    @XRequestMapping("/test", method = HttpMethod.POST)
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
````

4. Start the SpringApplication and access `http://localhost:9099/xspringboot/test` .
````
2020-06-04 14:27:33.209  INFO 23853 --- [           main] o.z.xspringboot.mvc.XWebStartListener    : XSPRINGBOOT build rest api. path: /xspringboot/test handler: org.test.controller.TestController.c
2020-06-04 14:27:33.374  INFO 23853 --- [           main] o.z.xspringboot.mvc.XWebStartListener    : XSPRINGBOOT start succeed on port:9099 !!
2020-06-04 14:42:44.854  INFO 23949 --- [ntloop-thread-1] o.z.xspringboot.mvc.XWebStartListener    : XSPRINGBOOT process path: /xspringboot/test  cost:376 μs
````

More detail in [DemoProject](https://github.com/Zack-Ku/xSpringBoot-Demo).

