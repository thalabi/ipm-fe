package com.kerneldc.ipm.batch.pricing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.commonservices.util.HttpUtil;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.extern.slf4j.Slf4j;

@TestInstance(Lifecycle.PER_CLASS) // to be able to use non-static @BeforeAll and @AfterAll methods
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.properties")
@Slf4j
class StockPriceServiceTest extends AbstractBaseTest { // TODO fix to use com.kerneldc.common.AbstractBaseTest

	private static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";
	private static final String WEB_SERVER_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/web-server";
	private static final String WEB_SERVER_WINDOWS_EXE = "mongoose_windows.exe";
	private static final String WEB_SERVER_LINUX_EXE = "mongoose_linux";
	private Process process;

	private static StockAndEtfPriceService stockAndEtfPriceService;

	@Mock
	private PriceRepository priceRepository;
	@Mock
	private HttpUtil httpUtil;
	
	@Value("${alphavantage.api.url.template}")
	private String alphavantageApiUrlTemplate;
	@Value("${alphavantage.api.key}")
	private String alphavantageApiKey;
	@Value("${yahoo.finance.api.url.template}")
	private String yahooFinanceApiUrlTemplate;
	
	@BeforeAll
	public void beforeAll() throws IOException {
		LOGGER.info("beforeAll()");
		
		var latestPriceList = List.of(price1, price2);
		when(priceRepository.findLatestPriceList()).thenReturn(latestPriceList);
		
		
		stockAndEtfPriceService = new StockAndEtfPriceService(List.of(new AlphaVantageQuoteService(httpUtil)), priceRepository);
		
		//spinupWebServer();
	}

	@Test
	void testAlphaVantageQuoteOnce_Success(TestInfo testInfo) throws ApplicationException {
		printTestName(testInfo);
		var instrument = new Instrument();
		var instrumentStock = new InstrumentStock();
		instrumentStock.setInstrument(instrument);
		instrument.setTicker("BCE");
		instrumentStock.setExchange(ExchangeEnum.TSE);
		
		var urlContent = """
			{
		    "Global Quote": {
		        "01. symbol": "BCE",
		        "02. open": "17.5900",
		        "03. high": "17.7100",
		        "04. low": "17.4900",
		        "05. price": "17.6100",
		        "06. volume": "35255306",
		        "07. latest trading day": "2024-04-03",
		        "08. previous close": "17.5200",
		        "09. change": "0.0900",
		        "10. change percent": "0.5137%"
				}
			}
		""";
	    when(httpUtil.alphavantageApiContent("BCE.TO")).thenReturn(urlContent);
		
	    var snapshotDateTime = AppTimeUtils.toOffsetDateTime(LocalDateTime.of(2024, 04, 03, 17, 0));
	    
		var priceQuote = stockAndEtfPriceService.quote(snapshotDateTime, instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}

	@Test
	void testQuoteThatFailsAndUsesLatestPrice(TestInfo testInfo) throws ApplicationException {
		printTestName(testInfo);
		var instrumentStock = new InstrumentStock();
		instrumentStock.setInstrument(instrument2);
		instrumentStock.setExchange(ExchangeEnum.NYSE);
		
		when(httpUtil.alphavantageApiContent("T")).thenThrow(new ApplicationException());
		
		var priceQuote = stockAndEtfPriceService.quote(OffsetDateTime.now(), instrument2, instrumentStock);
		System.out.println(priceQuote);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	

	private static final Price price1 = new Price();
	private static final Price price2 = new Price();
	private static final Instrument instrument1 = new Instrument();
	private static final Instrument instrument2 = new Instrument();
	static {
		instrument1.setId(1L);
		instrument1.setTicker("BCE");
		price1.setId(1L);
		price1.setInstrument(instrument1);
		price1.setPrice(new BigDecimal("1"));
		price1.setPriceTimestamp(OffsetDateTime.of(2024, 04, 15, 07, 07, 07, 07, ZoneOffset.ofHours(4)));

		instrument2.setId(2L);
		instrument2.setTicker("T");
		price2.setId(2L);
		price2.setInstrument(instrument2);
		price2.setPrice(new BigDecimal("2"));
		price2.setPriceTimestamp(OffsetDateTime.of(2024, 04, 29, 07, 07, 07, 07, ZoneOffset.UTC));
	}

//	@AfterAll
//	public void afterAll() {
//		LOGGER.info("afterAll()");
//		var pid = process.pid();
//		LOGGER.info("killing web server pid: {}", pid);
//		process.destroy();
//	}

	private void spinupWebServer() throws IOException {
		var pb = new ProcessBuilder(getOsExecutable());
		pb.directory(new File(WEB_SERVER_DIRECTORY));
		pb.redirectErrorStream(true);
		process = pb.start();
		var pid = process.pid();
		LOGGER.info("web server started with pid: {}", pid);
	}

	private String getOsExecutable() {
		LOGGER.info("OS: {}", System.getProperty("os.name"));
		if (StringUtils.containsIgnoreCase(System.getProperty("os.name"), "windows")) {
			return WEB_SERVER_DIRECTORY + "/" + WEB_SERVER_WINDOWS_EXE;
		} else {
			var exe = new File(WEB_SERVER_DIRECTORY + "/" + WEB_SERVER_LINUX_EXE);
			exe.setExecutable(true);
			return "./" + WEB_SERVER_LINUX_EXE;
		}
	}
	
}
