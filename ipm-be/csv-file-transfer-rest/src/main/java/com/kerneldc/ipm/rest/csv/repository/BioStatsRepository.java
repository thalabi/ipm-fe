package com.kerneldc.ipm.rest.csv.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.domain.BioStats;
import com.kerneldc.ipm.rest.csv.domain.UploadTableEnum;

public interface BioStatsRepository extends BaseTableRepository<BioStats, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.BIO_STATS;
	}
}
