package com.ne.template.users;


import com.ne.template.auth.dtos.RegisterRequestDto;
import com.ne.template.commons.exceptions.BadRequestException;
import com.ne.template.users.dtos.UserResponseDto;
import com.ne.template.users.mappers.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(RegisterRequestDto user) {
        if(userRepository.existsByEmailOrPhoneNumberOrNationalId(user.email(), user.phoneNumber(), user.nationalId()))
            throw new BadRequestException("User with this email or nationalId or  phone number already exists.");

        var newUser = userMapper.toEntity(user);
        newUser.setPassword(passwordEncoder.encode(user.password()));
        newUser.setRole(Role.CUSTOMER);
        newUser.setEnabled(false);
        log.info("user is here, {}", newUser);
        userRepository.save(newUser);
        return userMapper.toResponseDto(newUser);
    }

    public void changeUserPassword(String userEmail, String newPassword){
        var user = findByEmail(userEmail);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void activateUserAccount(String userEmail){
        var user = findByEmail(userEmail);
        user.setEnabled(true);
        userRepository.save(user);
    }


    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User with that email not found."));
    }
}
