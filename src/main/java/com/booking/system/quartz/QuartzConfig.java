package com.booking.system.quartz;

import com.booking.system.config.AutowiringSpringBeanJobFactory;
import com.booking.system.quartz.RefundCreditsJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.*;

import javax.sql.DataSource;
import java.util.Calendar;
import java.util.TimeZone;

import static java.util.Calendar.MILLISECOND;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "schedule.cron.enable_scheduler", havingValue = "true")
public class QuartzConfig {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;

    public QuartzConfig(ApplicationContext applicationContext, DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger refundCreditsTrigger) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactory.setTriggers(refundCreditsTrigger);
        return schedulerFactory;
    }

    @Bean
    public JobDetailFactoryBean refundCreditsJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(RefundCreditsJob.class);
        factoryBean.setDurability(true);
        factoryBean.setName("refundCreditsJob");
        return factoryBean;
    }

    @Bean
    public CronTriggerFactoryBean refundCreditsTrigger(JobDetail refundCreditsJobDetail) {
        return createCronTrigger(refundCreditsJobDetail, "0 0 0 * * ?", "refundCreditsTrigger"); // Run daily at midnight
    }

    public static CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cronExpression, String triggerName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getDefault());

        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setStartTime(calendar.getTime());
        factoryBean.setStartDelay(0L);
        factoryBean.setName(triggerName);
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY);

        return factoryBean;
    }
}
