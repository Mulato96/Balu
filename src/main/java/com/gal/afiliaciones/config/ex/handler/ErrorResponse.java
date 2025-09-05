package com.gal.afiliaciones.config.ex.handler;

import com.gal.afiliaciones.config.ex.Error;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private Error error;

    public ErrorResponse(final Error.Type type, final String message, final List<String> detail) {
        this.error = new Error(type, message, detail);
    }

    public ErrorResponse(final Error.Type type, final String message) {
        this.error = new Error(type, message, null);
    }

    public ErrorResponse(final Error.Type type) {
        this.error = new Error(type, null, null);
    }
}