package com.adape.gtk.front.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component("Utils")
public class Utils {

	private static MessageSource messages;
	
	@Autowired
	public void setMessage(MessageSource msg) {
		Utils.messages = msg;
	}

	public static String message(String id, String... vars) {
		return messages.getMessage(id, vars, LocaleContextHolder.getLocale());
	}
}