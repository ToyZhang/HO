package com.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
public class TCSL_RESULT_Errors {
    private TCSL_RESULT_Error error;

    public TCSL_RESULT_Errors() {
    }

    public TCSL_RESULT_Errors(TCSL_RESULT_Error error) {
        this.error = error;
    }
    @XmlElement(name = "Error")
    public TCSL_RESULT_Error getError() {
        return error;
    }

    public void setError(TCSL_RESULT_Error error) {
        this.error = error;
    }
}
