package com.project.sharedfolderclient.v1.utils.http.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * Annotation to mark methods, so we can inject id to context
 */
public @interface ContextEnabled {
}
