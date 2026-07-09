package com.oexil.staffid.repository;

import com.oexil.staffid.enums.ERole;
import com.oexil.staffid.enums.UserType;
import com.oexil.staffid.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndActiveIsTrue(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String username);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByUsernameAndIdNot(String username, Long currentUserId);

    Boolean existsByEmailAndIdNot(String username, Long currentUserId);

    Boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long currentUserId);

    Page<User> findAllByRolesName(ERole role, PageRequest pageRequest);

    Page<User> findAllByUserType(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndActiveIsTrue(UserType userType, PageRequest pageRequest);

    List<User> findAllByUserTypeAndActiveIsTrue(UserType userType);

    Page<User> findAllByUserTypeAndActiveIsFalse(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndApproveIsNull(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndApproveIsTrue(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndApproveIsFalse(UserType userType, PageRequest pageRequest);

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.approve = true AND (u.active = true OR u.active = false)")
    Page<User> findAllByUserTypeAndApproveIsTrueAndActiveIsTrueOrActiveIsFalse(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndApproveIsNullOrApproveIsFalse(UserType userType, PageRequest pageRequest);

    Page<User> findAllByUserTypeAndApproveIsNullOrApproveIsTrue(UserType userType, PageRequest pageRequest);

    Optional<User> findByIdAndUserTypeAndActiveIsTrue(Long id, UserType type);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String firstName, String lastName, String email);
}
