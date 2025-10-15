package com.gal.afiliaciones.config;

import com.gal.afiliaciones.infrastructure.quartz.RetirementJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.CronScheduleBuilder.cronSchedule;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail retirementJobDetail() {
        return JobBuilder.newJob(RetirementJob.class)
                .withIdentity("retirementJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger retirementJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(retirementJobDetail())
                .withIdentity("retirementTrigger")
                .withSchedule(cronSchedule("0 0 0 * * ?")) // Fire at midnight every day
                .build();
    }
}