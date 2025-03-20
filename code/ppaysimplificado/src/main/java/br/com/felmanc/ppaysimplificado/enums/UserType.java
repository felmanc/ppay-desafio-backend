package br.com.felmanc.ppaysimplificado.enums;

import br.com.felmanc.ppaysimplificado.exceptions.InvalidUserTypeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum UserType {
    COMMON,
    MERCHANT;

	public static UserType fromString(String value) {
	    try {
	        log.info("Convertendo {} para upper case.", value);
	        return UserType.valueOf(value.toUpperCase());
	    } catch (IllegalArgumentException e) {
	        throw new InvalidUserTypeException("Invalid UserType: " + value);
	    }
	}
}