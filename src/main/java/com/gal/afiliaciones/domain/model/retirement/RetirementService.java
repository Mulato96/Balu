package com.gal.afiliaciones.domain.model.retirement;

import java.util.List;

public interface RetirementService {
    List<ContractListResponseDto> searchWorker(WorkerSearchRequestDto request);
    RetirementResponseDto requestRetirement(RetirementRequestDto request);
}