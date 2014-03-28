/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import java.util.HashMap;

/**
 *
 * @author drew
 */
public class InstanceResults {
    private int Status;
    private String msg;
    private HashMap<String,TestInstance> data;

    public HashMap<String, TestInstance> getTests() {
        return data;
    }
}
