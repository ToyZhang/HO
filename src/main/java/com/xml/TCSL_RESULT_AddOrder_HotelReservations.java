package com.xml;

import org.springframework.stereotype.Repository;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
public class TCSL_RESULT_AddOrder_HotelReservations {
    private TCSL_RESULT_AddOrder_HotelReservation hotelReservation;

    public TCSL_RESULT_AddOrder_HotelReservations() {
    }

    public TCSL_RESULT_AddOrder_HotelReservations(TCSL_RESULT_AddOrder_HotelReservation hotelReservation) {
        this.hotelReservation = hotelReservation;
    }
    @XmlElement(name = "HotelReservation")
    public TCSL_RESULT_AddOrder_HotelReservation getHotelReservation() {
        return hotelReservation;
    }

    public void setHotelReservation(TCSL_RESULT_AddOrder_HotelReservation hotelReservation) {
        this.hotelReservation = hotelReservation;
    }
}
