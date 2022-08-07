package com.godot.community;

import java.io.IOException;

public class WkTest {

    public static void main(String[] args) {
        String cmd = "d:/work/JAVA/data/wkhtmltox/bin/wkhtmltoimage.exe --quality 75 https://www.nowcoder.com D:\\work\\JAVA\\data\\wkhtmltox\\wk-images\\2.png";

        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
