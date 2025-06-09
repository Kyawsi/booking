package com.booking.system.repository;

import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.User;
import com.booking.system.entity.model.UserPackage;
import com.booking.system.entity.model.WaitingList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingListRepository extends CrudRepository<WaitingList,Long> {
    Optional<WaitingList> findFirstByScheduleOrderByCreatedOnAsc(ClassSchedule schedule);
    boolean existsByUserAndScheduleAndUserPackage(User user, ClassSchedule schedule, UserPackage userPackage);
    List<WaitingList>findBySchedule(ClassSchedule schedule);
    List<WaitingList>findByUser(User user);
}
