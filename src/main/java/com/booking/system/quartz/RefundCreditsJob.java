package com.booking.system.quartz;

import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.UserPackage;
import com.booking.system.entity.model.WaitingList;
import com.booking.system.repository.ClassScheduleRepository;
import com.booking.system.repository.UserPackageRepository;
import com.booking.system.repository.WaitingListRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class RefundCreditsJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RefundCreditsJob.class);

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Override
    @Transactional  // Ensure atomicity, rollback if any issue occurs
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // Get all class schedules that have ended
            List<ClassSchedule> classSchedules = classScheduleRepository.findByEndTimeBefore(ZonedDateTime.now());

            for (ClassSchedule classSchedule : classSchedules) {
                // Refund credits for users in the waitlist who were not booked
                List<WaitingList> waitlistUsers = waitingListRepository.findBySchedule(classSchedule);
                if (waitlistUsers.isEmpty()) {
                    logger.info("No waitlist users for class schedule ID: {}", classSchedule.getId());
                }

                for (WaitingList waitlist : waitlistUsers) {
                    UserPackage userPackage = waitlist.getUserPackage();
                    int requiredCredits = classSchedule.getRequiredCredits();

                    // Refund credits
                    userPackage.setRemainingCredits(userPackage.getRemainingCredits() + requiredCredits);
                    userPackage.setUpdatedOn(ZonedDateTime.now());
                    userPackageRepository.save(userPackage);

                    waitingListRepository.delete(waitlist);
                }

                logger.info("Refunded credits and removed users from waitlist for schedule ID: {}", classSchedule.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();  // Log the error
            throw new JobExecutionException("Error processing refund credits job", e);
        }
    }
}
