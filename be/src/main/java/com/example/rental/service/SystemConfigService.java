package com.example.rental.service;

import com.example.rental.dto.system.SystemConfigDto;
import com.example.rental.dto.system.SystemConfigUpsertRequest;

public interface SystemConfigService {
    SystemConfigDto get();
    SystemConfigDto upsert(SystemConfigUpsertRequest request);
}
