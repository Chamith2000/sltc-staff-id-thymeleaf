package com.oexil.staffid.service;

import com.oexil.staffid.model.User;
import org.springframework.stereotype.Service;

@Service
public interface CurrentUser {
    User getUser();

    User getUserForGlobalControllerAdvice();
}
