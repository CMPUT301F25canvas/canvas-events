package com.example.lotteryeventsystem;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Class to generate thr QR code for newly created event
 */
public class QRCodeGenerator {
// TODO: incomplete class
    /**
     *
     * @param eventID eventID of the event to link the QR code to
     * @return QRCode Bitmap
     */
    public static Bitmap generateQRCode(String eventID) {
        String text = "app://lotteryeventsystem/event?eventID=" + eventID;

        QRCodeWriter writer = new QRCodeWriter();
        int size = 512;
        try {
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
