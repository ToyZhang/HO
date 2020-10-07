package com.webService;


import com.bo.TCSL_BO_AddOrder;
import com.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Created by zhangtuoyu on 2017/6/22.
 */
@Repository("tcsl_ws_addOrder")
@WebService(serviceName = "addOrder")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class TCSL_WS_AddOrder {
    @Autowired
    private TCSL_BO_AddOrder boAddOrder;

    @WebMethod
    @WebResult(name = "OTA_HotelResRS")
    public TCSL_RESULT_AddOrder_OTA_HotelResRS addOrder(@WebParam(name = "OTA_HotelResRQ") TCSL_PARAM_AddOrder_OTA_HotelResRQ param){
        TCSL_RESULT_AddOrder_OTA_HotelResRS result = boAddOrder.addOrder(param);
        return result;
    }

}
