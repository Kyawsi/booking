package com.booking.system.repository;

import com.booking.system.dto.CountryResponse;
import com.booking.system.entity.model.Country;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends CrudRepository<Country,Long> {
    Optional<Country> findByGuid(String guid);

    @Query("SELECT new com.booking.system.dto.CountryResponse(" +
            " c.guid as countryGuid, " +
            " c.name as name, " +
            " c.createdOn as createdOn, " +
            " c.updatedOn as updatedOn )" +
            " FROM Country c" +
            " ORDER BY c.id desc "
    )
    List<CountryResponse> findCountry(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Country c")
    long countCountry();

}
