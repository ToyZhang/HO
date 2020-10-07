package com.xml;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
@XmlType(propOrder = {"hotelReservationIDs","orderStatus"})
public class TCSL_RESULT_AddOrder_ResGlobalInfo {
    private TCSL_PARAM_HotelReservationIDs hotelReservationIDs;
    private String orderStatus;

    public TCSL_RESULT_AddOrder_ResGlobalInfo() {
    }

    public TCSL_RESULT_AddOrder_ResGlobalInfo(TCSL_PARAM_HotelReservationIDs hotelReservationIDs, String orderStatus) {
        this.hotelReservationIDs = hotelReservationIDs;
        this.orderStatus = orderStatus;
    }
    @XmlElement(name = "HotelReservationIDs")
    public TCSL_PARAM_HotelReservationIDs getHotelReservationIDs() {
        return hotelReservationIDs;
    }

    @XmlElement(name = "OrderStatus")
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setHotelReservationIDs(TCSL_PARAM_HotelReservationIDs hotelReservationIDs) {
        this.hotelReservationIDs = hotelReservationIDs;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
