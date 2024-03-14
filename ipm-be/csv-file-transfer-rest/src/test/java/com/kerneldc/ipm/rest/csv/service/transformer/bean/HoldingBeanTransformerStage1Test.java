package com.kerneldc.ipm.rest.csv.service.transformer.bean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;

class HoldingBeanTransformerStage1Test {

	private static final String TICKER1 = "T";
	private static final ExchangeEnum EXCHANGE1 = ExchangeEnum.NYSE;
	private static final FinancialInstitutionEnum INSTITUTION1 = FinancialInstitutionEnum.TD;
	private static final String ACCOUNT_NUMBER1 = "123456S";
	private static HoldingBeanTransformerStage1 holdingBeanTransformerStage1;
	private static InstrumentRepository instrumentRepository;
	private static PortfolioRepository portfolioRepository;
	private FileProcessingContext context;

	@BeforeAll
	static void setup() {
		instrumentRepository = mock(InstrumentRepository.class);
		portfolioRepository = mock(PortfolioRepository.class);
		holdingBeanTransformerStage1 = new HoldingBeanTransformerStage1(instrumentRepository, portfolioRepository);
	}
	
	@BeforeEach
	void init() {
		context = new FileProcessingContext(InvestmentPortfolioTableEnum.HOLDING);
	}

	@Test
	void testValidHolding() {
		var holding1 = new Holding();
		holding1.setTicker(TICKER1);
		holding1.setExchange(EXCHANGE1);

		holding1.setFinancialInstitution(INSTITUTION1);
		holding1.setAccountNumber(ACCOUNT_NUMBER1);

		context.setBeans(List.of(holding1));
		
		var instrument1 = new Instrument();
		var instrumentStock = new InstrumentStock();
		instrumentStock.setInstrument(instrument1);
		instrument1.setTicker(TICKER1);
		instrumentStock.setExchange(EXCHANGE1);
		when(instrumentRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getTicker(), holding1.getExchange()))).thenReturn(List.of(instrument1));
		
		var portfolio1 = new Portfolio();
		portfolio1.setFinancialInstitution(INSTITUTION1);
		portfolio1.setAccountId(ACCOUNT_NUMBER1);
		when(portfolioRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getFinancialInstitution().getInstitutionNumber(), holding1.getAccountNumber()))).thenReturn(List.of(portfolio1));

		try {
			holdingBeanTransformerStage1.transform(context);
		} catch (AbortFileProcessingException e) {
			e.printStackTrace();
			assertThat("Test failed. holdingBeanTransformerStage1.transform() threw an exception.", false);
		}
		assertThat(context.getBeanTransformerExceptionList().size(), equalTo(0));
	}

	@Test
	void testInstrumentLookupByLogicalLeyFails() {
		var holding1 = new Holding();
		holding1.setTicker(TICKER1);
		holding1.setExchange(EXCHANGE1);

		holding1.setFinancialInstitution(INSTITUTION1);
		holding1.setAccountNumber(ACCOUNT_NUMBER1);

		context.setBeans(List.of(holding1));

		when(instrumentRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getTicker(), holding1.getExchange()))).thenReturn(List.of());
		var portfolio1 = new Portfolio();
		portfolio1.setFinancialInstitution(INSTITUTION1);
		portfolio1.setAccountId(ACCOUNT_NUMBER1);
		when(portfolioRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getFinancialInstitution().getInstitutionNumber(), holding1.getAccountNumber()))).thenReturn(List.of(portfolio1));
		
		try {
			holdingBeanTransformerStage1.transform(context);
		} catch (AbortFileProcessingException e) {
			e.printStackTrace();
			assertThat("Test failed. holdingBeanTransformerStage1.transform() threw an exception.", false);
		}
		assertThat(context.getBeanTransformerExceptionList().size(), equalTo(1));
		System.out.println(context.getBeanTransformerExceptionList().get(0));
		assertThat(context.getBeanTransformerExceptionList().get(0).getTransformerName(), equalTo(holdingBeanTransformerStage1.getTransformerName()));
		assertThat(context.getBeanTransformerExceptionList().get(0).getMessage(), containsStringIgnoringCase("instrument"));
	}

}
