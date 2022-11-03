package org.keysupport.api.controller.oauth;

import org.keysupport.api.pojo.oauth.Cnf;
import org.keysupport.api.pojo.oauth.JwtPayload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtPayloadController {

	@GetMapping("/vss/v1/jwt")
	public JwtPayload jwtPayload() {
		JwtPayload payload = new JwtPayload();
		payload.iss = "https://api.keysupport.org/";
		payload.sub = "grandamp@gmail.com";
		Long exp = System.currentTimeMillis();
		payload.exp = exp;
		payload.nbf = exp;
		Cnf cnf = new Cnf();
		cnf.x5tS256 = "bwcK0esc3ACC3DB2Y5_lESsXE8o9ltc05O89jdN-dg2";
		payload.cnf = cnf;
		return payload;
	}
}
