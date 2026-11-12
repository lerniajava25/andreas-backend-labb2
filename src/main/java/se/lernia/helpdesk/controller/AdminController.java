
package se.lernia.helpdesk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.lernia.helpdesk.entity.Role;

import se.lernia.helpdesk.service.UserService;

@Controller
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String showAllUsers(
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();
        model.addAttribute("currentUsername", username);

        model.addAttribute(
                "users",
                userService.getAllUsers(username)
        );

        return "admin-users";
    }


    @GetMapping("/admin/users/new")
    public String showCreateUserForm(Model model) {

        model.addAttribute("roles", Role.values());

        return "admin-user-form";
    }



    @PostMapping("/admin/users")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam Role role,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.createUser(username, password, role);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Användaren har skapats!"
            );

            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "enteredUsername",
                    username
            );

            return "redirect:/admin/users/new";
        }
    }



    @PostMapping("/admin/users/{id}/role")
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        userService.updateUserRole(
                id,
                role,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Användarens roll har uppdaterats!"
        );

        return "redirect:/admin/users";
    }


    @PostMapping("/admin/users/{id}/enabled")
    public String updateUserEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        userService.updateUserEnabled(
                id,
                enabled,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                enabled
                        ? "Användaren har aktiverats!"
                        : "Användaren har inaktiverats!"
        );

        return "redirect:/admin/users";
    }

}
