package com.example.userservice.service;


import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.jpa.UsersEntity;
import com.example.userservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final Environment env;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
                true, true, true, true,
                new ArrayList<>());
    }


    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UsersEntity userEntity = mapper.map(userDto, UsersEntity.class);
        userEntity.setEncryptedPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UsersEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
//        List<ResponseOrder> orders = new ArrayList<>();

        // Using as rest template
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ResponseOrder>>() {
                });

        List<ResponseOrder> orderList = orderListResponse.getBody();
        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public Iterable<UsersEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UsersEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        return new ModelMapper().map(userEntity, UserDto.class);
    }
}
