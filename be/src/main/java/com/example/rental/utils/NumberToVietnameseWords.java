package com.example.rental.utils;

public class NumberToVietnameseWords {

    private static final String[] units = {
            "", "một", "hai", "ba", "bốn", "năm", "sáu",
            "bảy", "tám", "chín"
    };

    private static final String[] tens = {
            "", "mười", "hai mươi", "ba mươi", "bốn mươi",
            "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"
    };

    private static final String[] scales = {
            "", "nghìn", "triệu", "tỷ"
    };

    public static String convert(long number) {
        if (number == 0) {
            return "Không đồng";
        }

        String snumber = Long.toString(number);
        StringBuilder result = new StringBuilder();

        int groupCount = 0;
        while (snumber.length() > 0) {
            int group;
            if (snumber.length() > 3) {
                group = Integer.parseInt(snumber.substring(snumber.length() - 3));
                snumber = snumber.substring(0, snumber.length() - 3);
            } else {
                group = Integer.parseInt(snumber);
                snumber = "";
            }

            if (group != 0) {
                String groupText = convertGroup(group);
                if (!groupText.isEmpty()) {
                    result.insert(0, groupText + " " + scales[groupCount] + " ");
                }
            }
            groupCount++;
        }

        return capitalize(result.toString().trim()) + " đồng chẵn";
    }

    private static String convertGroup(int number) {
        StringBuilder sb = new StringBuilder();

        int hundreds = number / 100;
        int tensUnit = (number % 100) / 10;
        int unitsDigit = number % 10;

        if (hundreds > 0) {
            sb.append(units[hundreds]).append(" trăm ");
        }

        if (tensUnit > 1) {
            sb.append(tens[tensUnit]).append(" ");
            if (unitsDigit > 0) {
                sb.append(units[unitsDigit]);
            }
        } else if (tensUnit == 1) {
            sb.append("mười ");
            if (unitsDigit > 0) {
                sb.append(units[unitsDigit]);
            }
        } else {
            if (unitsDigit > 0) {
                sb.append(units[unitsDigit]);
            }
        }

        return sb.toString().trim();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
