//package com.booking.system.jUnit;
//
//import com.booking.system.entity.request.BookingRequest;
//import com.booking.system.entity.response.ResponseFormat;
//import com.booking.system.repository.ClassScheduleRepository;
//import com.booking.system.repository.UserPackageRepository;
//import com.booking.system.repository.UserRepository;
//import com.booking.system.service.BookingService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class BookingConcurrencyTest {
//
//    @Autowired
//    private BookingService bookingService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserPackageRepository userPackageRepository;
//
//    @Autowired
//    private ClassScheduleRepository classScheduleRepository;
//
//    @Test
//    public void testConcurrentBookingWithRedisLock() throws InterruptedException {
//        // Prepare valid booking request
//        BookingRequest request = new BookingRequest();
//        request.setUserPackageId(1L);  // use real existing ID
//        request.setScheduleId(1L);     // use real existing ID
//
//        String username = "testuser@example.com"; // must exist in DB
//
//        // Simulate 2 concurrent booking attempts
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//
//        Runnable bookingTask = () -> {
//            try {
//                ResponseFormat response = bookingService.booking(request, username);
//                System.out.println("Booking Result: " + response.getMessage().orElse("No message"));
//            } catch (Exception e) {
//                System.err.println("Booking failed: " + e.getMessage());
//            }
//        };
//
//        executorService.submit(bookingTask);
//        executorService.submit(bookingTask);
//
//        executorService.shutdown();
//        executorService.awaitTermination(15, TimeUnit.SECONDS);
//    }
//}