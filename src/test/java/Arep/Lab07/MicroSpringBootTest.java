package Arep.Lab07;

import Arep.Lab07.annotations.GetMapping;
import Arep.Lab07.annotations.RequestParam;
import Arep.Lab07.annotations.RestController;
import Arep.Lab07.controller.MathController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

class MicroSpringBootTest {


    @Test
    void testHttpRequestParsesQueryParams() {
        HttpRequest req = new HttpRequest("/App/hello?name=Carlos");
        assertEquals("Carlos", req.getValue("name"));
    }

    @Test
    void testHttpRequestMissingParamReturnsEmpty() {
        HttpRequest req = new HttpRequest("/App/hello");
        assertEquals("", req.getValue("name"));
    }

    @Test
    void testHttpRequestMultipleParams() {
        HttpRequest req = new HttpRequest("/App/test?a=1&b=2");
        assertEquals("1", req.getValue("a"));
        assertEquals("2", req.getValue("b"));
    }

    @Test
    void testHttpRequestPathIsCorrect() {
        HttpRequest req = new HttpRequest("/App/hello?name=Test");
        assertEquals("/App/hello", req.getPath());
    }



    @Test
    void testHelloWithName() {
        assertEquals("Hello Carlos!", MathController.hello("Carlos"));
    }

    @Test
    void testHelloWithDefaultValue() {
        assertEquals("Hello World!", MathController.hello("World"));
    }

    @Test
    void testPiReturnsCorrectValue() {
        assertTrue(MathController.pi().contains(String.valueOf(Math.PI)));
    }

    @Test
    void testEulerReturnsCorrectValue() {
        assertTrue(MathController.euler().contains(String.valueOf(Math.E)));
    }


    @Test
    void testRestControllerAnnotationPresent() {
        assertTrue(MathController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    void testGetMappingAnnotationsRegistered() throws NoSuchMethodException {
        Method helloMethod = MathController.class.getDeclaredMethod("hello", String.class);
        assertTrue(helloMethod.isAnnotationPresent(GetMapping.class));
        assertEquals("/App/hello", helloMethod.getAnnotation(GetMapping.class).value());
    }

    @Test
    void testRequestParamAnnotationOnHello() throws NoSuchMethodException {
        Method helloMethod = MathController.class.getDeclaredMethod("hello", String.class);
        Parameter param = helloMethod.getParameters()[0];
        assertTrue(param.isAnnotationPresent(RequestParam.class));
        RequestParam rp = param.getAnnotation(RequestParam.class);
        assertEquals("name", rp.value());
        assertEquals("World", rp.defaultValue());
    }

    @Test
    void testAllGetMappingRoutesPresent() {
        long count = java.util.Arrays.stream(MathController.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(GetMapping.class))
                .count();
        assertTrue(count >= 3, "Should have at least 3 @GetMapping routes");
    }


    @Test
    void testHttpResponseDefaultStatusCode() {
        HttpResponse res = new HttpResponse();
        assertEquals(200, res.getStatusCode());
    }

    @Test
    void testHttpResponseSetContentType() {
        HttpResponse res = new HttpResponse();
        res.setContentType("application/json");
        assertEquals("application/json", res.getContentType());
    }
}
