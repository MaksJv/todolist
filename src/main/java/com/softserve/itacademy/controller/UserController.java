package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }
    
    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.details.id")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.details.id")
    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "update-user";
    }

    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.details.id")
    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model,
                         @Validated @ModelAttribute("user") UpdateUserDto updateUserDto, BindingResult result) {
        UserDto oldUser = userService.findByIdThrowing(id);

        if (result.hasErrors()) {
            updateUserDto.setRole(oldUser.getRole()); // fallback to the current role
            model.addAttribute("roles", UserRole.values());
            return "update-user";
        }

        userService.update(updateUserDto);
        return "redirect:/users/" + id + "/read";
    }

    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.details.id")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getId() == id) {
            userService.delete(id);
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }
        userService.delete(id);
        return "redirect:/users/all";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "users-list";
    }
}
