package com.example.lotteryeventsystem;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;


public class Event {
    private String id;
    private String creatorId;
    private String name;
    private String description;
    private String date;
    private String start_time;
    private String end_time;
    private Number entrant_limit;
    private String QRCodeURL;

    public Event(String id, String name, String creatorId, String description,
                 String date, String start_time, String end_time) {
        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.entrant_limit = null; // Set null if no limit
    }

    public Event(String id, String name, String creatorId, String description,
                 String date, String start_time, String end_time,
                 Number entrant_limit) {
        this(id, name, creatorId, description, date, start_time, end_time);
        this.entrant_limit = entrant_limit;
    }

    public Event() {
        // Firestore likes a public no-arg constructor.
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatorId() {
        return creatorId;
    }
    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return start_time;
    }

    public String getEndTime() {
        return end_time;
    }

    public Number getEntrantLimit() {
        return entrant_limit;
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

    public void setStartTime(String start_time) {
        this.start_time = start_time;
    }

    public void setEndTime(String end_time) {
        this.end_time = end_time;
    }

    public void setEntrantLimit(Integer entrant_limit) {
        this.entrant_limit = entrant_limit;
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
