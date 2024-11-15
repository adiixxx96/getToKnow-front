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
public class UserData {
	
	private Integer userId;
	
	private String fullname;
	
	private String profileImage;
	
	private String password;

}

