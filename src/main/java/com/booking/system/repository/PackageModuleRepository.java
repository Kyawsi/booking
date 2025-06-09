package com.booking.system.repository;

import com.booking.system.dto.PackageResponse;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.PackageModule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageModuleRepository extends CrudRepository<PackageModule,Long> {

    @Query("SELECT new com.booking.system.dto.PackageResponse(" +
            " p.id as packageId, " +
            " p.name as name, " +
            " c.name as countryName ," +
            " p.price as price, " +
            " p.creditAmount as creditAmount, " +
            " p.expirationDays as expirationDays, " +
            " p.createdOn as createdOn, " +
            " p.updatedOn as updatedOn," +
            " p.status as status )" +
            " FROM PackageModule p" +
            " JOIN Country c ON c.id=p.country " +
            " WHERE p.status = 'Available' " +
            " AND c=:country " +
            " ORDER BY p.id desc "
    )
    List<PackageResponse> findPackageByCountry(@Param("country") Country country,
                                               Pageable pageable);

    @Query("SELECT COUNT(p) " +
            "FROM PackageModule p " +
            "JOIN Country c ON c.id = p.country " +
            "WHERE p.status = 'Available' AND c = :country")
    long countPackageByCountry(@Param("country") Country country);

    boolean existsByNameAndCountry(String name, Country country);

}
