package net.nordu.mdx.web;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@RequestMapping("/")
public class WelcomeController {

	@RequestMapping("/")
	public String index() {
		return "redirect:/md/all";
	}
	
}
