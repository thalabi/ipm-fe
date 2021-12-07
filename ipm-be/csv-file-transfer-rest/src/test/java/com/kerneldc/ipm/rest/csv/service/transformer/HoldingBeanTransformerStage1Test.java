package com.kerneldc.ipm.rest.csv.service.transformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService.BeanTransformerResult;

class HoldingBeanTransformerStage1Test {

	private static final String TICKER1 = "T";
	private static final String EXCHANGE1 = "NYSE";
	private static final String INSTITUTION1 = "WealthSimple";
	private static final String ACCOUNT_NUMBER1 = "123456S";
	private static HoldingBeanTransformerStage1 holdingBeanTransformerStage1;
	private static InstrumentRepository instrumentRepository;
	private static PortfolioRepository portfolioRepository;

	@BeforeAll
	static void setup() {
		instrumentRepository = mock(InstrumentRepository.class);
		portfolioRepository = mock(PortfolioRepository.class);
		holdingBeanTransformerStage1 = new HoldingBeanTransformerStage1(instrumentRepository, portfolioRepository);
	}

	@Test
	void testValidHolding() {
		var holding1 = new Holding();
		holding1.setTicker(TICKER1);
		holding1.setExchange(EXCHANGE1);

		holding1.setInstitution(INSTITUTION1);
		holding1.setAccountNumber(ACCOUNT_NUMBER1);

		var beanList1 = List.of(holding1);
		
		var beanTransformerResult1 = new BeanTransformerResult(beanList1, new ArrayList<TransformerException>());
		
		var instrument1 = new Instrument();
		instrument1.setTicker(TICKER1);
		instrument1.setExchange(EXCHANGE1);
		when(instrumentRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getTicker(), holding1.getExchange()))).thenReturn(List.of(instrument1));
		
		var portfolio1 = new Portfolio();
		portfolio1.setInstitution(INSTITUTION1);
		portfolio1.setAccountNumber(ACCOUNT_NUMBER1);
		when(portfolioRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getInstitution(), holding1.getAccountNumber()))).thenReturn(List.of(portfolio1));

		BeanTransformerResult beanTransformerResult = null;
		try {
			beanTransformerResult = holdingBeanTransformerStage1.transform(beanTransformerResult1);
		} catch (TransformerException e) {
			e.printStackTrace();
			assertThat("Test failed. holdingBeanTransformerStage1.transform() threw an exception.", false);
		}
		assertThat(beanTransformerResult.transformerExceptionList().size(), equalTo(0));
	}

	@Test
	void testInstrumentLookupByLogicalLeyFails() {
		var holding1 = new Holding();
		holding1.setTicker(TICKER1);
		holding1.setExchange(EXCHANGE1);

		holding1.setInstitution(INSTITUTION1);
		holding1.setAccountNumber(ACCOUNT_NUMBER1);

		var beanList1 = List.of(holding1);
		
		var beanTransformerResult1 = new BeanTransformerResult(beanList1, new ArrayList<TransformerException>());
		
		when(instrumentRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getTicker(), holding1.getExchange()))).thenReturn(List.of());
		when(portfolioRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(holding1.getInstitution(), holding1.getAccountNumber()))).thenReturn(List.of());
		
		BeanTransformerResult beanTransformerResult = null;
		try {
			beanTransformerResult = holdingBeanTransformerStage1.transform(beanTransformerResult1);
		} catch (TransformerException e) {
			e.printStackTrace();
			assertThat("Test failed. holdingBeanTransformerStage1.transform() threw an exception.", false);
		}
		assertThat(beanTransformerResult.transformerExceptionList().size(), equalTo(1));
		System.out.println(beanTransformerResult.transformerExceptionList().get(0));
		assertThat(beanTransformerResult.transformerExceptionList().get(0).getTransformerName(), equalTo(holdingBeanTransformerStage1.getTransformerName()));
		assertThat(beanTransformerResult.transformerExceptionList().get(0).getMessage(), containsStringIgnoringCase("instrument"));
		assertThat(beanTransformerResult.transformerExceptionList().get(0).getMessage(), containsStringIgnoringCase("portfolio"));
	}

}
