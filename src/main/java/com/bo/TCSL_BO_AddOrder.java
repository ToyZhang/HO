package com.bo;

import com.xml.*;
import org.springframework.stereotype.Controller;

/**
 * Created by zhangtuoyu on 2017/6/23.
 */
@Controller
public class TCSL_BO_AddOrder {
    public TCSL_RESULT_AddOrder_OTA_HotelResRS addOrder(TCSL_PARAM_AddOrder_OTA_HotelResRQ orderInfo){
        TCSL_RESULT_AddOrder_OTA_HotelResRS result = new TCSL_RESULT_AddOrder_OTA_HotelResRS();
        //success 返回值样例
//        result.setSuccess("");
//        TCSL_PARAM_HotelReservationID id = new TCSL_PARAM_HotelReservationID("24","21312");
//        List<TCSL_PARAM_HotelReservationID> idList = new ArrayList<TCSL_PARAM_HotelReservationID>();
//        idList.add(id);
//        TCSL_PARAM_HotelReservationIDs ids = new TCSL_PARAM_HotelReservationIDs(idList);
//        TCSL_RESULT_AddOrder_ResGlobalInfo info = new TCSL_RESULT_AddOrder_ResGlobalInfo(ids,"3");
//        TCSL_RESULT_AddOrder_HotelReservation reservation = new TCSL_RESULT_AddOrder_HotelReservation(info);
//        TCSL_RESULT_AddOrder_HotelReservations reservations = new TCSL_RESULT_AddOrder_HotelReservations(reservation);
//        result.setHotelReservations(reservations);
        //fail 返回值样例
//        TCSL_RESULT_Error error = new TCSL_RESULT_Error("BS0001","错误描述");
//        TCSL_RESULT_Errors errors = new TCSL_RESULT_Errors(error);
//        result.setErrors(errors);
        //订单所属渠道
        String channel = orderInfo.getPos().getSource().getRequestorID().getID();
        //订单房型
        String roomType = orderInfo.getHotelReservations().getHotelReservation().getRoomStays()
                .getRoomStay().getRoomRates().getRoomRate().getRoomTypeCode();
        //预订房数
        String roomNum = orderInfo.getHotelReservations().getHotelReservation().getRoomStays()
                .getRoomStay().getRoomRates().getRoomRate().getNumberOfUnits();
        return result;
    }
}
