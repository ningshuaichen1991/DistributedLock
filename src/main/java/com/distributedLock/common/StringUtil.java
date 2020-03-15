package com.distributedLock.common;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
/**
 */
public class StringUtil {
	private static Logger logger = LoggerFactory.getLogger(StringUtil.class);
	public static SimpleDateFormat yyyyMMddHHmmssSSSFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	

    public static String getDateTimeRadomStr(int length) {
        return yyyyMMddHHmmssSSSFormat.format(new Date())+getRadomStr(length);
    }
    public static boolean isEmpty(String str) {
        if (str == null || "null".equals(str) || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }
    

    
    public static boolean isNull(Object o){
    	return (o==null||o.toString().trim().equals("")||o.toString().equals("null")||o.toString().trim().length()==0);
    }

    public static String getRadomStr(int length){
        StringBuilder result = new StringBuilder("");
        char[] allChars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'X'};
        for (int i = 0; i < length; i++) {
            int index = new Random().nextInt(allChars.length);
            result.append(allChars[index]);
        }
        return result.toString();
    }

    /**
     * 将int转换为指定长度的字符串，前缀以0补位
     * 
     * @param intValue
     * @param strLength
     * @return
     */
    public static String int2Str(int intValue, int strLength) {
        if (strLength > 0) {
            char padding = '0';
            StringBuilder result = new StringBuilder();
            String fromStr = String.valueOf(intValue);
            for (int i = fromStr.length(); i < strLength; i++) {
                result.append(padding);
            }
            result.append(fromStr);
            return result.toString();
        } else {
            return null;
        }
    }

    /**
     * 生成32位编码
     * 
     * @return string
     */
    public static String getUUID(){
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

    /**
     * 生成6位编码
     * 
     * @return string
     */
    public static String getRandomNum(){
        String code ="";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code += random.nextInt(10);
        }
        return code;
    }

