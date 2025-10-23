package com.codeit.monew.user.controller;

import com.codeit.monew.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor // final 필드(userService)에 대한 생성자 주입
@RequestMapping("/api/users")
public class UserController {


}
