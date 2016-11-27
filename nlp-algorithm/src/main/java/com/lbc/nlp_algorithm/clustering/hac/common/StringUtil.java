package com.lbc.nlp_algorithm.clustering.hac.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 字符串工具类
 *
 * @author Sid
 *
 */
public final class StringUtil {
    public static final String UNIQUE_STRING = "어";
    public static final String SPECIAL_STRING = "Ю";
    public static final String SPLIT_STRING = "Θ";

    public static final char[] MOOD_WORDS = {'吧', '阿','啊','哇','呀','吗','呢','了', '咯', '啦', '么', '嘛', '喽', '咧', '呗'};

    private StringUtil() {
        throw new IllegalArgumentException("不能实例化StringUtil");
    }

    public static String doubleFormat(double d) {
        DecimalFormat df = new DecimalFormat("#########.###");
        return df.format(d);
    }

    /**
     * 标准化字符串
     *
     * @param string
     *            字符串
     * @return 标准的字符串
     */
    // 由于缺少繁体转简体的包，暂时把该功能去掉了
    public static String getStandardString(final String string) {
        return StringUtils.isEmpty(string) ? StringUtils.EMPTY : qBchange(
                trim(string).toLowerCase()).trim();
    }

    /**
     * 返回Pascal化的字符串，即将字符串的首字母大写
     *
     * @param string
     *            字符串
     * @return 首字母大写的字符串
     */
    public static String getPascalString(final String string) {
        return StringUtils.capitalize(string);
    }

    /**
     * 判断字符是否为中文字符
     *
     * @param ch
     *            字符
     * @return true则是中文字符，false不是中文字符
     */
    public static boolean isChineseCharacter(final char ch) {
        String string = String.valueOf(ch);
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(string).find();
    }

    /**
     * 把pascal化的字符的首字母变小写
     *
     * @param string
     *            字符串
     * @return 去pascal化的字符串
     */
    public static String getUnPascal(final String string) {
        return StringUtils.uncapitalize(string);
    }

    /**
     * 把字符串列用空格连接成一个字符串
     *
     * @param strings
     *            字符串列
     * @return 字符串
     */
    public static String getStringFromStrings(final String[] strings) {
        return getStringFromStrings(strings, " ");
    }

    public static String getStringFromStringsWithUnique(final String[] strings) {
        return getStringFromStrings(strings, UNIQUE_STRING);
    }

    /**
     * 把字符串列用指定的方式连接成一个字符串
     *
     * @param strings
     *            字符串列
     * @return 字符串
     */
    public static String getStringFromStrings(final String[] strings,
                                              final String spliter) {
        return ArrayUtils.isEmpty(strings) ? StringUtils.EMPTY : StringUtils
                .join(strings, spliter);
    }

    public static String[] getStringsFromString(final String stirng,
                                                final String spliter) {
        return StringUtils.split(spliter);
    }

    /**
     * 去掉字符串中的空格和tab
     *
     * @param string
     *            字符串
     * @return 去掉后的值
     */
    public static String removeWhiteSpace(final String string) {
        String result = StringUtils.EMPTY;
        if (!StringUtils.isEmpty(string)) {
            String regex = "\\u0020|\\u00A0|\\u1680|\\u180E|\\u2002|\\u2003|\\u2004|\\u2005|\\u2006|\\u2007|\\u2008|\\u2009|\\u200A|\\u200B|\\u200C|\\u200D|\\u202F|\\u205F|\\u2060|\\u3000|\\uFEFF";
            result = string.replaceAll(regex, " ").replaceAll(" ", "");
        }
        return result;
    }

    public static String trim(final String str) {
        String regex = "\\u0020|\\u00A0|\\u1680|\\u180E|\\u2002|\\u2003|\\u2004|\\u2005|\\u2006|\\u2007|\\u2008|\\u2009|\\u200A|\\u200B|\\u200C|\\u200D|\\u202F|\\u205F|\\u2060|\\u3000|\\uFEFF";
        return str.replaceAll(regex, " ").trim();
    }

