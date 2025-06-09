package com.booking.system.repository;

import com.booking.system.dto.LoginProfileResponse;
import com.booking.system.entity.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    @Query("SELECT new com.booking.system.dto.LoginProfileResponse(" +
            " u.email as email, " +
            " u.name as name, " +
            " c.name as countryName ," +
            " u.isVerified as isVerified )" +
            " FROM User u" +
            " JOIN Country c ON c.id=u.country" +
            " WHERE u=:user")
    Optional<LoginProfileResponse> findProfileByGuid(@Param("user") User user);
}
