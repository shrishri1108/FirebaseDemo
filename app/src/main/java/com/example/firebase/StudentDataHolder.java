package com.example.firebase;

public class StudentDataHolder {
    private String name;
    private String age;
    private String imgLinks;
    private String contact_no;
    private String password;

    public StudentDataHolder(String name, String age, String contact_no, String imgLinks, String password) {
        this.name = name;
        this.age = age;
        this.contact_no = contact_no;
        this.password = password;
        this.imgLinks=imgLinks;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getContact_no() {
        return contact_no;
    }

    public String getPassword() {
        return password;
    }

    public String getImgLinks() {
        return imgLinks;
    }
}
