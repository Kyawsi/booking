package com.booking.system.service.impl;

import com.booking.system.entity.model.*;
import com.booking.system.entity.request.BookingCancelRequest;
import com.booking.system.entity.request.BookingRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.repository.*;
import com.booking.system.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat booking(BookingRequest request, String username) {
        ResponseFormat responseFormat = null;

        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

            UserPackage userPackage = userPackageRepository.findById(request.getUserPackageId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Package not found"));

            if (!userPackage.getUser().equals(user)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not own this package.");
            }

            ClassSchedule classSchedule = classScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));

            checkForOverlappingBookings(user, classSchedule);

            Integer requiredCredits = classSchedule.getRequiredCredits();
            Integer remainingCredits = userPackage.getRemainingCredits();

            String lockKey = "booking_lock_" + classSchedule.getId();
            RLock lock = redissonClient.getLock(lockKey);

            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many booking attempts, please try again later.");
            }

            try {
                boolean alreadyBooked = bookingRepository.existsByUserAndScheduleAndUserPackage(user, classSchedule, userPackage);
                if (alreadyBooked) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this class with the selected package.");
                }

                boolean alreadyOnWaitlist = waitingListRepository.existsByUserAndScheduleAndUserPackage(user, classSchedule, userPackage);
                if (alreadyOnWaitlist) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already on the waitlist for this class.");
                }

                int bookedCount = bookingRepository.countByScheduleAndStatus(classSchedule, "booked");
                if (bookedCount >= classSchedule.getSlotCount()) {
                    if (remainingCredits == null || remainingCredits < requiredCredits) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough credits to be added to the waitlist");
                    }

                    userPackage.setRemainingCredits(remainingCredits - requiredCredits);
                    userPackage.setUpdatedOn(ZonedDateTime.now());
                    userPackageRepository.save(userPackage);

                    WaitingList waitlist = WaitingList.builder()
                            .user(user)
                            .userPackage(userPackage)
                            .schedule(classSchedule)
                            .createdOn(ZonedDateTime.now())
                            .updatedOn(ZonedDateTime.now())
                            .build();
                    waitingListRepository.save(waitlist);

                    responseFormat = new ResponseFormat();
                    responseFormat.setSuccess(true);
                    responseFormat.setMessage(Optional.of("Class full. You have been added to the waitlist."));
                    return responseFormat;
                }

                if (requiredCredits == null || requiredCredits <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid required credit configuration for class");
                }

                if (remainingCredits == null || remainingCredits < requiredCredits) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough credits to book this class");
                }

                userPackage.setRemainingCredits(remainingCredits - requiredCredits);
                userPackage.setUpdatedOn(ZonedDateTime.now());
                userPackageRepository.save(userPackage);

                String status = "booked";
                Booking booking = Booking.builder()
                        .user(user)
                        .userPackage(userPackage)
                        .schedule(classSchedule)
                        .status(status)
                        .createdOn(ZonedDateTime.now())
                        .updatedOn(ZonedDateTime.now())
                        .build();
                bookingRepository.save(booking);

                responseFormat = new ResponseFormat();
                responseFormat.setSuccess(true);
                responseFormat.setMessage(Optional.of("Class booked successfully"));
                responseFormat.setData(Optional.of(Map.of("userId", user.getId(), "bookingId", booking.getId())));

            } finally {
                lock.unlock();
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error booking class: {}", ExceptionUtils.getStackTrace(e));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to book the class");
        }

        return responseFormat;
    }

    private void checkForOverlappingBookings(User user, ClassSchedule newSchedule) {
        List<Booking> userBookings = bookingRepository.findByUserAndStatus(user, "booked");

        List<WaitingList> userWaitlists = waitingListRepository.findByUser(user);

        List<ClassSchedule> scheduledClasses = new ArrayList<>();
        userBookings.forEach(booking -> scheduledClasses.add(booking.getSchedule()));
        userWaitlists.forEach(waitlist -> scheduledClasses.add(waitlist.getSchedule()));

        for (ClassSchedule existingSchedule : scheduledClasses) {
            if (isOverlapping(existingSchedule, newSchedule)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This class overlaps with your existing booking: " + existingSchedule.getTitle());
            }
        }
    }

    private boolean isOverlapping(ClassSchedule schedule1, ClassSchedule schedule2) {
        ZonedDateTime start1 = schedule1.getStartTime();
        ZonedDateTime end1 = schedule1.getEndTime();
        ZonedDateTime start2 = schedule2.getStartTime();
        ZonedDateTime end2 = schedule2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }


    @Override
    public ResponseFormat cancelBooking(BookingCancelRequest request, String username) {
        ResponseFormat responseFormat = null;
        try {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

            if (!booking.getUser().getEmail().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unauthorized user to cancel this booking");
            }

            if ("cancelled".equals(booking.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already cancelled");
            }

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime classStart = booking.getSchedule().getStartTime();

            if (now.isAfter(classStart)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class already started. Cannot cancel.");
            }

            booking.setStatus("cancelled");
            booking.setUpdatedOn(now);
            bookingRepository.save(booking);

            Duration timeUntilClass = Duration.between(now, classStart);

            if (timeUntilClass.toHours() >= 4) {
                UserPackage pkg = booking.getUserPackage();
                pkg.setRemainingCredits(pkg.getRemainingCredits() + booking.getSchedule().getRequiredCredits());
                pkg.setUpdatedOn(now);
                userPackageRepository.save(pkg);
            }


            promoteFromWaitlist(booking.getSchedule());

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Cancel booked successfully"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while cancelling booking: {}", ExceptionUtils.getStackTrace(e));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel booking");
        }
        return responseFormat;
    }


    public void promoteFromWaitlist(ClassSchedule schedule) {
        Optional<WaitingList> next = waitingListRepository.findFirstByScheduleOrderByCreatedOnAsc(schedule);
        if (next.isPresent()) {
            WaitingList waitlist = next.get();
            Booking booking = Booking.builder()
                    .user(waitlist.getUser())
                    .userPackage(waitlist.getUserPackage())
                    .schedule(schedule)
                    .status("booked")
                    .createdOn(ZonedDateTime.now())
                    .updatedOn(ZonedDateTime.now())
                    .build();

            bookingRepository.save(booking);
            waitingListRepository.delete(waitlist);
        }
    }


}
