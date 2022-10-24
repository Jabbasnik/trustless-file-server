package pl.jackowiak.trustlessfileserver.application;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple error controller.
 */
@RestController
class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping(value = "/error")
    String error() {
        return "No no no no my friend";
    }
}