package com.gal.afiliaciones.infrastructure.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageDescriptor {
    private final BulkMsg key;
    private final Object[] args;

    public static MessageDescriptor of(BulkMsg key, Object... args) {
        return new MessageDescriptor(key, args);
    }
}


