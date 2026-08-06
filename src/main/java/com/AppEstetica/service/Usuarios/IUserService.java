package com.AppEstetica.service.Usuarios;

import com.AppEstetica.entities.User;

public interface IUserService {

    User findById(Long id);

    User findByUsername(String username);

    User createUser(User user);


}
