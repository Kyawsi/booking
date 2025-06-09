package com.booking.system.repository;


import com.booking.system.entity.model.Booking;
import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.User;
import com.booking.system.entity.model.UserPackage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends CrudRepository<Booking,Long> {
    boolean existsByUserAndScheduleAndUserPackage(User user, ClassSchedule schedule, UserPackage userPackage);

    int countByScheduleAndStatus(ClassSchedule schedule, String status);

    List<Booking>findByUserAndStatus(User user, String status);
}
