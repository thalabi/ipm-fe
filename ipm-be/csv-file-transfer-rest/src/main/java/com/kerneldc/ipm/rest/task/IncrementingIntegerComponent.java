package com.kerneldc.ipm.rest.task;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IncrementingIntegerComponent {
	//private static final ThreadLocal<Integer> integerContext = new ThreadLocal<>();
	private int beanInteger = 0;

	public void taskIncrementingSomeInteger() throws InterruptedException {
		int methodInteger = 0;
		LOGGER.info("Begin ...");
		LOGGER.info("Current thread name: {}, thread ID: {}", Thread.currentThread().getName(), Thread.currentThread().getId());
		Thread.sleep(1000);
//		if (integerContext.get() == null) {
//			integerContext.set(1);
//		} else {
//			integerContext.set(integerContext.get()+1);
//		}
		beanInteger++;
		methodInteger++;
		//LOGGER.info("integerContext: {}, beanInteger: {}, methodInteger: {}", integerContext.get(), beanInteger, methodInteger);
		LOGGER.info("beanInteger: {}, methodInteger: {}", beanInteger, methodInteger);
		LOGGER.info("Sleeping 5 seconds");
		
		Thread.sleep(5000);

		beanInteger++;
		methodInteger++;
		LOGGER.info("beanInteger: {}, methodInteger: {}", beanInteger, methodInteger);

		
//		integerContext.remove();
		LOGGER.info("End ...");
	}

}
