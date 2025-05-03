package com.booking.system.repository;


import com.booking.system.entity.model.Booking;
import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.model.UserPackage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends CrudRepository<Booking,Long> {
    boolean existsByUserAndScheduleAndUserPackage(OAuthUser user, ClassSchedule schedule, UserPackage userPackage);

    int countByScheduleAndStatus(ClassSchedule schedule, String status);

}
