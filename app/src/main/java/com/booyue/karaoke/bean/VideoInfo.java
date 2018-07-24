package com.booyue.karaoke.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/9/4.18:00
 */

public class VideoInfo implements Parcelable,Comparable<VideoInfo>{

    public String videoName;
    public String videoPath;
    public String imagePath;

//    public String getVideoName() {
//        return videoName;
//    }
//
//    public void setVideoName(String videoName) {
//        this.videoName = videoName;
//    }
//    public String getVideoPath() {
//        return videoPath;
//    }
//
//    public void setVideoPath(String videoPath) {
//        this.videoPath = videoPath;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoName);
        dest.writeString(videoPath);
        dest.writeString(imagePath);

    }

    public static final Parcelable.Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel source) {
            return new VideoInfo(source);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    public VideoInfo(Parcel parcel){
        videoName = parcel.readString();
        videoPath = parcel.readString();
        imagePath = parcel.readString();
    }
    public VideoInfo(){

    }

    @Override
    public int compareTo(@NonNull VideoInfo o) {
        if(HasDigit(o.videoName) && HasDigit(videoName)){
            String numberString = getNumbers(videoName);
            String numberString1 = getNumbers(o.videoName);
            int number = Integer.parseInt(numberString);
            int number1 = Integer.parseInt(numberString1);
            if(number > number1){
                return 1;
            }else if(number == number1){
                return 0;
            }
            else return -1;

        }
        return 0;
    }
    // 判断一个字符串是否含有数字
    public boolean HasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    //截取数字  【读取字符串中第一个连续的字符串，不包含后面不连续的数字】
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }
}
