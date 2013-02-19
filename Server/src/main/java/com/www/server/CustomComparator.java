/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.www.server;

import java.io.File;
import java.util.Comparator;

public class CustomComparator implements Comparator<File> {

    @Override
    public int compare(File firstString, File secondString) {
        int num1 = Integer.parseInt(firstString.getName());
        int num2 = Integer.parseInt(secondString.getName());
        return num1 - num2;
    }
}
