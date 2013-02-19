package com.www.client;
import java.io.Serializable;
import android.content.Context;
public class ClientContext implements Serializable {
	private static final long serialVersionUID = 1L;
	int context;
	public ClientContext(int c){
    	this.context = c;
	}
}
