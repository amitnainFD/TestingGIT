package com.freshdirect.smartstore.dsl;

public class CompileException extends Exception {

    public final static int UNKNOWN_VARIABLE = 1;
    public final static int SYNTAX_ERROR = 2;
    public final static int TYPE_ERROR = 3;
    public final static int UNKNOWN_FUNCTION = 4;
    public final static int PARAMETER_ERROR = 5;
    public final static int UNKNOWN_OPERATION = 6;
    
    int code;
    
    public CompileException() {
    }

    public CompileException(String message) {
        super(message);
    }
    public CompileException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CompileException(Throwable cause) {
        super(cause);
    }

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompileException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
