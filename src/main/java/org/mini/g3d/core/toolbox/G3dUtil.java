/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.core.toolbox;

/**
 *
 * @author Gust
 */
public class G3dUtil {

    /**
     * 在src中搜索key
     *
     * @param src
     * @param key
     * @return
     */
    static public int binSearch(byte[] src, byte[] key, int startPos) {
        if (src == null || key == null || src.length == 0 || key.length == 0 || startPos >= src.length) {
            return -1;
        }
        for (int i = startPos, iLen = src.length - key.length; i <= iLen; i++) {
            if (src[i] == key[0]) {
                boolean march = true;
                for (int j = 1; j < key.length; j++) {
                    if (src[i + j] != key[j]) {
                        march = false;
                    }
                }
                if (march) {
                    return i;
                }
            }
        }
        return -1;
    }

}
