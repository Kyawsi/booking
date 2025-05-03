package com.booking.system.repository;

import com.booking.system.dto.ClassScheduleResponse;
import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.Country;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassScheduleRepository extends CrudRepository<ClassSchedule,Long> {

    @Query("SELECT new com.booking.system.dto.ClassScheduleResponse(" +
            " cs.title as title, " +
            " cs.requiredCredits as requiredCredits ," +
            " cs.startTime as startTime, " +
            " cs.endTime as endTime, " +
            " cs.slotCount as slotCount, " +
            " cs.createdOn as createdOn, " +
            " cs.updatedOn as updatedOn," +
            " c.name as countryName )" +
            " FROM ClassSchedule cs" +
            " JOIN Country c ON c.id=cs.country " +
            " WHERE c=:country" +
            " Order by cs.id desc"
    )
    List<ClassScheduleResponse> findClassScheduleByCountry(@Param("country") Country country,
                                                           Pageable pageable);

    @Query("SELECT COUNT(cs) FROM ClassSchedule cs WHERE cs.country = :country")
    long countClassScheduleByCountry(@Param("country") Country country);

}
