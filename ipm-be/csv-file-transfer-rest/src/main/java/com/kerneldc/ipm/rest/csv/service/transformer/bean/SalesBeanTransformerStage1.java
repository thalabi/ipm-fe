package com.kerneldc.ipm.rest.csv.service.transformer.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.Sales;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.BeanTransformerException;

import lombok.RequiredArgsConstructor;

/**
 * Bean transformer validates paymentType to be Visa or Mastercard
 * 
 * @author Tarif Halabi
 *
 */
@Component
@RequiredArgsConstructor
public class SalesBeanTransformerStage1 implements IBeanTransformer {

	private static final List<String> VALID_PAYMENT_TYPES = List.of("visa", "mastercard");
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException {
		
		var inputPriceList = context.getBeans();
		var outputSalesList = new ArrayList<AbstractPersistableEntity>();
		
		for (AbstractPersistableEntity bean : inputPriceList) {
			var sales = SerializationUtils.clone((Sales) bean);

			var exceptionMessageJoiner = new StringJoiner(". ", StringUtils.EMPTY, ".");
			var exceptionsFound = false;
			
			if (! /* not */ VALID_PAYMENT_TYPES.contains(sales.getPaymentType().toLowerCase())) {
				exceptionMessageJoiner.add(String.format("Payment type [%s] not valid", sales.getPaymentType()));
				exceptionsFound = true;
			}
			
			if (exceptionsFound) {
				context.getBeanTransformerExceptionList().add(new BeanTransformerException(getTransformerName(), sales, exceptionMessageJoiner.toString()));
			} else {
				outputSalesList.add(sales);

			}
		}
		
		context.setBeans(outputSalesList);
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(UploadTableEnum.SALES)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return"SalesBeanTransformerStage1";
	}
}