    /**
     * 判断是否为英文或者数字字符串
     *
     * @param string
     *            字符串
     * @return true则是，false则否
     */
    public static boolean isCharOrNumberString(final String string) {
        Pattern pattern = Pattern.compile("[\\w\\-\\_]+");
        Matcher matecher = pattern.matcher(string);
        if (matecher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为英文字符
     *
     * @param ch
     *            字符
     * @return true为英文字符，false则不是
     */
    public static boolean isEnglishCharacter(final char ch) {
        String a = String.valueOf(ch).toLowerCase();
        return a.charAt(0) >= 'a' && a.charAt(0) <= 'z';
    }

    public static boolean isEnglishOrNumberCharacter(final char ch) {
        return isEnglishCharacter(ch) || Character.isDigit(ch);
    }

    public static boolean isNumberCharacter(final char ch) {
        return Character.isDigit(ch);
    }

    public static boolean containsChinese(final String word) {
        if (StringUtils.isNotEmpty(word)) {
            for (int i = 0; i < word.length(); i++) {
                if (isChineseCharacter(word.charAt(i))) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    public static boolean isNotContainsChinese(String word) {
        if (StringUtils.isNotEmpty(word)) {
            for (int i = 0; i < word.length(); i++) {
                if (isChineseCharacter(word.charAt(i))) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    public static boolean containsEnglishOrNumber(String word) {
        if (StringUtils.isNotEmpty(word)) {
            for (int i = 0; i < word.length(); i++) {
                if (isEnglishOrNumberCharacter(word.charAt(i))) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    public static boolean containsEnglishAndNumber(String word) {
        if (StringUtils.isNotEmpty(word)) {
            boolean containEnglishCharacter = false;
            boolean containNumberCharacter = false;
            for (int i = 0; i < word.length(); i++) {
                if (isEnglishCharacter(word.charAt(i))) {
                    containEnglishCharacter = true;
                    break;
                }
            }
            for (int i = 0; i < word.length(); i++) {
                if (isNumberCharacter(word.charAt(i))) {
                    containNumberCharacter = true;
                    break;
                }
            }
            return containEnglishCharacter && containNumberCharacter;
        }

        return false;
    }

    public static boolean isAllChineseCharacter(String word) {
        if (StringUtils.isNotEmpty(word)) {
            for (int i = 0; i < word.length(); i++) {
                if (!isChineseCharacter(word.charAt(i))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * 返回所有的rex在souce串中独立出现的位置 此处独立的意思为英文和数字的两边不能为英文和数字
     *
     * @param source
     *            源字符串
     * @param rex
     *            表达式
     * @return 所有的位置信息
     */
    public static int[] getAllInDependentIndex(String source, String rex) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        if (isCharOrNumberString(rex)) {
            int position = 0;
            while (position < source.length()) {
                int index = source.indexOf(rex, position);
                if (index > -1) {
                    if (index > 0 && index + rex.length() < source.length()) {
                        if (!isEnglishOrNumberCharacter(source
                                .charAt(index - 1))
                                && !isEnglishOrNumberCharacter(source
                                .charAt(index + rex.length()))) {
                            list.add(source.indexOf(rex, position));
                        }
                    } else if (index > 0) {
                        if (!isEnglishOrNumberCharacter(source
                                .charAt(index - 1))) {
                            list.add(source.indexOf(rex, position));
                        }
                    } else if (index + rex.length() < source.length()) {
                        // if (!isEnglishOrNumberCharacter(source.charAt(index
                        // + rex.length()))) {
                        list.add(source.indexOf(rex, position));
                        // }
                    } else if (index + rex.length() == source.length()) {
                        list.add(source.indexOf(rex, position));
                    }
                    position = index + 1;
                } else {
                    break;
                }
            }
        } else {
            return getAllIndex(source, rex);
        }

        int[] ins = new int[list.size()];
        for (int i = 0; i < ins.length; i++) {
            ins[i] = list.get(i);
        }

        return ins;
    }

    /**
     * 返回所有的rex在souce串中出现的位置
     *
     * @param source
     *            源字符串
     * @param rex
     *            表达式
     * @return 所有的位置信息
     */
    public static int[] getAllIndex(String source, String rex) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        int position = 0;
        while (position < source.length()) {
            int index = source.indexOf(rex, position);
            if (index > -1) {
                list.add(source.indexOf(rex, position));
                position = index + 1;
            } else {
                break;
            }
        }

        int[] ins = new int[list.size()];
        for (int i = 0; i < ins.length; i++) {
            ins[i] = list.get(i);
        }

        return ins;
    }

    /**
     * 倒转字符串，输入abc，返回cba
     *
     * @param string
     *            字符串
     * @return 倒转后的值
     */
    public static String reverseString(String string) {
        return StringUtils.isEmpty(string) ? StringUtils.EMPTY : StringUtils
                .reverse(string);
    }

    public static String getNotNullValue(String string) {
        return StringUtils.isEmpty(string) ? StringUtils.EMPTY : string;
    }

    /**
     * 全角转半角
     *
     * @param QJstr
     *            全角字符
     * @return
     */
    public static String qBchange(String QJstr) {
        if (StringUtils.isEmpty(QJstr)) {
            return StringUtils.EMPTY;
        }

        char[] c = QJstr.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    // /**
    // * 繁体汉字转为简体
    // *
    // * @param fanString
    // * 繁体中文
    // * @return
    // */
    // public static String chineseFJChange(String fanString) {
    // if (StringUtils.isEmpty(fanString)) {
    // return StringUtils.EMPTY;
    // }
    // ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
    // return chinesdJF.chineseFan2Jan(fanString);
    // }

    /**
     * 去除字符串中的特殊字符
     *
     * @param str
     *            原始字符串
     * @return 去除特殊字符后的字符串
     */
    public static String removeSpecialChars(String str) {
        if (str == null) {
            return "";
        }
        String s = trim(str);
        // 清除掉所有特殊字符
        String regEx = "[ `~!@#$%^&*()_+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—|{}【】‘；：”“’\"。，、？\\-]";
        Matcher m = null;
        try {
            Pattern p = Pattern.compile(regEx);
            m = p.matcher(s);
        } catch (PatternSyntaxException p) {
            p.printStackTrace();
            if (m == null) {
                throw new NullPointerException(
                        "在removeNotNumber方法中Matcher不能为NULL，请检查");
            }
        }
        return m.replaceAll("").trim();
    }

    /**
     * <字符后有大小写拼音字母或!?/的认为是特殊字符
     *
     * @param str
     * @return true: 包含 false: 不包含
     */
    public static boolean containsSpecialChar(String str) {
        if (str == null) {
            str = "";
        }
        String regex = ".*<[a-zA-Z!?/]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static String getTimeString(String time, int length) {
        String result = "";
        if (time.contains(".")) {
            int dl = time.length() - time.indexOf(".") - 1;
            if (dl > length) {
                result = time.substring(0, time.length() - dl + length);
            }
        }

        return result;
    }

    public static boolean parseBoolean(String bool) {
        return Boolean.parseBoolean(bool);
    }

    public static int getChineseLength(String string) {
        if (StringUtils.isEmpty(string)) {
            return 0;
        } else {
            int length = 0;
            for (char c : string.toCharArray()) {
                if (isChineseCharacter(c)) {
                    length++;
                }
            }

            return length;
        }
    }

    /**
     * 将字符串中的特殊字符替换为" "
     *
     * @param str
     *            原始字符串
     * @return 去除特殊字符后的字符串
     */
    public static String replaceSpecialCharToWhiteChar(String str) {
        // 清除掉所有特殊字符
        // String regEx =
        // "[`@#$%=';,.?<>￥…（）【】‘’“”；，：。、？！+-&|!(){}\\[\\]^\"~*?:\\\\]";
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）\\-—+|{}【】‘；：”“’。，、？\"\\\\]";
        Matcher m = null;
        try {
            Pattern p = Pattern.compile(regEx);
            m = p.matcher(str);
        } catch (PatternSyntaxException p) {
            p.printStackTrace();
            if (m == null) {
                throw new NullPointerException(
                        "在removeNotNumber方法中Matcher不能为NULL，请检查");
            }
        }
        return m.replaceAll(" ").trim();
    }

    /**
     * 移除字符串中非数字字符
     *
     * @param str
     * @return
     */
    public static String removeNotNumber(String str) {
        String regEx = "[^\\d\\.]*";
        Matcher m = null;
        try {
            Pattern p = Pattern.compile(regEx);
            m = p.matcher(str);
        } catch (PatternSyntaxException p) {
            p.printStackTrace();
            if (m == null) {
                throw new NullPointerException(
                        "在removeNotNumber方法中Matcher不能为NULL，请检查");
            }
        }
        return m.replaceAll("").trim();
    }

    /**
     * 1.将特殊字符转换为空格 2.将中文字符与英文字符连接处插入空格
     *
     * @param s
     * @return
     */
    public static String getSplitString(final String s) {
        StringBuilder sb = new StringBuilder();
        String standar = getStandardString(s);
        // String regEx =
        // "[ `~!@#$%^*()=|{}':;,\\[\\]<>/?~！@#￥%…*（）{}【】‘；：”“’。，、？어]";
        // Matcher m = null;
        // try {
        // Pattern p = Pattern.compile(regEx);
        // m = p.matcher(str);
        // } catch (PatternSyntaxException p) {
        // p.printStackTrace();
        // }
        // str = m.replaceAll(" ").trim();

        if (standar.length() <= 1) {
            return standar;
        }
        for (int i = 0; i < standar.length() - 1; i++) {
            char c = standar.charAt(i);
            char n = standar.charAt(i + 1);
            if ((isChineseCharacter(c) && isEnglishOrNumberCharacter(n))
                    || (isEnglishOrNumberCharacter(c) && isChineseCharacter(n))) {
                sb.append(c).append(" ");
            } else {
                sb.append(c);
            }
        }
        sb.append(standar.charAt(standar.length() - 1));
        sb.append(" ").append(standar);

        String[] strArr = StringUtils.split(sb.toString(), "\\s+");
        Set<String> tmpSet = new LinkedHashSet<String>();
        tmpSet.addAll(Arrays.asList(strArr));
        return StringUtils.join(tmpSet, " ");
    }

    /**
     * 根据浮点型的字符串得到浮点型。 如果字符串中包含非“.”的特殊字符采用移除特殊字符操作
     *
     * @param floatStr
     * @return
     */
    public static String getFormatFloat(String floatStr) {
        if (isBlank(floatStr)) {
            return "0.00";
        }
        String str = removeNotNumber(floatStr);
        if (isBlank(floatStr)) {
            return "0.00";
        }
        if (str.endsWith(".")) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.startsWith(".")) {
            str = "0" + str;
        }

        String[] strs = str.split("\\.");
        if (strs.length <= 2) {
            return str;
        }
        return str.replace(".", "").replace(strs[0] + strs[1],
                strs[0] + "." + strs[1]);
    }

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * <>替换为转义字符
     *
     * @param str
     * @return
     */
    public static String replaceTerm(String str) {
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        return str;
    }

    /**
     * 去掉末尾的语气词.
     * @return
     */
    public static String removeTailMoodWord(String str){
        if(str == null || str.length() <= 0){
            return "";
        }

        char tailChar = str.charAt(str.length()-1);
        for(int i=0; i < MOOD_WORDS.length; i++){
            if(tailChar == MOOD_WORDS[i]){
                return str.substring(0, str.length()-1);
            }
        }
        return str;
    }

    /**
     * 搜索预处理，去除特殊字符及末尾英文串.
     * @param question
     * @return
     */
    public static  String preProcess(String question) {
        String result = replaceSpecialCharToWhiteChar(question);
        if(result.matches("^[a-zA-Z]+$")){
            return result;
        }
        result = result.replaceAll("[a-zA-Z]+$", "");
        return result;
    }

    public static boolean isSolrSpecialChar(String str) {
        if (str.matches("^[\\+\\–&||!(){}\\[\\]^”~*?:\\\\]+$"))
            return true;
        return false;
    }

    public static void main(String[] args) {
//		System.out
//				.println(StringUtil
//						.isCharOrNumberString("wodedingd"));

        System.out.println(StringUtil.removeTailMoodWord(StringUtil.removeSpecialChars("请问下单后多久可以发货啊?")));
        List<Integer> tmps = new ArrayList<Integer>();
        tmps.add(1);
        tmps.add(2);
        tmps.add(3);
        tmps.add(4);
        tmps.add(5);
        System.out.println(tmps.subList(0,4));
    }

}

