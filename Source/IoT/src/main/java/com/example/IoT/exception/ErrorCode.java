package com.example.IoT.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    EMAIL_EXISTED(1008, "Email existed, please choose another one", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1009, "Username existed, please choose another one", HttpStatus.BAD_REQUEST),
    USERNAME_IS_MISSING(1010, "Please enter username", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1011, "User not existed", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1012, "Incorrect password", HttpStatus.BAD_REQUEST),
    PHARMACY_NOT_FOUND(1013, "Pharmacy not found", HttpStatus.BAD_REQUEST),
    UNIT_NOT_FOUND(1014, "Unit not found", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1015, "Order not found", HttpStatus.BAD_REQUEST),
    DEVICE_NOT_FOUND(1016, "Device not found", HttpStatus.BAD_REQUEST),
    FIRMWARE_VERSION_EXISTED(1017, "Firmware version already exists", HttpStatus.BAD_REQUEST),
    FIRMWARE_VERSION_NOT_FOUND(1018, "Firmware version not found", HttpStatus.BAD_REQUEST),
    FIRMWARE_UPDATE_IN_PROGRESS(1019, "Firmware update already in progress", HttpStatus.BAD_REQUEST),
    FIRMWARE_FILE_EMPTY(1020, "Firmware file is empty", HttpStatus.BAD_REQUEST),
    FIRMWARE_FILE_INVALID(1021, "Invalid firmware file format. Only .bin files are allowed", HttpStatus.BAD_REQUEST),
    FIRMWARE_URL_REQUIRED(1022, "Firmware download URL is required", HttpStatus.BAD_REQUEST),
    FIRMWARE_UPDATE_NOT_FOUND(1023, "Firmware update not found or already completed", HttpStatus.BAD_REQUEST),
    FIRMWARE_UPDATE_NOT_CANCELLABLE(1024, "Cannot cancel firmware update in current status", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;
}
