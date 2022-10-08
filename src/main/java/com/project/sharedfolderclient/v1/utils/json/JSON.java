package com.project.sharedfolderclient.v1.utils.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class JSON {
    public final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


}
