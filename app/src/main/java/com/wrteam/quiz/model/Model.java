package com.wrteam.quiz.model;


public class Model {
    public String desc,date,coin;
    public String id,title,message,image,datesent;
    public String name,start_date,end_date,description,entry,top_users,points,date_created,participants,rank,userid,score,type,scheduled;
    public boolean adsShow;
    public Model() {
    }

    public Model(String top_users, String points) {
        this.top_users = top_users;
        this.points = points;
    }

    public Model(String desc, String date, String coin) {
        this.desc = desc;
        this.date = date;
        this.coin = coin;
    }

    public Model(String id, String title, String message, String image, String datesent, String type, String scheduled) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.image = image;
        this.datesent = datesent;
        this.type = type;
        this.scheduled = scheduled;
    }

    public Model(String rank, String userid, String name, String score) {
        this.rank = rank;
        this.userid = userid;
        this.name = name;
        this.score = score;
    }

    public Model(String rank, String userid, String name, String score, String image) {
        this.rank = rank;
        this.userid = userid;
        this.name = name;
        this.score = score;
        this.image = image;
    }

    public Model(String id, String name, String start_date, String end_date, String description, String image, String entry, String top_users, String points, String date_created, String participants, String type) {
        this.id = id;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.image = image;
        this.entry = entry;
        this.top_users = top_users;
        this.points = points;
        this.date_created = date_created;
        this.participants = participants;
        this.type = type;
    }

    public Model(boolean adsShow) {
        this.adsShow = adsShow;
    }

    public boolean isAdsShow() {
        return adsShow;
    }

    public void setAdsShow(boolean adsShow) {
        this.adsShow = adsShow;
    }
    public String getType() {
        return type;
    }

    public String getRank() {
        return rank;
    }

    public String getUserid() {
        return userid;
    }

    public String getScore() {
        return score;
    }

    public String getParticipants() {
        return participants;
    }

    public String getName() {
        return name;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public String getDescription() {
        return description;
    }

    public String getEntry() {
        return entry;
    }

    public String getTop_users() {
        return top_users;
    }

    public String getPoints() {
        return points;
    }

    public String getDate_created() {
        return date_created;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getImage() {
        return image;
    }

    public String getDatesent() {
        return datesent;
    }

    public String getDesc() {
        return desc;
    }

    public String getDate() {
        return date;
    }

    public String getCoin() {
        return coin;
    }

}
