package com.demo.gateway;

import com.demo.gateway.common.ThreadInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author lw
 */
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		ThreadInfo threadInfo = new ThreadInfo();
		threadInfo.start();

		SpringApplication.run(DemoApplication.class, args);
	}

}
