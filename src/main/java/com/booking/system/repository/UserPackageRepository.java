package com.booking.system.repository;

import com.booking.system.dto.UserPackageHistoryProjection;
import com.booking.system.entity.model.User;
import com.booking.system.entity.model.PackageModule;
import com.booking.system.entity.model.UserPackage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPackageRepository extends CrudRepository<UserPackage,Long> {

    Optional<UserPackage> findByUserAndPackageModule(User user, PackageModule packageModule);

    @Query(value = "SELECT " +
            "up.id as ownPackageId, " +
            "up.remaining_credits AS remainingCredits, " +
            "u.name AS name, " +
            "p.name AS packageName, " +
            "p.price AS price, " +
            "p.credit_amount AS creditAmount, " +
            "p.expiration_days AS expirationDays, " +
            "up.created_on AS createdOn, " +
            "up.updated_on AS updatedOn " +
            "FROM user_package up " +
            "JOIN package p ON p.id = up.package_id " +
            "JOIN oauth_user u ON u.id = up.user_id " +
            "WHERE u.id = :userId " +
            "ORDER BY up.id desc ",
            nativeQuery = true)
    List<UserPackageHistoryProjection> findPurchasedPackageHistoryNative(
            @Param("userId") Long userId,
            Pageable pageable);


    @Query(value = "SELECT COUNT(*) FROM user_package up " +
            "JOIN package p ON p.id = up.package_id " +
            "JOIN oauth_user u ON u.id = up.user_id " +
            "WHERE u.id = :userId",
            nativeQuery = true)
    long countPurchasedPackageHistory(@Param("userId") Long userId);

}
