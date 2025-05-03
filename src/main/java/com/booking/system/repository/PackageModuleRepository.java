package com.booking.system.repository;

import com.booking.system.dto.LoginProfileResponse;
import com.booking.system.dto.PackageResponse;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.model.PackageModule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageModuleRepository extends CrudRepository<PackageModule,Long> {

    @Query("SELECT new com.booking.system.dto.PackageResponse(" +
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
            " AND c=:country")
    Optional<PackageResponse> findPackageByCountry(@Param("country") Country country);

}
