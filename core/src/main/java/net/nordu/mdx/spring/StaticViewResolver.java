package net.nordu.mdx.spring;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class StaticViewResolver implements ViewResolver, Ordered {

	private View view;
	private int order;
	
	public StaticViewResolver(View view) {
		this.view = view;
	}
	
	public View resolveViewName(String vn, Locale loc) throws Exception {
		return view;
	}

	public View getView() {
		return view;
	}
	
	public void setView(View view) {
		this.view = view;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return order;
	}

}
