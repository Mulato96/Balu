package com.gal.afiliaciones.infrastructure.dao.repository.addoption;

import com.gal.afiliaciones.domain.model.IconList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IconListRepository extends JpaRepository<IconList,Long> {
}
