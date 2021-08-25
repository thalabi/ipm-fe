package com.kerneldc.springsecurityjwt.csv.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.springsecurityjwt.csv.domain.Sales;
import com.kerneldc.springsecurityjwt.csv.domain.UploadTableEnum;

public interface SalesRepository extends BaseTableRepository<Sales, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.SALES;
	}

}
