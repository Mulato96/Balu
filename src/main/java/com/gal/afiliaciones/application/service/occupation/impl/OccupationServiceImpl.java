package com.gal.afiliaciones.application.service.occupation.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.occupation.OccupationService;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {

    private final GenericWebClient webClient;

    private static final List<String> excludedOccupations = Arrays.asList(
            "7111", "9210", "1078", "1579", "1654", "1352", "1353", "1354", "1355", "1356", "1357", "1358", "1359",
            "1360", "1361", "1362", "1363", "1364", "1365", "1366", "1367", "8321", "8322", "8323", "8324", "9331", "9332");

    @Override
    public List<Occupation> findOccupationsProvisionService(){
        List<Occupation> allOccupations = webClient.getAllOccupations();
        ObjectMapper mapper = new ObjectMapper();
        List<Occupation> occupationList = mapper.convertValue(allOccupations,
                new TypeReference<List<Occupation>>() {
                });
        List<Occupation> response = new ArrayList<>();

        if(!allOccupations.isEmpty()){
            response = occupationList.stream()
                            .filter(occupation -> !excludedOccupations.contains(occupation.getCodeOccupation()))
                            .toList();
        }

        return response;
    }

}
