package com.adape.gtk.front.beans;

import org.springframework.web.multipart.MultipartFile;

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
	
	private MultipartFile profileImage;
	
	private String password;

}

