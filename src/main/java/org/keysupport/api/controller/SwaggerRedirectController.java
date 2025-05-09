package org.keysupport.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class SwaggerRedirectController {

	@Operation(hidden = true)
	@RequestMapping("/")
    public RedirectView handleRoot() {
        return new RedirectView("/swagger-ui/index.html");
    }

}
