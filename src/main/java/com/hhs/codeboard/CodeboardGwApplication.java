package com.hhs.codeboard;

import com.hhs.codeboard.gw.util.CommonUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import reactor.blockhound.BlockHound;

import java.util.Arrays;
import java.util.Objects;

@SpringBootApplication
public class CodeboardGwApplication {

	public static void main(String[] args) {
		if (!CommonUtil.checkPrdProfiles(args)) {
			// 운영이 아닐때만 실행
			BlockHound.install();
		}

		SpringApplication.run(CodeboardGwApplication.class, args);
	}

}
