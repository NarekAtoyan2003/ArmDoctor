package com.armdoctor.util;
import org.apache.commons.lang3.RandomStringUtils;
import javax.swing.*;

public class TokenGenerate {
    public static String generateVerifyCode() {
        return RandomStringUtils.random(6, true, true);
    }
}
