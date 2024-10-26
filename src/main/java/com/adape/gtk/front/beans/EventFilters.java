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
public class EventFilters {
	
	private int page;
	
	private String body;
	
	private int province;
	
	private String city;
		
	private String eventDateFrom;
	
	private String eventDateTo;
	
	private List<Integer> categories;
	
	private List<Integer> tags;
	
	private BigDecimal priceFrom;
	
	private BigDecimal priceTo;
	
	private int participantsFrom;
	
	private int participantsTo;

}

