package com.oexil.staffid.config;

import com.oexil.staffid.dto.user.UserForResponsePrivet;
import com.oexil.staffid.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CurrentUser currentUser;
    private final ModelMapper modelMapper;

    @ModelAttribute("loggedInUser")
    public UserForResponsePrivet addLoggedInUserToModel() {
        if (currentUser.getUserForGlobalControllerAdvice() == null)
            return null;

        return modelMapper.map(currentUser.getUserForGlobalControllerAdvice(), UserForResponsePrivet.class);
    }

}
