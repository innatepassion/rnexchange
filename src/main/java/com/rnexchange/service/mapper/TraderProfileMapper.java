package com.rnexchange.service.mapper;

import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.User;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TraderProfile} and its DTO {@link TraderProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface TraderProfileMapper extends EntityMapper<TraderProfileDTO, TraderProfile> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    TraderProfileDTO toDto(TraderProfile s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
