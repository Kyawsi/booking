package com.booking.system.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.context.ApplicationContext;

/**
 * Allows Quartz jobs to have Spring-managed beans autowired.
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    private AutowireCapableBeanFactory beanFactory;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object jobInstance = super.createJobInstance(bundle);
        beanFactory.autowireBean(jobInstance); // This injects @Autowired dependencies
        return jobInstance;
    }
}
