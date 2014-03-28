/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import com.google.gson.Gson;
import java.util.HashMap;

/**
 *
 * @author drew
 */
public class Results<K> {
    private int status;
    private String msg;
    private HashMap<String,K> data;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public HashMap<String, K> getData() {
        return data;
    }    
}
