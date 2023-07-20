package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Slf4j
@RequiredArgsConstructor
public class UserControllerAdmin {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(name = "ids") List<Long> ids,
                                     @RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /admin/users/ids");
        return null;
    }

    @PostMapping
    public UserDto saveUser(@RequestBody UserShortDto userDto) {
        log.info("POST /admin/users");
        return null;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
    }
}
