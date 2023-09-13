package tools;

import org.mini.glwrap.GLUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Test {


    public static void main(String[] args) {


        System.out.println("Test cl:" + Test.class.getClassLoader());

        byte[][] b = new byte[4][];
        System.out.println("byte[][] cl:" + byte[][].class.getClassLoader());
        Test[] t1 = new Test[2];
        System.out.println("Test[] cl:" + Test[].class.getClassLoader());

        Date d = new Date(1654146146089L);
        Date d1 = new Date(1654146744700L);
        System.out.println(d);
        System.out.println(d1);

        B bins = new B();
        bins.m();
        bins = null;
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("WuQi_Ren_Nv_Cike".matches("WuQi_.*"));

//        String s1 = "\uD83C\uDF1E";//🌞
//        String s1 = "\uE788";
//        String s1 = "中";
        String s1 = "\uE4AD";
//        String s1 = "简体中文";
        int cp = s1.codePointAt(0);
        System.out.println("codepoint: " + Integer.toHexString(cp));
        try {
            byte[] b1 = s1.getBytes("utf-8");
            for (int i = 0; i < b1.length; i++) System.out.print(Integer.toString(b1[i] & 0xff, 16) + " ");
            System.out.println();
            s1 = new String(b1, "utf-8");
        } catch (Exception e) {
        }
        for (int i = 0; i < s1.length(); i++) System.out.print(Integer.toString(s1.charAt(i), 16) + " ");
        System.out.println();
        {
            byte[] b1 = toUtf8(s1);
            for (int i = 0; i < b1.length; i++) System.out.print(Integer.toString(b1[i] & 0xff, 16) + " ");
            System.out.println();
        }
        //System.out.println("emoji:\uD83C\uDF1E \uE788 \uD83C\uDF09");
    }


    public static byte[] toUtf8(String s) {
        if (s == null) {
            return null;
        } else {
            int pos = s.lastIndexOf(0);
            if (pos < 0 || pos != s.length() - 1) {
                s = s + '\u0000';
            }

            byte[] barr = null;

            try {
                barr = s.getBytes("utf-8");
            } catch (UnsupportedEncodingException var4) {
            }

            return barr;
        }
    }
}

class A {
    protected void finalize() {
        System.out.println("a finalize");
    }
}

class B extends A implements I {

}

interface I {
    default public void m() {
        System.out.println("I m");
    }
}