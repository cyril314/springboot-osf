package com.fit;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
public class OsfApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(OsfApplication.class, args);
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = System.getenv("POST");
        ConfigurableEnvironment environment = run.getEnvironment();
        if (Strings.isEmpty(port)) {
            port = environment.getProperty("server.port");
        }
        String path = environment.getProperty("server.servlet.context-path", "").replace("/", "");
        log.info("---------------------------------------------------------");
        log.info("Access URLs:\n\tLocal: \t\thttp://localhost:{}/{}\n\tExternal:\t{}://{}:{}/{}", port, path, "http", ip, port, path);
        log.info("---------------------- admin-web ------------------------");
    }
}
