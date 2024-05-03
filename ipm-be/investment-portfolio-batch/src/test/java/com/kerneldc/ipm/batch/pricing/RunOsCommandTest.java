package com.kerneldc.ipm.batch.pricing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@TestInstance(Lifecycle.PER_CLASS) // to be able to use non-static @BeforeAll and @AfterAll methods
@Slf4j
class RunOsCommandTest extends AbstractBaseTest { // TODO fix to use com.kerneldc.common.AbstractBaseTest 

	private static final String TEST_RESOURCES = "src/test/resources";
	private static final String WEB_SERVER_DIRECTORY = TEST_RESOURCES + "/web-server";
	private static final String WEB_SERVER_WINDOWS_EXE = WEB_SERVER_DIRECTORY + "/mongoose_windows.exe";
	private static final String WEB_SERVER_LINUX_EXE = WEB_SERVER_DIRECTORY + "/mongoose_linux";
	private Process process;
	
	@AfterAll
	public void afterAll() {
		LOGGER.info("afterAll()");
		var pid = process.pid();
		LOGGER.info("killing web server pid: {}", pid);
		process.destroy();
	}
	
	@BeforeAll
	public void beforeAll() throws IOException {
		LOGGER.info("beforeAll()");
//		var pb = new ProcessBuilder("cmd.exe", "/c", "cd \"C:\\git-repo\\ipm-be\\ipm-be\\investment-portfolio-batch\\src\\test\\resources\\web-server\" && mongoose.exe");
//		pb.redirectErrorStream(true);
//		var process = pb.start();
		//assertThat(process.exitValue(), is(equals(0)));
		//var directory = "C:/git-repo/ipm-be/ipm-be/investment-portfolio-batch/src/test/resources/web-server";
		//var directory = "src/test/resources/web-server";

		var pb = new ProcessBuilder(getOsExecutable());
		pb.directory(new File(WEB_SERVER_DIRECTORY));
		pb.redirectErrorStream(true);
		process = pb.start();
		var pid = process.pid();
		LOGGER.info("web server started with pid: {}", pid);

		
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		executor.submit(() -> {
//			//var pb = new ProcessBuilder("cmd.exe", "/c", "cd \"C:\\git-repo\\ipm-be\\ipm-be\\investment-portfolio-batch\\src\\test\\resources\\web-server\" && mongoose.exe");
//			var pb = new ProcessBuilder("C:/git-repo/ipm-be/ipm-be/investment-portfolio-batch/src/test/resources/web-server/mongoose.exe");
//			pb.directory(new File("C:/git-repo/ipm-be/ipm-be/investment-portfolio-batch/src/test/resources/web-server"));
//			pb.redirectErrorStream(true);
//			try {
//				var process = pb.start();
//				var pid = process.pid();
//				LOGGER.info("pid: {}", pid);
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//						
//		});
//		var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//		var line = "";t
//		while (true) {
//			line = br.readLine();
//			if (line == null) {
//				break;
//			}
//			LOGGER.info(line);
//		}
	}

	@Test
	void test1(TestInfo testInfo) throws InterruptedException {
		printTestName(testInfo);
		//TimeUnit.MINUTES.sleep(1);
		RestTemplate restTemplate = new RestTemplate();
		String quote = restTemplate.getForObject("http://localhost:8000/BCE.TO.html", String.class);
		LOGGER.info("quote: {}", quote);
		assertThat(quote, notNullValue());
	}
	@Test
	void test2(TestInfo testInfo) {
		printTestName(testInfo);
		var i = 7;
		assertThat(i, is(7));
	}
	
	private String getOsExecutable() {
		LOGGER.info("OS: {}", System.getProperty("os.name"));
		if (StringUtils.containsIgnoreCase(System.getProperty("os.name"), "windows")) {
			return WEB_SERVER_WINDOWS_EXE;
		} else {
			var exe = new File(WEB_SERVER_LINUX_EXE);
			exe.setExecutable(true);
			return WEB_SERVER_LINUX_EXE;
		}
	}
}
