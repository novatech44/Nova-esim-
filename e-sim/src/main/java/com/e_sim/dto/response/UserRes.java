package com.e_sim.dto.response;

import com.e_sim.dao.entity.PhoneNumber;
import com.e_sim.dao.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for {@link User}
 */
@Data
public class UserRes implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedOn;

    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private Set<PhoneNumberDto> phoneNumbers;
    private boolean active;


    public UserRes(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.createdOn = user.getCreatedOn();
        this.updatedOn = user.getUpdatedOn();
        this.active = user.isActive();
        this.phoneNumbers = mapPhoneNumbers(user.getPhoneNumbers());
    }

    private static Set<PhoneNumberDto> mapPhoneNumbers(Set<PhoneNumber> phoneNumbers) {
        if (CollectionUtils.isEmpty(phoneNumbers)) {
            return Collections.emptySet();
        }
        return phoneNumbers.stream()
                .map(PhoneNumberDto::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}