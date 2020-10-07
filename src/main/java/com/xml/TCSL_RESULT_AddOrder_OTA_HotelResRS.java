package com.xml;

import org.springframework.stereotype.Repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
@XmlType(name = "success",propOrder = {"timeStamp","rsResponseType","version","success","hotelReservations","errors"})
public class TCSL_RESULT_AddOrder_OTA_HotelResRS {
    private String timeStamp;
    private String rsResponseType;
    private String version;
    private String success;
    private TCSL_RESULT_AddOrder_HotelReservations hotelReservations;
    private TCSL_RESULT_Errors errors;

    public TCSL_RESULT_AddOrder_OTA_HotelResRS() {
    }

    public TCSL_RESULT_AddOrder_OTA_HotelResRS(String timeStamp, String rsResponseType, String version, String success, TCSL_RESULT_AddOrder_HotelReservations hotelReservations, TCSL_RESULT_Errors errors) {
        this.timeStamp = timeStamp;
        this.rsResponseType = rsResponseType;
        this.version = version;
        this.success = success;
        this.hotelReservations = hotelReservations;
        this.errors = errors;
    }

    @XmlAttribute(name = "TimeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }
    @XmlAttribute(name = "RsResponseType")
    public String getRsResponseType() {
        return rsResponseType;
    }
    @XmlAttribute(name = "Version")
    public String getVersion() {
        return version;
    }
    @XmlElement(name = "Success")
    public String getSuccess() {
        return success;
    }
    @XmlElement(name = "HotelReservations")
    public TCSL_RESULT_AddOrder_HotelReservations getHotelReservations() {
        return hotelReservations;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setRsResponseType(String rsResponseType) {
        this.rsResponseType = rsResponseType;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setHotelReservations(TCSL_RESULT_AddOrder_HotelReservations hotelReservations) {
        this.hotelReservations = hotelReservations;
    }
    @XmlElement(name = "Errors")
    public TCSL_RESULT_Errors getErrors() {
        return errors;
    }

    public void setErrors(TCSL_RESULT_Errors errors) {
        this.errors = errors;
    }
}
