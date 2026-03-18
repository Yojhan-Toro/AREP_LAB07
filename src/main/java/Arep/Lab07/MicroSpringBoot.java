package Arep.Lab07;

import Arep.Lab07.annotations.GetMapping;
import Arep.Lab07.annotations.RequestParam;
import Arep.Lab07.annotations.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;

public class MicroSpringBoot {

    public static void run(Class<?> controllerClass, String[] args)
            throws IOException, URISyntaxException {

        System.out.println("Loading components from: " + controllerClass.getName());

        if (!controllerClass.isAnnotationPresent(RestController.class)) {
            System.err.println("Class " + controllerClass.getName()
                    + " is not annotated with @RestController. Aborting.");
            return;
        }

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping mapping = method.getAnnotation(GetMapping.class);
                String path = mapping.value();

                HttpServer.get(path, (req, res) -> {
                    try {
                        Parameter[] parameters = method.getParameters();
                        Object[] args2 = new Object[parameters.length];

                        for (int i = 0; i < parameters.length; i++) {
                            Parameter param = parameters[i];

                            if (param.getType() == HttpRequest.class) {
                                args2[i] = req;
                            } else if (param.getType() == HttpResponse.class) {
                                args2[i] = res;
                            } else if (param.isAnnotationPresent(RequestParam.class)) {
                                RequestParam rp = param.getAnnotation(RequestParam.class);
                                String val = req.getValue(rp.value());
                                args2[i] = val.isEmpty() ? rp.defaultValue() : val;
                            } else {
                                args2[i] = null;
                            }
                        }

                        Object result = method.invoke(null, args2);
                        return result != null ? result.toString() : "";

                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Error invoking method: " + e.getMessage();
                    }
                });

                System.out.println("Registered route: GET " + path
                        + " -> " + controllerClass.getSimpleName() + "." + method.getName() + "()");
            }
        }

        System.out.println("All routes registered. Starting HttpServer on port 35000...");
        HttpServer.main(args);
    }
}
