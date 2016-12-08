package cz.zcu.kiv.dbm2.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Matej Lochman on 14.11.16.
 */

@Controller
@RequestMapping("/")
public class IndexController implements ErrorController {

    private static final String ERROR_PATH = "error";

    @RequestMapping
    public String viewIndex() {
        return "index";
    }

    @RequestMapping(value = "uploaded", method = RequestMethod.GET)
    public String viewUploaded(Model model) {
        return "uploaded";
    }

    @RequestMapping(value = ERROR_PATH)
    public String error() {
        return "error";
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