    /**
     * 随机指定范围内N个不重复的数
     * 
     * @param min
     *            指定范围最小值
     * @param max
     *            指定范围最大值
     * @param n
     *            随机数个数
     */
    public static int[] randomCommon(int min, int max, int n){
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while(count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if(num == result[j]){
                    flag = false;
                    break;
                }
            }
            if(flag){
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    /**
     * 银行卡
     * 
     * @param code
     * @return
     */
    public static String formatCardCode(String code){
        return  "************"+code.substring(code.length()-4, code.length());
    }

    /**
     * 身份证加*
     * 
     * @param code
     * @return
     */
    public static String formatCertificatesCode(String code){
        return  code.substring(0, 3)+"************"+code.substring(code.length()-4, code.length());
    }

    /**
     * 手机加*
     * 
     * @param telephone
     * @return
     */
    public static String formatTelephone(String telephone){
        return  telephone.substring(0, 3)+"*****"+telephone.substring(telephone.length()-4, telephone.length());
    }

    /**
     * @Description: 传入jsonStr 去除当中的空格
     * @param jsonStr
     * @return
     * @exception:(异常说明)
     */
    public static String jsonStrTrim(String jsonStr){

        JSONObject reagobj = JSON.parseObject(jsonStr);
        // 取出 jsonObject 中的字段的值的空格
        Iterator itt = reagobj.keySet().iterator();

        while (itt.hasNext()) {

            String key = itt.next().toString();
            String value = reagobj.getString(key);

            if(value == null){
                continue ;
            }else if("".equals(value.trim())){
                continue ;
            }else{
                reagobj.put(key, value.trim());
            }
        }
        return reagobj.toJSONString();
    }

    /**
     * 过滤特殊字符
     * 
     * @param str
     *            文本字符串
     * @return
     */
    public static String StrFilter(String str){
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？-]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return  m.replaceAll("").trim();
    }
    
    /**
     * 替换回车换行符
     * @param str
     * @return
     */
    public static String replaceCarriageReturnLine(String str) {
		String dest = "";
		if (!isEmpty(str)) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

    /**
     * 过滤特殊字符
     * 
     * @param dateStr
     *            日期字符转
     * @return
     */
    public static String DateStrFilter(String dateStr){
        String regEx = "[`~!@#$%^&*()+=|{}':;',.<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";// 注：允许‘/’和‘-’通过。
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(dateStr);
        return  m.replaceAll("").trim();
    }

    public static boolean matcherStr(String dateStr,String pattern){
    	boolean result=false;
    	if(dateStr!=null){
    		Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(dateStr);
    		result=m.matches();
    	}
    	return result;
    }

    /**
     * 过滤特殊字符
     * 
     * @param text
     *            文本字符串
     * @return
     */
    public static String TextFilter(String text){
        String regEx="[<>]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        return  m.replaceAll("").trim();
    }

    /**
     * 判断是否有特殊字符
     * 
     * @param str
     * @return true 有，false 没有
     */
    public static boolean isSpecialCharacters(String str){
        str = str.trim();
        return !str.equals(StrFilter(str));
    }

    /**
     * 判断是否有特殊字符
     * 
     * @param str
     * @return true 有，false 没有
     */
    public static boolean isSpecialText(String str){
        str = str.trim();
        return !str.equals(TextFilter(str));
    }



    /**
     * 生成批次号
     * 规则：(六位时分秒+三位毫秒+一位随机数)[10位]
     * @return
     */
    public static String generateBatchCode() {
        StringBuffer code = new StringBuffer();
        Calendar calendar = null;
        calendar =new GregorianCalendar();
        String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
        String minute = calendar.get(Calendar.MINUTE) + "";
        String second = calendar.get(Calendar.SECOND) + "";
        String mills = String.valueOf(System.currentTimeMillis());

        String threeMill = mills.substring(mills.length()-4, mills.length()-1);
        code.append(hour + minute + second + threeMill);

        Random random = new Random();
        code.append(random.nextInt(10));

        return code.toString();
    }
    
    public static String toString(Object o){
        return o==null?"":o.toString();
    }


    /**
     * 判断字符串s是否包含在数组中
     *
     * @param s
     * @param array
     * @return
     * @throws Exception
     */
    public static boolean isInArray(String s, String[] array) {
        boolean b = false;
        if (s == null)
            return b;
        try {
            for (int i = 0; array != null && i < array.length; i++) {
                if (s.equals(array[i]))
                    return true;
            }
        } catch (Exception e) {
        	logger.error("StringUtil   isInArray方法出错，异常信息为:",e); 
        }
        return b;
    }


    /**
     * 按要求分割字符串.
     *
     */
    public static String[] splitString(String srcStr, String splitter) {
        if (srcStr == null) return new String[]{""};
        String[] tmpArr = srcStr.split(splitter);
        if (tmpArr == null || tmpArr.length == 0) {
            return new String[]{""};
        } else {
            for (int index = 0; index < tmpArr.length; index++) {
                tmpArr[index] = tmpArr[index].trim();
            }
            return tmpArr;
        }
    }



    /**
     * 将字符串转换为UTF-8格式
     * @param str
     * @return
     */
    public static String getUTF8String(String str) {
        if (isEmpty(str)) {
            return null;
        } else {
            try {
                return new String(str.getBytes() ,"utf-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
    }
    
    /**
	 * 去除换行和回车符号
	 * @param str
	 * @return
	 */
    public static String reaplaceStr(String str){
		if(StringUtil.isEmpty(str)){
			return str;
		}
		Pattern p = Pattern.compile("\t|\r|\n|\"|'");
	    Matcher m = p.matcher(str);
	    return m.replaceAll("");
	}

    /**
     * 统计字符串中文个数
     * @param str
     * @return
     */
	public static  int getStringNum(String str) {
        int count = 0;
        char[] c = str.toCharArray();
        for(int i = 0; i < c.length; i ++)
        {
            String len = Integer.toBinaryString(c[i]);
            if(len.length() > 8)
                count ++;
        }
        return count;
    }
	
	/**
	 * 过滤字符utf8mb4，如表情符号
	 * @author csn V1.0 2017年12月22日 上午11:42:36
	 * @param str
	 * @return String
	 */
	public static String filter(String str) {
		if (str == null) {
			return null;
		}
		if (str.trim().equals("")) {
			return str;
		}
		String pattern = "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]";
		String reStr = "";
		Pattern emoji = Pattern.compile(pattern);
		Matcher emojiMatcher = emoji.matcher(str);
		str = emojiMatcher.replaceAll(reStr);
		return reaplaceStr(str);
	}
}

