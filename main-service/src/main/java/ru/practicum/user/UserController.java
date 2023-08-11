package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserMapper;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto saveUser(@RequestBody @Valid NewUserRequestDto newUserRequestDto) {
        return UserMapper.toUserDto(userService.saveUser(newUserRequestDto));
    }

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
                                     @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return UserMapper.toUserDto(userService.getAllUsers(ids, from, size));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
