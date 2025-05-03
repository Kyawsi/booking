package com.booking.system.repository;

import com.booking.system.entity.model.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends CrudRepository<Country,Long> {
    Optional<Country> findByGuid(String guid);
}
