package com.dylan.pass.repository.notification;

import com.dylan.pass.repository.booking.BookingEntity;
import com.dylan.pass.util.LocalDateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

// ReportingPolicy.IGNORE: Ignore unmatched fields
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationModelMapper {
    NotificationModelMapper INSTANCE = Mappers.getMapper(NotificationModelMapper.class);

    // If the field names are not the same or if you want to map them in a custom way, you can add @Mapping.
    @Mapping(target = "uuid", source = "bookingEntity.userEntity.uuid")
    @Mapping(target = "text", source = "bookingEntity.startedAt", qualifiedByName = "text")
    NotificationEntity toNotificationEntity(BookingEntity bookingEntity, NotificationEvent event);

    // Create message to send alarm
    @Named("text")
    default String text(LocalDateTime startedAt) {
        return String.format("hello. %s class begins. Please check attendance before class. \uD83D\uDE0A", LocalDateTimeUtils.format(startedAt));

    }

}
