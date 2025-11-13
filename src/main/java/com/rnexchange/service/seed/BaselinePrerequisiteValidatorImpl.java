package com.rnexchange.service.seed;

import com.rnexchange.domain.Authority;
import com.rnexchange.domain.User;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import com.rnexchange.service.seed.dto.BaselineSeedValidationReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BaselinePrerequisiteValidatorImpl implements BaselinePrerequisiteValidator {

    private static final Logger log = LoggerFactory.getLogger(BaselinePrerequisiteValidatorImpl.class);

    private static final Map<String, String> REQUIRED_USERS = Map.of(
        "exchange-operator",
        AuthoritiesConstants.EXCHANGE_OPERATOR,
        "broker-admin",
        AuthoritiesConstants.BROKER_ADMIN,
        "trader-one",
        AuthoritiesConstants.TRADER,
        "trader-two",
        AuthoritiesConstants.TRADER
    );

    private final UserRepository userRepository;

    public BaselinePrerequisiteValidatorImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public BaselineSeedValidationReport validate(BaselineSeedRequest request) {
        List<String> errors = new ArrayList<>();

        REQUIRED_USERS.forEach((login, requiredAuthority) -> {
            Optional<User> maybeUser = userRepository.findOneWithAuthoritiesByLogin(login);
            if (maybeUser.isEmpty()) {
                errors.add("Missing required user '" + login + "' with authority " + requiredAuthority);
                return;
            }
            User user = maybeUser.get();
            Set<String> authorityNames = user
                .getAuthorities()
                .stream()
                .map(Authority::getName)
                .collect(java.util.stream.Collectors.toSet());
            if (!authorityNames.contains(requiredAuthority)) {
                errors.add("User '" + login + "' is missing authority " + requiredAuthority);
            }
            if (!user.isActivated()) {
                errors.add("User '" + login + "' is not activated");
            }
        });

        BaselineSeedValidationReport report = BaselineSeedValidationReport.builder().successful(errors.isEmpty()).errors(errors).build();
        if (!report.isSuccessful()) {
            log.warn("Baseline seed prerequisite validation failed with errors {}", errors);
        }
        return report;
    }
}
