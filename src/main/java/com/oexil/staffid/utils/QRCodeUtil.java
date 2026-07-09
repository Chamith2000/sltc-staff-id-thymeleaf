package com.oexil.staffid.utils;

import com.google.zxing.EncodeHintType;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeUtil {

    public static byte[] generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Configure encoding hints to set margin
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 2); // Set margin to 1 module (minimal white space)
            // Alternatively, use 0 for no margin: hints.put(EncodeHintType.MARGIN, 0);

            // Encode QR code with specified dimensions and hints
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Create a BufferedImage with transparency
            BufferedImage qrImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = qrImage.createGraphics();

            // Set transparent background
            graphics.setColor(new Color(255, 255, 255, 0)); // Fully transparent
            graphics.fillRect(0, 0, 200, 200);

            // Set foreground color (QR code pixels)
            graphics.setColor(Color.BLACK);

            // Draw QR code pixels
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // Convert BufferedImage to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
