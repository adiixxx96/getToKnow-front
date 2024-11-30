package com.adape.gtk.front.utils;

import java.sql.Timestamp;
import java.util.Calendar;

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
	
	public static int calculateAge(Timestamp birthDate) {
        if (birthDate == null) {
            return 0;
        }

        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTimeInMillis(birthDate.getTime());
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}