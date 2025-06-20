package com.hhs.codeboard.blog.gw;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CodeboardGwApplicationTests {

	@Test
	void contextLoads() {
		TestClass tc = new TestClass();
		TestInterface tw = tc;
		System.out.println(tw.getClass());
	}

	@Getter
	static class TestClass implements TestInterface {
		private String code;
	}

	@Getter
	@RequiredArgsConstructor
	enum TestEnum implements TestInterface {

		TEST1("1"), TEST2("2");

		private final String code;

	}

	interface TestInterface {

		String getCode();

		default String getClassInfo() {
			return "";
		}

	}

	@Getter
	@RequiredArgsConstructor
	enum CreateTestEnum {
		TEST1(new ArrayList<>()), TEST2(new ArrayList<>());

		private final List<String> test;
	}

	@Test
	public void enumTest() {
		List<String> value = CreateTestEnum.TEST1.getTest();
		List<String> value2 = CreateTestEnum.TEST1.getTest();
		value.add("1");
		if (value2 == value) {
			System.out.println("this is same object");
		} else {
			System.out.println("this not same object");
		}
	}


}
