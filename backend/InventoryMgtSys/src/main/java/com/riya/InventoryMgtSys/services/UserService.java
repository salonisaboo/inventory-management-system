package com.project.InventoryMgtSys.services;

import com.project.InventoryMgtSys.dtos.LoginRequest;
import com.project.InventoryMgtSys.dtos.RegisterRequest;
import com.project.InventoryMgtSys.dtos.Response;
import com.project.InventoryMgtSys.dtos.UserDTO;
import com.project.InventoryMgtSys.models.User;

public interface UserService {
    Response registerUser(RegisterRequest registerRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    User getCurrentLoggedInUser();

    Response getUserById(Long id);

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    Response getUserTransactions(Long id);
}
