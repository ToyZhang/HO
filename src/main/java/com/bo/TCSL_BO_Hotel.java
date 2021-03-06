package com.bo;
import com.dao.TCSL_DAO_Hotel;
import com.po.*;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import com.util.TCSL_UTIL_COMMON;
import com.util.TCSL_UTIL_RESOURCE;
import com.util.TCSL_UTIL_XML;
import com.vo.*;

import com.xml.TCSL_XML_OTA_HotelAvailNotifRS;
import com.xml.TCSL_XML_PMSHotelMappingResult;
import com.xml.TCSL_XML_PmsHotelInfoRS;
import org.apache.axiom.om.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * @DESCRIPTION
 * @AUTHER administrator zhangna
 * @create 2017-05-15
 */
@Repository
public class TCSL_BO_Hotel {
    @Autowired
    private TCSL_DAO_Hotel daoHotel;
    private Logger logger = Logger.getLogger(TCSL_BO_Hotel.class);
    /**
     * 上传酒店信息，酒店产品信息
     * @param hotelInfo
     * @return
     */
    public TCSL_VO_Result uploadHotelInfo(TCSL_VO_HotelInfo hotelInfo){
        logger.info("进入uploadHotelInfo()上传酒店信息产品信息方法");
        TCSL_VO_Result result = new TCSL_VO_Result();
        Properties otaProperty = TCSL_UTIL_COMMON.getProperties("ota.properties"); //OTA配置文件中相关信息
        /**
         * 1.将数据库中不存在的产品拼成待上传产品列表
         */
        //1.1将数据库所有产品查询出来组成map<数据库复合主键,数据库映射类po>，便于快速判断产品是否存在数据库中
        String shopId = hotelInfo.getHotelCode(); //pms酒店编号
        List<TCSL_PO_HotelProduct> dbProducts = daoHotel.getProducts(shopId); //获取数据库中酒店产品列表
        String[] keyArr = {"CSHOPID","CCHANNEL","CROOMTYPEID","CPAYTYPE"}; //不同参数顺序，生成key不同
        List<String> keyList = Arrays.asList(keyArr);
        Map<String,TCSL_PO_HotelProduct> dbProductsMap = TCSL_UTIL_COMMON.changeToMap(dbProducts,keyList);
        //1.2遍历hotelInfo中产品列表，比对map中数据，创建出待上传产品列表
        List<TCSL_VO_HotelProduct> products = hotelInfo.getProducts(); //请求参数中产品列表
        List<TCSL_VO_HotelProduct> uploadProducts = new ArrayList<TCSL_VO_HotelProduct>(); //待上传OTA产品列表
        for (TCSL_VO_HotelProduct product :products) {
            if (product == null) { //参数中产品信息为空，解析下一个
                continue;
            }
            String channel = product.getChannel(); //产品应用渠道
            String roomType = product.getRoomTypeCode(); //产品房型编码
            String payType = product.getBalanceType(); //产品支付方式
            String[] paramArr = {shopId,channel,roomType,payType};
            boolean checkResult = TCSL_UTIL_COMMON.checkParmIsValid(paramArr); //校验关键参数是否存在空，防止主键值为空，无法插入数据库
            if(checkResult == true){
                result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
                result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
                result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
                return result;
            }
            //查看数据库中是否存在该产品
            String key = shopId+channel+roomType+payType;
            //去空格str = .replaceAll("\\s*", "");可以替换大部分空白字符， 不限于空格;\s 可以匹配空格、制表符、换页符等空白字符的其中任意一个。
            key = key.replaceAll("\\s*",""); //去除参数中空格
            TCSL_PO_HotelProduct dbProduct = dbProductsMap.get(key);
            if(dbProduct == null){
                uploadProducts.add(product);
            }
        }
        /**
         * 2.将待上传产品转换成soapBody所要求的xml
         */
        OMElement hotelXml = createHotelXml(hotelInfo,uploadProducts,otaProperty);
        logger.debug("uploadHotelInfo()---发送soap请求xml---"+hotelXml);
        /**
         * 3.发送soap请求，上传酒店及产品信息
         */
        String url = otaProperty.getProperty("ota_uploadHotelInfo_url");
        String soapAction = otaProperty.getProperty("ota_uploadHotelInfo_soapAction");
        String soapResponse = TCSL_UTIL_XML.sendSoap(url,soapAction,hotelXml);
        logger.debug("uploadHotelInfo()--发送soap响应---"+soapResponse);
        //判断上传OTA是否成功
        if("".equals(soapResponse)){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_OTA);//401
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_OTA);//上传OTA失败信息
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            return result;
        }
        //将soapResponse转换成bean对象
        TCSL_XML_PmsHotelInfoRS productResult = TCSL_UTIL_XML.xmlTojavaBean(TCSL_XML_PmsHotelInfoRS.class,soapResponse);
        //判断是否创建失败
        int size = productResult.getpMSHotelMappingResults().getpMSHotelMappingResult().size();
        boolean isSucess = Boolean.parseBoolean(productResult.getpMSHotelMappingResults().getpMSHotelMappingResult().get(0).getIsSuccess());
        if(size==1 && isSucess == false){
            String msg = productResult.getpMSHotelMappingResults().getpMSHotelMappingResult().get(0).getMessage();
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_OTA);//401
            result.setErrorText(msg);//上传OTA失败信息
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            return result;
        }
        /**
         * 4.解析产品创建结果
         */
        List<TCSL_VO_ProductResult> productResults = analyzeProducts(productResult); //产品创建结果列表
        /**
         * 5.判断所有待上传产品是否创建成功
         */
        for (TCSL_VO_HotelProduct product:uploadProducts){
            String channel = product.getChannel();
            String roomTypeId = product.getRoomTypeCode();
            List<TCSL_PO_ProductFailInfo> failList = daoHotel.getFailInfo(shopId,channel,roomTypeId); //酒店产品创建失败记录表
            if(failList.isEmpty()){
                daoHotel.addOrUpdateHotel(hotelInfo,channel);
                daoHotel.addOrUpdateProduct(shopId,product);
            }else{
                result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_PRODUCTFAIL);
                result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_PRODUCTFAIL);
                result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL);
            }

        }
        result.setData(productResults);
        //成功
        if(result.getErrorCode() == null){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_SUCCESS);
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_SUCCESS);
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_SUCCESS);
        }
        return result;
    }

    /**
     * 解析酒店产品创建结果
     * @param hotelInfoRs soap响应xml对应javaBean
     * @return
     */
    public List<TCSL_VO_ProductResult> analyzeProducts(TCSL_XML_PmsHotelInfoRS hotelInfoRs){
        List<TCSL_VO_ProductResult> result = new ArrayList<TCSL_VO_ProductResult>();
        /**
         * 1.解析hotelInfoRs，判断每个产品活动创建结果，将失败的存入数据库失败记录表
         * 2.成功的记录，去数据库失败记录表删除对应的记录
         */
        if(hotelInfoRs == null || (hotelInfoRs!=null && hotelInfoRs.getpMSHotelMappingResults() == null)){ //创建产品结果为空
            return result;
        }
        List<TCSL_XML_PMSHotelMappingResult> productResult = hotelInfoRs.getpMSHotelMappingResults().getpMSHotelMappingResult();
        //遍历解析产品创建结果列表
        for (TCSL_XML_PMSHotelMappingResult xmlProductResult:productResult){
            if(productResult == null){
                continue;
            }
            String errorCode = xmlProductResult.getErrorCode();
            //数据库中只保存以下四种创建失败产品的失败记录
            List<String> errorCodeList = new ArrayList<String>();
            errorCodeList.add("101"); //产品已经存在
            errorCodeList.add("102"); //酒店创建失败
            errorCodeList.add("103"); //价格代码不存在
            errorCodeList.add("104"); //房型创建失败
            if(!errorCodeList.contains(errorCode)){
                result.add(new TCSL_VO_ProductResult(xmlProductResult));
                continue;
            }
            //单个产品创建结果信息
            TCSL_VO_ProductResult voProductResult = new TCSL_VO_ProductResult(xmlProductResult);
            String channel = voProductResult.getChannel().toUpperCase();
            String hotelCode = voProductResult.getHotelCode();
            String roomTypeCode = voProductResult.getRoomTypeCode();
            String ratePlanCode = voProductResult.getRatePlanCode();
            boolean isSuccess = voProductResult.getIsSuccess();
            String message = voProductResult.getMessage();
            /**
             * 判断该产品是否创建过
             */
            //判断产品是否已创建成功
            String payType = daoHotel.getProductPayType(channel,ratePlanCode); //支付方式
            TCSL_PO_HotelProduct dbProduct = daoHotel.getProduct(hotelCode,channel,roomTypeCode,payType);
            if(dbProduct != null){
                continue;
            }
            if(true == isSuccess){//产品创建成功,删除失败日志表中记录
                daoHotel.deleteFailInfo(hotelCode,channel,roomTypeCode,ratePlanCode);
            }else{ //产品创建失败，添加失败记录
                TCSL_PO_ProductFailInfo faliInfo = daoHotel.getFailInfoSingle(hotelCode,channel,roomTypeCode,ratePlanCode);
                if(faliInfo == null){
                    daoHotel.addFailInfo(hotelCode,channel,roomTypeCode,ratePlanCode,message);
                }else{
                    daoHotel.updateFailInfo(hotelCode,channel,roomTypeCode,ratePlanCode,message);
                }
            }
            result.add(voProductResult);
        }
        return result;
    }
    /**
     * hotelInfo转换成OTA接口所需xml
     * @param hotelInfo 酒店信息
     * @param products 待上传产品列表
     * @param otaProperty ota相关配置信息
     * @return
     */
    public OMElement createHotelXml(TCSL_VO_HotelInfo hotelInfo,List<TCSL_VO_HotelProduct> products,Properties otaProperty){
        List<TCSL_PO_ProductActivity> activities = daoHotel.getProductActivity(); //OTA所有活动列表
        OMFactory factory = OMAbstractFactory.getOMFactory();
        String nameSpaceProperty =  otaProperty.getProperty("ota_nameSpace");
        OMNamespace namespace = factory.createOMNamespace(nameSpaceProperty,"");
        OMElement pmsHotelInfoRQTag = factory.createOMElement("PmsHotelInfoRQ",namespace);
        OMElement pMSBaseHotelInfosTag = factory.createOMElement("PMSBaseHotelInfos",null);
        OMElement pMSHotelInfoTag = factory.createOMElement("PMS_Hotel_Info",null);

        String hotelCode = hotelInfo.getHotelCode(); //酒店编码
        String hotelName = hotelInfo.getHotelName(); //酒店名称
        String telephone = hotelInfo.getTelephone(); //酒店联系电话
        String address = hotelInfo.getAddress(); //酒店地址
        String email = hotelInfo.getEmail(); //酒店联系邮箱
        String city = hotelInfo.getHotelCityName(); //酒店所在城市名称
        String province = hotelInfo.getProvinceCode(); //酒店所在省份
        OMElement hotelCodeTag = factory.createOMElement("HotelCode",null); //酒店编码标签
        hotelCodeTag.setText(hotelCode);
        OMElement hotelNameTag = factory.createOMElement("HotelName",null); //酒店名称标签
        hotelNameTag.setText(hotelName);
        OMElement groupTag = factory.createOMElement("HotelGroupCode",null); //OTA商户组标签，使用2.0模式进行对接
        groupTag.setText("TCSL2");
        OMElement telephoneTag = factory.createOMElement("Telephone",null); //酒店联系电话标签
        telephoneTag.setText(telephone);
        OMElement addressTag = factory.createOMElement("Address",null); //酒店地址标签
        addressTag.setText(address);
        OMElement emailTag = factory.createOMElement("Email",null); //酒店联系邮箱标签
        emailTag.setText(email);
        OMElement cityTag = factory.createOMElement("HotelCityName",null); //酒店所在城市名称标签
        cityTag.setText(city);
        OMElement provinceTag = factory.createOMElement("ProvinceCode",null); //酒店所在省份标签
        provinceTag.setText(province);
        pMSHotelInfoTag.addChild(hotelCodeTag);
        pMSHotelInfoTag.addChild(hotelNameTag);
        pMSHotelInfoTag.addChild(groupTag);
        pMSHotelInfoTag.addChild(telephoneTag);
        pMSHotelInfoTag.addChild(addressTag);
        pMSHotelInfoTag.addChild(emailTag);
        pMSHotelInfoTag.addChild(cityTag);
        pMSHotelInfoTag.addChild(provinceTag);
        OMElement pmsHotelProductInfosTag = factory.createOMElement("PmsHotelProductInfos",null); //酒店所有产品列表标签
        //遍历products
        for (TCSL_VO_HotelProduct product:products){
            for (TCSL_PO_ProductActivity activity:activities){
                if(!activity.getCCHANNEL().equals(product.getChannel())){ //产品应用渠道 与 线上活动渠道不符合
                    continue;
                }
                if(!activity.getCPAYTYPE().equals(product.getBalanceType())){ //产品应用支付方式 与 线上活动支付方式不符合
                    continue;
                }
                OMElement pMSProductInfoTag = factory.createOMElement("PMS_Product_Info",null); //产品信息标签
                //房型标签
                OMElement roomTypeTag = factory.createOMElement("RoomTypeCode",null);
                roomTypeTag.setText(product.getRoomTypeCode());
                pMSProductInfoTag.addChild(roomTypeTag);
                //房型名称
                OMElement roomNameTag = factory.createOMElement("RoomTypeName",null);
                roomNameTag.setText(product.getRoomTypeName());
                pMSProductInfoTag.addChild(roomNameTag);
                //OTA活动编码
                OMElement activityCodeTag = factory.createOMElement("RatePlanCode",null);
                activityCodeTag.setText(activity.getCACTIVITYID());
                pMSProductInfoTag.addChild(activityCodeTag);
                //支付类型
                OMElement balanceTypeTag = factory.createOMElement("BalanceType",null);
                balanceTypeTag.setText(product.getBalanceType());
                pMSProductInfoTag.addChild(balanceTypeTag);
                //OTA活动名称
                OMElement activityNameTag = factory.createOMElement("RatePlanName",null);
                activityNameTag.setText(activity.getCACTIVITYNAME());
                pMSProductInfoTag.addChild(activityNameTag);
                //渠道
                OMElement channelTag = factory.createOMElement("Channel",null);
                channelTag.setText(product.getChannel());
                pMSProductInfoTag.addChild(channelTag);
                pmsHotelProductInfosTag.addChild(pMSProductInfoTag);
            }
        }
        pMSHotelInfoTag.addChild(pmsHotelProductInfosTag);
        pMSBaseHotelInfosTag.addChild(pMSHotelInfoTag);
        pmsHotelInfoRQTag.addChild(pMSBaseHotelInfosTag);
        return pmsHotelInfoRQTag;
    }

    /**
     * 上传房态逻辑部分
     * @param roomStatus 房态信息
     * @return
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public TCSL_VO_Result uploadRoomStatus(TCSL_VO_RoomStatus roomStatus){
        TCSL_VO_Result result = new TCSL_VO_Result();
        //待添加房态主表列表
        List<TCSL_PO_RoomStatus> addStatusList = new ArrayList<TCSL_PO_RoomStatus>();
        //待上传房态明细表列表
        List<TCSL_PO_ProductStatusDt> updateStatusDTs = new ArrayList<TCSL_PO_ProductStatusDt>();
        //待更新房态列表
        List<TCSL_PO_RoomStatus> updateStatusList = new ArrayList<TCSL_PO_RoomStatus>();
        //对roomStatus中projects房态列表有效性验证
        if(roomStatus.getProjects() == null || roomStatus.getProjects().size()==0){
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不完整
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
            logger.info("上传的房态信息参数不完整！");
            return  result;
        }
        /**
         * 批量添加数据库
         * 1.检验关键房态参数是否为空，校验房态在产品表中是否存在该产品（根据酒店代码、渠道、房型代码），不存在则返回失败把不存在数据放到data中，存在则
         * 2.查询房态是否存在，若房态不存在，放到房态待上传列表，若房态存在放到待更新列表
         * 3.删除该房态在子表中数据，插入房态的子表信息
         */
        //1.检验关键房态参数是否为空
        for(TCSL_VO_RSItem obj : roomStatus.getProjects()){
            //参数校验（房态生效时间/PMS房型代码/价格代码/渠道代码/房间状态）是否为空,调用工具类中的checkParmIsValid方法
            ArrayList param = new ArrayList();
            List<String> channels = obj.getDestinationSystemCodes();//房型应用渠道列表
            param.add(obj.getStart());//房态生效开始时间
            param.add(obj.getInvTypeCode()); //房型
            param.add(obj.getRatePlanCode()); //线下房态方案编码
            param.add(obj.getStatus()); //房态
            param.add(obj.getEnd()); //房态生效结束时间
            logger.debug("房态生效开始时间："+obj.getStart()+"渠道："+obj.getDestinationSystemCodes()+"房型代码："+obj.getInvTypeCode()+"房态生效结束时间"+obj.getEnd());
            if(TCSL_UTIL_COMMON.checkParmIsValid(param) || channels == null || channels.size() ==0){
                result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_INVALIDPARAM);//400
                result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_INVALIDPARAM);//参数不全
                result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
                logger.info("房态信息参数不全！");
                return result;
            }
        }
        /**
         * 1.判断房态产品在产品表中是否存在
         * 1.1 不存在 返回失败
         * 1.2 存在 判断房态方案在房态表中是否存在
         * 1.2.1 存在 添加到待更新房态列表中
         *       不存在 添加待添加房态列表中
         * 1.2.2 将房态明细数据添加到待更新房态明细列表中
         */
        String hotelCode = roomStatus.getHotelCode();//酒店代码
        for(TCSL_VO_RSItem status : roomStatus.getProjects()){
            String roomCode = status.getInvTypeCode() ;//房型代码
            String planId = status.getRatePlanCode();//线下房态方案编码
            String delFlg = status.getRemoveFlg(); //线下房态删除标志 0:不删除 1:删除
            for(String channel : status.getDestinationSystemCodes()) {// 遍历渠道列表
                //校验房态在产品表中是否存在该产品（根据酒店代码、渠道、房型代码）
                List<TCSL_PO_HotelProduct> productsList = daoHotel.getProductExceptChannel(hotelCode, channel, roomCode);
                if (productsList.size() == 0) {
                    //如果这个产品不在产品表中，
                    result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_PRODUCTNO);//503
                    result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_PRODUCTNO);//产品不存在
                    result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL); //失败
                    result.setData(status);
                    logger.info("没有此产品！");
                    return result;
                }
                //查询房态是否存在,若房态不存在，放到房态待上传列表，若房态存在放到待更新列表
                TCSL_PO_RoomStatus poRoomStatus = new TCSL_PO_RoomStatus();
                poRoomStatus.setCCHANNEL(channel);//渠道
                poRoomStatus.setCPLANID(planId);//线下房态方案编码
                poRoomStatus.setCSHOPID(hotelCode);//酒店代码
                poRoomStatus.setCPLANNAME(status.getRatePlanName());//线下房态方案名称
                poRoomStatus.setIDELFLG(Integer.parseInt(delFlg)); //线下房态方案删除标志
                TCSL_PO_ProductStatusDt statusDetail = new TCSL_PO_ProductStatusDt();
                statusDetail.setCPLANID(planId);//线下房态方案编码
                statusDetail.setCROOMTYPEID(roomCode);//房型编码
               //字符串转date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String startDate = status.getStart();
                String endDate = status.getEnd();
                try {
                    statusDetail.setDBTIME(sdf.parse(startDate));//房态生效开始时间
                    statusDetail.setDETIME(sdf.parse(endDate));//房态生效结束时间
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if("open".equalsIgnoreCase(status.getStatus())) {
                    statusDetail.setIROOMSTATUS(1);//房间状态 1:打开状态
                }else{
                    statusDetail.setIROOMSTATUS(0);//房间状态 0：关闭状态
                }
                if(daoHotel.getRoomState(hotelCode,channel,planId) == null){//该房态方案 数据库中是否存在
                    addStatusList.add(poRoomStatus);//添加到待上传房态列表
                    updateStatusDTs.add(statusDetail);//添加到待上传房态详情列表
                }else{
                    //房态已存在
                    updateStatusList.add(poRoomStatus);//添加到待更新列表
                    updateStatusDTs.add(statusDetail);//添加到待上传房态详情列表
                }
            }
        }
        //批量上传房态信息
        this.batchOperate(addStatusList,updateStatusList,updateStatusDTs);
        /**
         * 2.将上传房态数据整合成OTA线上活动产品对应数据
         * 2.1根据房型，酒店编码，渠道，关联查出该线下房型对应OTA活动产品，房态
         * 2.2将数据转换成soapXML
         * 2.3发送soap请求
         * 2.4 soap发送成功返回成功，发送失败 返回失败 启动补偿线程rsEqualize，该线程在工程启动时也启动
         * 2.4.1 补偿线程 查询该酒店所有未上传房态数据
         * 2.4.2 整合数据成soapXML(可调用bo层整合方法) 发送soap请求
         * 2.4.3 发送失败 TCSL_UTIL_COMMON.equalizeNum 加一，与ota.properties中设置的补偿次数相等时，暂停补偿
         *       发送成功，TCSL_UTIL_COMMON.equalizeNum置为0，fusingFlag置为false，关闭线程
         */
        //2.1线下产品转换为对应线上活动产品
        List<TCSL_VO_RSItem> list = changeToOTARS(roomStatus);
        //2.2将数据转换成soapXML
        Properties p = TCSL_UTIL_COMMON.getProperties("ota.properties");
        String url = p.getProperty("ota_uploadRoomStatus_url");
        OMElement soapXml = createRsXml(roomStatus.getHotelCode(),list,p);
        logger.info("整合soapXml-----"+soapXml.toString());

        //2.3发送soap请求,成功返回成功，失败启动补偿线程
        String soapResult = TCSL_UTIL_XML.sendSoap(url,"",soapXml);
        if(!"".equals(soapResult)){ //发送soap成功
            //解析soapResult判断是否OTA处理成功
            TCSL_XML_OTA_HotelAvailNotifRS res = TCSL_UTIL_XML.xmlTojavaBean(TCSL_XML_OTA_HotelAvailNotifRS.class,soapResult);
            if(res.getErrors() == null || "".equals(res.getErrors())){ //OTA解析成功
                //更新数据房态方案上传OTA时间
                for(TCSL_VO_RSItem item :roomStatus.getProjects()){
                    for (String channel :
                            item.getDestinationSystemCodes()) { //每个线下房态方案应用的渠道
                        daoHotel.updateRsOtaTimeByKey(hotelCode,item.getRatePlanCode(),channel);
                    }
                }
//TODO

                result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_SUCCESS);
                result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_SUCCESS);//成功
                result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_SUCCESS);
            }else{ //OTA解析失败
                result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_OTA);
                result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_OTA);//上传OTA失败
                result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL);
            }
            return result;
        }else{ //发送soap失败
            result.setErrorCode(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_CODE_OTAFAIL);
            result.setErrorText(TCSL_UTIL_RESOURCE.RESOURCE_ERROR_DES_OTAFAIL);//soap发送失败
            result.setReturnCode(TCSL_UTIL_RESOURCE.RESOURCE_RETRUN_CODE_FAIL);
            return result;
        }
    }

    /**
     * 批量处理房态数据
     * @param uploadStatus
     * @param updateStatus
     * @param uploadStateDetails
     */
    public void batchOperate(List<TCSL_PO_RoomStatus> uploadStatus,List<TCSL_PO_RoomStatus> updateStatus,List<TCSL_PO_ProductStatusDt> uploadStateDetails){
        if(uploadStatus.size()!=0){//待添加房态数据列表
            List<TCSL_PO_RoomStatus> uploadList = new ArrayList<TCSL_PO_RoomStatus>();//30条上传房态列表
            for(int i=0;i<uploadStatus.size();i++){
                //单条插入
                //daoHotel.addRoomSingle(uploadStatus.get(i));
                uploadList.add(uploadStatus.get(i));
                if((i != 0 && i % 30 == 0) || i == uploadStatus.size()-1 ){//每30条上传一次
                    daoHotel.addRoomStatus(uploadList);//批量添加房态数据
                    //清空已上传30条房态信息
                    uploadList = new ArrayList<TCSL_PO_RoomStatus>();
                }
            }
        }
        if(updateStatus.size()!=0){//待更新房态信息列表
            List<TCSL_PO_RoomStatus> updateList = new ArrayList<TCSL_PO_RoomStatus>();//30条更新房态列表
            for(int i=0;i<updateStatus.size();i++){
                updateList.add(updateStatus.get(i));
                if((i != 0 && i % 30 == 0) || ((updateStatus.size()-1)==i )){//每30条更新一次
                    //批量更新待更新房态信息
                    daoHotel.updateRoomStatus(updateList);
                    //清空已更新的30条房态信息
                    updateList = new ArrayList<TCSL_PO_RoomStatus>() ;
                }
            }
        }
        if(uploadStateDetails.size()!=0){//待更新房态详情列表
            List<TCSL_PO_ProductStatusDt> uploadStateData = new ArrayList<TCSL_PO_ProductStatusDt>();//30条上传房态详情列表
            for (int i = 0 ; i < uploadStateDetails.size() ;i++){
                uploadStateData.add(uploadStateDetails.get(i));
                if((i != 0 && i % 30 == 0) || i == uploadStateDetails.size()-1){
                    //批量上传房态详情(先删除，在插入)
                    daoHotel.delRoomStatusDetail(uploadStateData);
                    daoHotel.addRoomStatusDetail(uploadStateData);
                    //清空已上传的30条房态信息
                    uploadStateData = new ArrayList<TCSL_PO_ProductStatusDt>();
                }
            }
        }
    }
    /**
     * 创建上传房态soapXml
     * @param hotelCode 酒店编码
     * @param list 酒店线上活动产品房态列表
     * @param otaProperty ota配置文件
     * @return
     */
    public OMElement createRsXml(String hotelCode,List<TCSL_VO_RSItem> list,Properties otaProperty){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        //创建命名空间
        String nameSpaceProperty =  otaProperty.getProperty("ota_nameSpace");
        OMNamespace namespace = factory.createOMNamespace(nameSpaceProperty,"");
        String nameSpaceXsiProperty = otaProperty.getProperty("ota_nameSpace_xsi");
        OMNamespace nameSpaceXsi =  factory.createOMNamespace(nameSpaceXsiProperty,"xsi");
        //创建xml节点
        OMElement OTA_HotelAvailNotifRQ = factory.createOMElement("OTA_HotelAvailNotifRQ",namespace);
        OTA_HotelAvailNotifRQ.declareNamespace(nameSpaceXsi);//添加多个命名空间
        OMElement AvailStatusMessages = factory.createOMElement("AvailStatusMessages",null);
        AvailStatusMessages.addAttribute("HotelCode",hotelCode,null);//节点属性
        for(TCSL_VO_RSItem rsItem : list){
            String start = rsItem.getStart();
            String end = rsItem.getEnd();
            String invTypeCode = rsItem.getInvTypeCode();
            String ratePlanCode = rsItem.getRatePlanCode();
            String mon = rsItem.getMon();
            String tue = rsItem.getTue();
            String webs = rsItem.getWeds();
            String thur = rsItem.getThur();
            String fri = rsItem.getFri();
            String sat = rsItem.getSat();
            String sun =  rsItem.getSun();
            //创建节点
            OMElement AvailStatusMessage = factory.createOMElement("AvailStatusMessage",null);
            OMElement StatusApplicationControl = factory.createOMElement("StatusApplicationControl",null);
            //创建节点属性
            StatusApplicationControl.addAttribute("Start",start,null);
            StatusApplicationControl.addAttribute("End",end,null);
            StatusApplicationControl.addAttribute("InvTypeCode",invTypeCode,null);
            StatusApplicationControl.addAttribute("RatePlanCode",ratePlanCode,null);
            StatusApplicationControl.addAttribute("Mon",mon,null);
            StatusApplicationControl.addAttribute("Tue",tue,null);
            StatusApplicationControl.addAttribute("Weds",webs,null);
            StatusApplicationControl.addAttribute("Thur",thur,null);
            StatusApplicationControl.addAttribute("Fri",fri,null);
            StatusApplicationControl.addAttribute("Sat",sat,null);
            StatusApplicationControl.addAttribute("Sun",sun,null);
            //创建节点
            OMElement DestinationSystemCodes = factory.createOMElement("DestinationSystemCodes",null);
            for (String channel:rsItem.getDestinationSystemCodes()){//渠道列表
                OMElement DestinationSystemCode = factory.createOMElement("DestinationSystemCode",null);
                DestinationSystemCode.setText(channel);
                DestinationSystemCodes.addChild(DestinationSystemCode);
            }
            StatusApplicationControl.addChild(DestinationSystemCodes);
            AvailStatusMessage.addChild(StatusApplicationControl);
            AvailStatusMessages.addChild(AvailStatusMessage);
        }
        OTA_HotelAvailNotifRQ.addChild(AvailStatusMessages);
        return OTA_HotelAvailNotifRQ;
    }

    /**
     * 将线下产品房态转换成线上活动产品房态
     * @return 线上活动产品列表
     */
    public List<TCSL_VO_RSItem> changeToOTARS(TCSL_VO_RoomStatus roomStatus){
        String hotelCode = roomStatus.getHotelCode(); //酒店编码
        List<TCSL_VO_RSItem> projects = roomStatus.getProjects(); //酒店待处理参数列表
        List<TCSL_VO_RSItem> list = new ArrayList<TCSL_VO_RSItem>();//线上活动产品列表
        //获取线下产品对应的线上活动信息
        Map<TCSL_VO_RSItem,List<TCSL_PO_ProductActivity>> rsMap =
                new HashMap<TCSL_VO_RSItem, List<TCSL_PO_ProductActivity>>(); //key 线下产品，value 线下产品对应的线上活动列表
        for (TCSL_VO_RSItem item : projects){ //遍历获取到请求参数中每个产品
            List<String> channels = item.getDestinationSystemCodes();
            for (String channel : channels){ //每个产品的渠道列表
                List<TCSL_PO_ProductActivity> activities =
                        daoHotel.getActivity(hotelCode,channel,item.getInvTypeCode()); //每个产品每个渠道的线上活动列表
                rsMap.put(item,activities);
            }
        }
        //拼接线上活动产品列表
        for (TCSL_VO_RSItem item : projects){
            List<TCSL_PO_ProductActivity> activities = rsMap.get(item);
            for(TCSL_PO_ProductActivity poActivity : activities){
                //使用activity的活动方案代码，名称，克隆一个rsItem
                TCSL_VO_RSItem rsItem = new TCSL_VO_RSItem(item,poActivity);
                list.add(rsItem);
            }
        }
        return list;
    }
    /**
     * 上传房价逻辑部分
     * @param roomPrice 房价信息
     * @return
     */
    public TCSL_VO_Result uploadRoomPrice(TCSL_VO_RoomPrice roomPrice){
        TCSL_VO_Result result = new TCSL_VO_Result();
        //酒店代码
        String hotelCode = roomPrice.getHotelCode();
        //酒店价格方案列表
        List<TCSL_VO_RPItem> rpItems =  roomPrice.getProjects();
        //TODO
        return result;
    }
}
