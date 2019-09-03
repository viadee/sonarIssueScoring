package de.viadee.sonarissuescoring.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

public abstract class ToStringMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    private final Class<?> supportedClass;

    public ToStringMessageConverter(Class<?> supportedClass) {
        super(StandardCharsets.UTF_8, MediaType.TEXT_PLAIN);
        this.supportedClass = supportedClass;
    }

    @Override public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) { return false; }

    @Override protected boolean supports(Class<?> clazz) { return supportedClass.isAssignableFrom(clazz); }

    @Override protected T readInternal(Class<? extends T> aClass, HttpInputMessage httpInputMessage) throws HttpMessageNotReadableException {
        throw new UnsupportedOperationException();
    }

    @Override protected void writeInternal(T t, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        StreamUtils.copy(write(t), StandardCharsets.UTF_8, httpOutputMessage.getBody());
    }

    protected abstract String write(T src);
}
