package com.net.comm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Default application error controller.
 * Converts error description into a json.c
 */
@RestController
@RequestMapping("/error")
public class AppErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    @Autowired
    public AppErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public Map<String, Object> error(HttpServletRequest aRequest) {
        return getErrorAttributes(aRequest);
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest) {
        WebRequest requestAttributes = new ServletWebRequest(aRequest);
        return errorAttributes.getErrorAttributes(requestAttributes, false);
    }
}
