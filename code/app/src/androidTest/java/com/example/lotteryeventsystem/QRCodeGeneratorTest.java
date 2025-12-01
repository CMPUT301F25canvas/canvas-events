package com.example.lotteryeventsystem;

import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4; // Correct runner for instrumented tests

import org.junit.Test;
import org.junit.runner.RunWith;

// The @RunWith annotation is crucial and already correct.
@RunWith(AndroidJUnit4.class)
public class QRCodeGeneratorTest {
    @Test
    public void testGenerateQRCode() {
        String eventID = "event123";
        // This method can now run because it has access to the full Android framework
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(eventID);

        assertNotNull(qrCodeBitmap);
    }
}
