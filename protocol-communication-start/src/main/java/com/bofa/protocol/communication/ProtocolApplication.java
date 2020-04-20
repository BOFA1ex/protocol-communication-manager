package com.bofa.protocol.communication;

import com.bofa.protocol.communication.commons.netty.ProtocolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
@SpringBootApplication
@ImportResource({
        "classpath:spring/spring-core-conf.xml",
        "classpath:spring/spring-executor-conf.xml",
        "classpath:spring/spring-profile-mqtt.xml"
})
public class ProtocolApplication {

    static final Logger logger = LoggerFactory.getLogger(ProtocolApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProtocolApplication.class, args)
                .getBean(ProtocolServer.class).startServer();
        logger.info("start protocol application");
    }
}
