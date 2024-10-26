package com.adape.gtk.front.beans;


import java.math.BigDecimal;
import java.util.List;
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
public class EventData {
	
	private Integer eventId;
	
	private String title;
	
	private String imagePath;
	
	private String eventDate;
	
	private int maxParticipants;
	
	private String description;
	
	private String address;
	
	private int province;
	
	private String city;
	
	@Builder.Default
	private BigDecimal price = new BigDecimal(0);
	
	private int category;
	
	private List<Integer> tags;

}

