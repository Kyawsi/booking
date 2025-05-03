package com.booking.system.service.impl;

import com.booking.system.entity.model.*;
import com.booking.system.entity.request.BookingCancelRequest;
import com.booking.system.entity.request.BookingRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.*;
import com.booking.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

            // Fetch the UserPackage based on the userPackageId
            UserPackage userPackage = userPackageRepository.findById(request.getUserPackageId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Package not found"));

            if (!userPackage.getUser().equals(user)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not own this package.");
            }

            ClassSchedule classSchedule = classScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));

            // Required and remaining credits validation
            Integer requiredCredits = classSchedule.getRequiredCredits();
            Integer remainingCredits = userPackage.getRemainingCredits();

            // Locking logic using Redis to avoid overbooking
            String lockKey = "booking_lock_" + classSchedule.getId();
            RLock lock = redissonClient.getLock(lockKey);

            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many booking attempts, please try again later.");
            }

            try {
                // Check if the user has already booked this class with the selected package
                boolean alreadyBooked = bookingRepository.existsByUserAndScheduleAndUserPackage(user, classSchedule, userPackage);
                if (alreadyBooked) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this class with the selected package.");
                }

                // Check if the user is already on the waitlist
                boolean alreadyOnWaitlist = waitingListRepository.existsByUserAndScheduleAndUserPackage(user, classSchedule, userPackage);
                if (alreadyOnWaitlist) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already on the waitlist for this class.");
                }

                // **Check if the class is full**
                int bookedCount = bookingRepository.countByScheduleAndStatus(classSchedule, "booked");
                if (bookedCount >= classSchedule.getSlotCount()) {
                    // If class is full, add to waitlist
                    if (remainingCredits == null || remainingCredits < requiredCredits) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough credits to be added to the waitlist");
                    }

                    // Deduct credits even for waitlist (user holds spot in case of cancellation)
                    userPackage.setRemainingCredits(remainingCredits - requiredCredits);
                    userPackage.setUpdatedOn(ZonedDateTime.now());
                    userPackageRepository.save(userPackage);

                    // Add to waitlist
                    WaitingList waitlist = WaitingList.builder()
                            .user(user)
                            .userPackage(userPackage)
                            .schedule(classSchedule)
                            .createdOn(ZonedDateTime.now())
                            .updatedOn(ZonedDateTime.now())
                            .build();
                    waitingListRepository.save(waitlist);

                    // Success response: Added to waitlist
                    responseFormat = new ResponseFormat();
                    responseFormat.setSuccess(true);
                    responseFormat.setMessage(Optional.of("Class full. You have been added to the waitlist."));
                    return responseFormat;  // Exit early, do not proceed with further booking logic
                }

                // **Proceed with booking if the class is not full**

                if (requiredCredits == null || requiredCredits <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid required credit configuration for class");
                }

                if (remainingCredits == null || remainingCredits < requiredCredits) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough credits to book this class");
                }

                // Deduct credits from user's package
                userPackage.setRemainingCredits(remainingCredits - requiredCredits);
                userPackage.setUpdatedOn(ZonedDateTime.now());
                userPackageRepository.save(userPackage);

                // Create the booking record
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


    @Override
    public ResponseFormat cancelBooking(BookingCancelRequest request, String username) {
        ResponseFormat responseFormat = null;
        try {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

            if (!booking.getUser().getEmail().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unauthorized user to cancel this booking");
            }

            ZonedDateTime classStart = booking.getSchedule().getStartTime();
            ZonedDateTime now = ZonedDateTime.now();
            long hoursDiff = ChronoUnit.HOURS.between(now, classStart);

            if (hoursDiff < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot cancel the class within 4 hours of its start time");
            }

            UserPackage pkg = booking.getUserPackage();
            pkg.setRemainingCredits(pkg.getRemainingCredits() + booking.getSchedule().getRequiredCredits());
            pkg.setUpdatedOn(now);
            userPackageRepository.save(pkg);

            booking.setStatus("cancelled");
            booking.setUpdatedOn(now);
            bookingRepository.save(booking);

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
