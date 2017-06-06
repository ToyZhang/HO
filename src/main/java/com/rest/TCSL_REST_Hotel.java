package com.rest;
import com.bo.TCSL_BO_Hotel;
import com.util.TCSL_UTIL_COMMON;
import com.util.TCSL_UTIL_RESOURCE;
import com.vo.TCSL_VO_HotelInfo;
import com.vo.TCSL_VO_Result;
import com.vo.TCSL_VO_RoomStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @DESCRIPTION
 * @AUTHER administrator zhangna
 * @create 2017-05-15
 */
@Controller
@RequestMapping("hotel")
public class TCSL_REST_Hotel {
    private Logger logger = Logger.getLogger(TCSL_REST_Hotel.class);
    @Autowired
    private TCSL_BO_Hotel boHotel;
    /**
     * 上传酒店及产品信息
     * @param hotelInfo 酒店及产品信息
     * @return
     */
    @RequestMapping(value = "/uploadHotelInfo", method = RequestMethod.POST)
    @ResponseBody
    public TCSL_VO_Result uploadHotelInfo(@RequestBody TCSL_VO_HotelInfo hotelInfo){
        logger.info("参数解析进入uploadHotelInfo");
        logger.debug("参数：TCSL_VO_HotelInfo"+hotelInfo.getHotelName()+"酒店代码："+hotelInfo.getHotelCode());
        TCSL_VO_Result result = new TCSL_VO_Result();
        if(hotelInfo == null){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            logger.info("酒店信息参数不全！");
            return result;
        }
        //参数校验（酒店代码/酒店名称）是否为空,调用工具类中的checkParmIsValid方法
        List param = new ArrayList();
        param.add(hotelInfo.getHotelCode());
        param.add(hotelInfo.getHotelName());
        if(TCSL_UTIL_COMMON.checkParmIsValid(param)){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            logger.info("酒店信息参数不全！");
            return result;
        }
        result = boHotel.uploadHotelInfo(hotelInfo);
        return result;
    }

    /**
     * 上传酒店房态对外接口
     * 上传酒店不同时间段，不同类型房间的房态到云端
     * @param roomStatus 房态信息
     * @return
     */
    @RequestMapping(value = "/uploadRoomStatus", method = RequestMethod.POST)
    @ResponseBody
    public TCSL_VO_Result uploadRoomStatus(@RequestBody TCSL_VO_RoomStatus roomStatus){
        logger.info("参数解析进入uploadRoomStatus()");
        logger.debug("参数：TCSL_VO_RoomStatus:酒店代码-"+roomStatus.getHotelCode());
        TCSL_VO_Result result = new TCSL_VO_Result();
        if(roomStatus == null){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            logger.info("酒店信息参数不全！");
            return result;
        }
        //参数校验（PMS酒店代码）是否为空
        if(roomStatus.getHotelCode() == null || "".equals(roomStatus.getHotelCode())){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            logger.info("酒店信息参数不全！");
            return result;
        }
        //调用bo层上传酒店房态逻辑处理
        result = boHotel.uploadRoomStatus(roomStatus);
        return result;
    }
}
