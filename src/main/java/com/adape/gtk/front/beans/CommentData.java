package com.adape.gtk.front.beans;


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
public class CommentData {
	
	private String comment;
	
	private String reply;
	
	private int eventId;
	
	private Integer parentId;

}

