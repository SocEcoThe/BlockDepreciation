package org.cockshott;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockTypeExtractor {

    public static String extractBlockType(String blockDescription) {
        // 定义正则表达式模式来匹配 Block{后面跟着任意字符直到出现}
        String regex = "(?<=data\\=Block\\{)(.*?)(?=\\})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(blockDescription);

        if (matcher.find()) {
            // 如果找到匹配项，matcher.group(1)将返回第一个括号内匹配到的内容
            return matcher.group(1);
        }

        return "Unknown"; // 如果没有找到匹配项，返回一个默认值
    }
}
