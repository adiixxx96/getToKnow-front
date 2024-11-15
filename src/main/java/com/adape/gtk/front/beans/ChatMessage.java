package com.adape.gtk.front.beans;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
	
	private String id;
	
	private Integer chatId;
	
	private Integer userId;
	
	private String message;
	
	private Date creationDate;
	
	private Boolean read;

}

