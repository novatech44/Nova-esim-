package com.e_sim.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.e_sim.dao.entity.PhoneNumber;
import com.e_sim.dao.entity.User;
import com.e_sim.dao.repository.PhoneNumberRepository;
import com.e_sim.dao.repository.UserRepository;
import com.e_sim.dto.request.PhoneNumberRequest;
import com.e_sim.dto.response.ApiRes;
import com.e_sim.dto.response.PhoneNumberDto;
import com.e_sim.exception.BusinessRuleException;
import com.e_sim.exception.DuplicateResourceException;
import com.e_sim.util.UserValidator;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPhoneService {

    private final PhoneNumberRepository phoneNumberRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Transactional
    public ApiRes addPhoneNumber(Long userId, PhoneNumberRequest request) {
        User user = userValidator.validateUser(userId);

        validatePhoneNumberUniqueness(user, request.number());

        validatePhoneNumberLimit(user);

        PhoneNumber phoneNumber = new PhoneNumber(request.number());
        user.addPhoneNumber(phoneNumber);
        log.info("Adding phone number  for userId={}, data={}", userId, request.number());

        userRepository.save(user);
        log.info("Phone number added successfully for userId={}", userId);

        return ApiRes.success(HttpStatus.OK);
    }

    @Transactional
    public ApiRes removePhoneNumber(Long userId, Long phoneNumberId) {
        User user = userValidator.validateUser(userId);

        PhoneNumber phoneNumber = phoneNumberRepository.findByIdAndUser(phoneNumberId, user)
                .orElseThrow(() -> {
                    log.warn("Phone number not found or doesn't belong to user. userId={}, phoneNumberId={}",
                            userId, phoneNumberId);
                    return new EntityNotFoundException(
                            "Phone number not found or doesn't belong to user"
                    );
                });

        user.removePhoneNumber(phoneNumber);
        log.info("Removing phone number {} for userId={}", phoneNumberId, userId);

        phoneNumberRepository.delete(phoneNumber);
        log.info("Successfully removed phone number {} for userId={}", phoneNumberId, userId);

        return ApiRes.success(HttpStatus.OK);
    }


    public ApiRes<Set<PhoneNumberDto>> getUserPhoneNumbers(Long userId) {
        User user = userValidator.validateUser(userId);

        Set<PhoneNumberDto> phoneNumbers = user.getPhoneNumbers().stream()
                .map(PhoneNumberDto::new)
                .collect(Collectors.toUnmodifiableSet());
        log.debug("Retrieved {} phone numbers for userId={}", phoneNumbers.size(), userId);

        return ApiRes.success(phoneNumbers, HttpStatus.OK);
    }


    private void validatePhoneNumberUniqueness(User user, String number) {
        if (phoneNumberRepository.existsByNumberAndUser(number, user)) {
            log.warn("Duplicate phone number attempt for user ID: {}, number: {}", user.getId(), number);
            throw new DuplicateResourceException(
                    String.format("Phone number %s already exists for this user", number)
            );
        }
    }

    private void validatePhoneNumberLimit(User user) {
        final int currentCount = user.getPhoneNumbers().size();
        final int maxAllowed = 3;

        if (currentCount >= maxAllowed) {
            log.warn("Phone number limit reached for user ID: {} ({} of {})",
                    user.getId(), currentCount, maxAllowed);
            throw new BusinessRuleException(
                    String.format("Maximum of %d phone numbers allowed per user", maxAllowed)
            );
        }
    }
}