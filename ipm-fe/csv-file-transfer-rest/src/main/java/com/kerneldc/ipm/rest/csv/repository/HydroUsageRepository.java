package com.kerneldc.ipm.rest.csv.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.domain.HydroUsage;
import com.kerneldc.ipm.rest.csv.domain.UploadTableEnum;

public interface HydroUsageRepository extends BaseTableRepository<HydroUsage, Long> {

	
	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.HYDRO_USAGE;
	}
	
}
