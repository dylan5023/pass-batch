package com.dylan.pass.repository.user;

import com.dylan.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user")
// Define json type
public class UserEntity extends BaseEntity {
    @Id
    private String userId;

    private String userName;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String phone;


    @Type(type = "json")
    private Map<String, Object> meta;

    public String getUuid() {
        String uuid = null;
        if (meta.containsKey("uuid")) {
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;

    }

}
