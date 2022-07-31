package com.valvoline.integration.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JacksonXmlRootElement(localName="steps")
@Component
public class GherkinSteps {
	
	@JacksonXmlProperty(isAttribute = true, localName="id")
	public String id;
	@JacksonXmlProperty(isAttribute = true, localName="last")
	public String last;
	@JacksonXmlElementWrapper(localName ="steps", useWrapping = false)
	public List<Step> step;

}
