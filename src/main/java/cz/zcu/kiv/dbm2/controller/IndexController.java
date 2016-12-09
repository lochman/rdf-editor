package cz.zcu.kiv.dbm2.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Created by Matej Lochman on 14.11.16.
 */

@Controller
@RequestMapping("/")
public class IndexController {

    private static final String ERROR_PATH = "error";

    @GetMapping
    public String viewIndex() {
        return "index";
    }

    @GetMapping("uploaded")
    public String viewUploaded(Model model) {
        return "uploaded";
    }

    @GetMapping("endsession")
    public String endSession(SessionStatus status) {
        status.setComplete();
        return "redirect:/";
    }
    /*
    @RequestMapping(value = ERROR_PATH)
    public String error() {
        return "error";
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
    */
}
