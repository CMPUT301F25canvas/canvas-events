package com.example.lotteryeventsystem;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;


public class Event {
    private String name;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private Integer entrantLimit;
    private String QRCodeURL;

    public Event(String name, String description,
                 String date, String startTime, String endTime) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entrantLimit = null; // Set null if no limit
    }

    public Event(String name, String description,
                 String date, String startTime, String endTime,
                 Integer entrantLimit) {
        this(name, description, date, startTime, endTime);
        this.entrantLimit = entrantLimit;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getEntrantLimit() {
        return entrantLimit;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setEntrantLimit(Integer entrantLimit) {
        this.entrantLimit = entrantLimit;
    }

    public Bitmap GenerateQRCode(String content) {
        /**
         * Function to
         */
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512; // pixels
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
