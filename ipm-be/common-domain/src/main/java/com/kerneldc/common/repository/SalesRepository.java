package com.kerneldc.common.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.Sales;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface SalesRepository extends BaseTableRepository<Sales, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.SALES;
	}

}
