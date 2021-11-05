package com.kerneldc.common.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.HydroUsage;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface HydroUsageRepository extends BaseTableRepository<HydroUsage, Long> {

	
	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.HYDRO_USAGE;
	}
	
}
