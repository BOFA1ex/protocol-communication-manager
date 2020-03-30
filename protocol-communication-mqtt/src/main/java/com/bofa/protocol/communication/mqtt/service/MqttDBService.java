package com.bofa.protocol.communication.mqtt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Service
public class MqttDBService {

    static final Logger logger = LoggerFactory.getLogger(MqttDBService.class);

    public void saveData(){
        //do nothing.
    }

    public void authc(){
        logger.info("do authc");
        //do nothing..
    }
}
