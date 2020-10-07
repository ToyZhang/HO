package com.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
@XmlType(propOrder = {"code","value"})
public class TCSL_RESULT_Error {
    private String code;
    private String value;

    public TCSL_RESULT_Error() {
    }

    public TCSL_RESULT_Error(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @XmlAttribute(name = "Code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    @XmlValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
