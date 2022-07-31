package com.valvoline.integration.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Step {
	
	@JacksonXmlProperty(isAttribute = true, localName="id")
	public String id;
	@JacksonXmlProperty(isAttribute = true, localName="type")
	public String type;
	@JacksonXmlElementWrapper(localName ="parameterizedString", useWrapping = false)
	public List<String> parameterizedString;
	@JacksonXmlProperty(localName="description")
	public String description;

}
