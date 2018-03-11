package cz.melkamar.andruian.indexer.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;

public class Util {
    public static void addPrincipalAttribute(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) principal = null;
        model.addAttribute("principal", principal);
    }
}
