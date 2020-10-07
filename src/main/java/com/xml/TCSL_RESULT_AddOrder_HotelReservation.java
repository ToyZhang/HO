package com.xml;

import org.springframework.stereotype.Repository;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
public class TCSL_RESULT_AddOrder_HotelReservation {
    private TCSL_RESULT_AddOrder_ResGlobalInfo resGlobalInfo;

    public TCSL_RESULT_AddOrder_HotelReservation() {
    }

    public TCSL_RESULT_AddOrder_HotelReservation(TCSL_RESULT_AddOrder_ResGlobalInfo resGlobalInfo) {
        this.resGlobalInfo = resGlobalInfo;
    }
    @XmlElement(name = "ResGlobalInfo")
    public TCSL_RESULT_AddOrder_ResGlobalInfo getResGlobalInfo() {
        return resGlobalInfo;
    }

    public void setResGlobalInfo(TCSL_RESULT_AddOrder_ResGlobalInfo resGlobalInfo) {
        this.resGlobalInfo = resGlobalInfo;
    }
}
