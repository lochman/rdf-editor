package cz.zcu.kiv.dbm2.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Matej Lochman on 14.11.16.
 */

@Controller
@RequestMapping("/file")
public class FileController {

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    public String loadFile(@RequestParam(value = "file", required = true) MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        ByteArrayInputStream stream = null;
        try {
            stream = new ByteArrayInputStream(file.getBytes());
            String message = IOUtils.toString(stream, "UTF-8");
            redirectAttributes.addFlashAttribute("message", message);
            System.out.println(message);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/uploaded";
    }
}
