package com.kerneldc.ipm.batch.pricing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.alphavantage.YahooApiQuote;
import com.kerneldc.ipm.commonservices.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;
@Slf4j
class TestYahooApiParse {

	private static final String yahooFinanceApiUrlTemplate = "https://query1.finance.yahoo.com/v8/finance/chart/%s?&interval=1d";
	@Test
	void test1() {
		boolean a = true;
		assertThat(a, is(true));
	}
	
	@Test
	void testBceUrl() throws IOException, ApplicationException {
		var objectMapper = new ObjectMapper();
		//objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
		
		HttpUtil httpUtil = new HttpUtil(null, yahooFinanceApiUrlTemplate);
		var urlContent = httpUtil.yahooFinanceApiContent("BCE.TO");
		assertThat(urlContent, is(notNullValue()));
		LOGGER.info("urlContent: {}", urlContent);
		
		objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		var yahooApiQuote = objectMapper.readValue(urlContent, YahooApiQuote.class);
		assertThat(yahooApiQuote, is(notNullValue()));
		LOGGER.info("yahooApiQuote: {}", yahooApiQuote);
		
		var priceTimestamp = yahooApiQuote.getPriceTimestamp();
		assertThat(priceTimestamp, is(notNullValue()));
		LOGGER.info("priceTimestamp: {}", priceTimestamp);
		
		var price = yahooApiQuote.getPrice();
		assertThat(price, is(notNullValue()));
		LOGGER.info("price: {}", price);
	}
	
	@Test
	void testInvalidTicker() throws IOException {
		HttpUtil httpUtil = new HttpUtil(null, yahooFinanceApiUrlTemplate);
		ApplicationException exception = assertThrows(ApplicationException.class, () -> {
			httpUtil.yahooFinanceApiContent("INVALID");
		});
		var message = exception.getMessage();
		LOGGER.info("message: {}", message);
		assertThat(message, is(notNullValue()));
		var siteMessage = message.substring(message.indexOf("Site message: ")+14);
		var jsonString = siteMessage.substring(1, siteMessage.length()-1);
		LOGGER.info("jsonString: {}", jsonString);
		var objectMapper = new ObjectMapper();
		var json = objectMapper.readTree(jsonString);
		LOGGER.info("json: {}", json);

		var errorCode = json.get("chart").get("error").get("code").textValue();
		LOGGER.info("errorCode: {}", errorCode);
		assertThat(errorCode, equalTo("Not Found"));
		
		var errorDescription = json.get("chart").get("error").get("description").textValue();
		LOGGER.info("errorDescription: {}", errorDescription);
		assertThat(errorDescription, equalTo("No data found, symbol may be delisted"));
	}
	
	@Test
	void testJsonRootNameAnnotation() throws IOException {
		var pojoString = """
				{
				"root": {
		          "firstName": "CAD",
		          "lastName": "BCE.TO"
				  }
				}
				""";
		var objectMapper = new ObjectMapper();
		//objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
		
		objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		var pojo = objectMapper.readValue(pojoString, PojoWithJsonRootNameAnnotation.class);
		assertThat(pojo, is(notNullValue()));
		LOGGER.info("pojo: {}", pojo);
	}

	@Test
	void testJsonRootNameWithoutAnnotation() throws IOException {
		var pojoString = """
				{
				"root": {
		          "firstName": "CAD",
		          "lastName": "BCE.TO"
				  }
				}
				""";
		var objectMapper = new ObjectMapper();
		//objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
		
		var pojo = objectMapper.readValue(pojoString, PojoWithOutJsonRootNameAnnotation.class);
		assertThat(pojo, is(notNullValue()));
		LOGGER.info("pojo: {}", pojo);
	}
}
