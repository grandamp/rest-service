package org.keysupport.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class SwaggerRedirectController {

	@RequestMapping("/")
    public RedirectView handleRoot() {
        return new RedirectView("https://api.keysupport.org/swagger-ui/index.html");
    }

}
