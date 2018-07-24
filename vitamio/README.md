Vitamio
===============

This folder contains the main library which should be linked against as an
Android library project in your application.

2018/4/23
    问题描述：视频缩略图上下边有黑边，视频播放上下边有黑边，未占满全屏
    解决方案：视频分辨率问题，更换视频之后不会出现这类问题

    问题描述：音轨切换出现没有声音，无法自动恢复，甚至长时间出现屏幕卡死，无响应
    解决方案：弃用vitamio功能的播放器，改用系统原生，并实现音轨切换，不过视频播放还是借用vitamio的实现原理



