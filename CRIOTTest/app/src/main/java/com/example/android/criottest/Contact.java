package com.example.android.criottest;

public class Contact {
    String _ssid;
    String _pass;
    String _phone_number;

    public Contact(){

    }
    public Contact(String name, String _phone_number){
        this._ssid = name;
        this._pass = _phone_number;
    }
    public String getID(){
        return this._ssid;
    }

    public void setID(String id){
        this._ssid = id;
    }

    public String getName(){
        return this._pass;
    }

    public void setName(String name){
        this._pass = name;
    }

    public String getPhoneNumber(){
        return this._phone_number;
    }

    public void setPhoneNumber(String phone_number){
        this._phone_number = phone_number;
    }
}