package com.xml;

import org.springframework.stereotype.Repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangtuoyu on 2017/6/22.
 */
public class TCSL_PARAM_HotelReservationIDs {
    private List<TCSL_PARAM_HotelReservationID> hotelReservationID = new ArrayList<TCSL_PARAM_HotelReservationID>();

    public TCSL_PARAM_HotelReservationIDs() {
    }

    public TCSL_PARAM_HotelReservationIDs(List<TCSL_PARAM_HotelReservationID> hotelReservationID) {
        this.hotelReservationID = hotelReservationID;
    }

    @XmlElement(name = "HotelReservationID")
    public List<TCSL_PARAM_HotelReservationID> getHotelReservationID() {
        return hotelReservationID;
    }

    public void setHotelReservationID(List<TCSL_PARAM_HotelReservationID> hotelReservationID) {
        this.hotelReservationID = hotelReservationID;
    }
}
