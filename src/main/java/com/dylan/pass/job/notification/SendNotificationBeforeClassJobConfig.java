package com.dylan.pass.job.notification;

import com.dylan.pass.repository.booking.BookingEntity;
import com.dylan.pass.repository.booking.BookingStatus;
import com.dylan.pass.repository.notification.NotificationEntity;
import com.dylan.pass.repository.notification.NotificationEvent;
import com.dylan.pass.repository.notification.NotificationModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class SendNotificationBeforeClassJobConfig {
    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;

    public SendNotificationBeforeClassJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, SendNotificationItemWriter sendNotificationItemWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.sendNotificationItemWriter = sendNotificationItemWriter;
    }

    @Bean
    public Job sendNotificationBeforeClassJob() {
        return this.jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();
    }

    @Bean
    public Step addNotificationStep() {
        return this.stepBuilderFactory.get("addNotificationStep")
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();

    }


    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                // pageSize: Number of rows to view at once
                .pageSize(CHUNK_SIZE)
                // Reservations whose status is preparing and whose start date and time (startedAt) start in 10 minutes are subject to alarm.
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();
    }

    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
        return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /**
     * Readers are synchronized and run sequentially, but writers operate as multi-threads.
     */
    @Bean
    public Step sendNotificationStep() {
        return this.stepBuilderFactory.get("sendNotificationStep")
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    /**
     * SynchronizedItemStreamReader: In a multi-thread environment, readers and writers must be thread-safe.
     * The Cursor technique's ItemReader is not thread-safe, so you have to use the Paging technique or declare it synchronized to perform it sequentially.
     */
    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)

                // // Alarms whose event is before class and whose sending status (sent) is not yet sent are subject to inquiry.
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();

    }

}
