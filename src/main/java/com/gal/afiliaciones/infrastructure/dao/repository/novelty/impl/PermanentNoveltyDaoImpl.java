package com.gal.afiliaciones.infrastructure.dao.repository.novelty.impl;

import com.gal.afiliaciones.config.ex.NoveltyException;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TypeOfUpdateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.NoveltySpecification;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PermanentNoveltyDaoImpl implements PermanentNoveltyDao {

    private final PermanentNoveltyRepository repository;
    private final TypeOfUpdateRepository noveltyTypeRepository;
    private final NoveltyStatusRepository noveltyStatusRepository;
    private final RequestChannelRepository channelRepository;

    @Override
    public PermanentNovelty createNovelty(PermanentNovelty novelty){
        return repository.save(novelty);
    }

    @Override
    public Page<PermanentNovelty> findByFilters(FilterConsultNoveltyDTO filter){
        String sortBy = (filter.getSortBy()==null || filter.getSortBy().isBlank()) ? "id" : filter.getSortBy();
        String sortOrder = (filter.getSortOrder()==null || filter.getSortOrder().isBlank()) ? "ASC" : filter.getSortOrder();
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        return repository.findAll(getSpecificationNovelty(filter), pageable);
    }

    @Override
    public List<PermanentNovelty> exportAllData(FilterConsultNoveltyDTO filter) {
        return repository.findAll(getSpecificationNovelty(filter));
    }

    private Specification<PermanentNovelty> getSpecificationNovelty(FilterConsultNoveltyDTO filter) {
        return Specification.where(NoveltySpecification.findByIdentificationDocumentType(filter.getIdentificationDocumentType()))
                .and(NoveltySpecification.findByIdentificationDocumentNumber(filter.getIdentificationDocumentNumber()))
                .and(NoveltySpecification.findByNoveltyType(getNoveltyTypeById(filter.getNoveltyTypeId())))
                .and(NoveltySpecification.findByNoveltyStatus(getNoveltyStatusById(filter.getNoveltyStatusId())))
                .and(NoveltySpecification.findByStartDate(filter.getStartDate()))
                .and(NoveltySpecification.findByEndDate(filter.getEndDate()))
                .and(NoveltySpecification.findByRequestChannel(getRequestChannelById(filter.getRequestChannelId())));
    }

    private TypeOfUpdate getNoveltyTypeById(Long noveltyTypeId){
        if(noveltyTypeId != null && noveltyTypeId>0L)
            return noveltyTypeRepository.findById(noveltyTypeId).orElse(null);
        return null;
    }

    private NoveltyStatus getNoveltyStatusById(Long statusId){
        if(statusId != null && statusId>0L)
            return noveltyStatusRepository.findById(statusId).orElse(null);
        return null;
    }

    private RequestChannel getRequestChannelById(Long channelId){
        if(channelId != null && channelId>0L)
            return channelRepository.findById(channelId).orElse(null);
        return null;
    }

    @Override
    public PermanentNovelty findById(Long id){
        return repository.findById(id).orElseThrow(() -> new NoveltyException("Novelty not found"));
    }

}
