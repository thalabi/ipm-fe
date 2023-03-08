package com.kerneldc.ipm.rest.csv.service.transformer.bean;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.domain.Position;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.BeanTransformerException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PositionBeanTransformerStage1 implements IBeanTransformer {

	private final InstrumentRepository instrumentRepository;
	private final PortfolioRepository portfolioRepository;
	private final PriceRepository priceRepository;
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException{
		
		//var context = FileProcessingContext.get();
		var inputHoldingList = context.getBeans();
		//var transformerExceptionList = beanTransformerResult.beanTransformerExceptionList();
		
		//List<Position> transformedHoldingList = new ArrayList<>();
		context.getBeans().clear();
		
		for (AbstractPersistableEntity bean : inputHoldingList) {
			var position = SerializationUtils.clone((Position) bean);

			var exceptionMessageJoiner = new StringJoiner(". ", StringUtils.EMPTY, ".");
			var exceptionsFound = false;
			
			List<Instrument> instrumentList = instrumentRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(position.getTicker(), position.getExchange()));
			if (CollectionUtils.isEmpty(instrumentList)) {
				exceptionMessageJoiner.add(String.format(" Instrument not found with ticker: [%s] and exchange: [%s].", position.getTicker(), position.getExchange()));
				exceptionsFound = true;
			} else {
				position.setInstrument(instrumentList.get(0));
			}
			
			List<Portfolio> portfolioList = portfolioRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(position.getInstitution(), position.getAccountNumber()));
			if (CollectionUtils.isEmpty(portfolioList)) {
				exceptionMessageJoiner.add(String.format(" Posrtfolio not found with instititution: [%s] and account number: [%s].", position.getInstitution(), position.getAccountNumber()));
				exceptionsFound = true;
			} else {
				position.setPortfolio(portfolioList.get(0));
			}

			if (position.getInstrument() != null) {
				List<Price> priceList = priceRepository.findByLogicalKeyHolder(LogicalKeyHolder.build(position.getInstrument().getId(), position.getPriceTimestamp()));
				if (CollectionUtils.isEmpty(priceList)) {
					exceptionMessageJoiner.add(String.format(" Price not found with instrument: [%s:%s] and price timestamp: [%s].", position.getInstrument().getTicker(), position.getInstrument().getExchange(), position.getPriceTimestamp()));
					exceptionsFound = true;
				} else {
					position.setPrice(priceList.get(0));
				}
			}
			
			
			
			if (exceptionsFound) {
				context.getBeanTransformerExceptionList().add(new BeanTransformerException(getTransformerName(), position, exceptionMessageJoiner.toString()));
			} else {
				//transformedHoldingList.add(position);
				context.getBeans().add(position);;
			}
		}
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(InvestmentPortfolioTableEnum.POSITION)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return"PositionBeanTransformerStage1";
	}
}
