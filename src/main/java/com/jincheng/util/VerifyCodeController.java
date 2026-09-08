package com.jincheng.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/verifyCode")
public class VerifyCodeController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int WIDTH = 132;
    private static final int HEIGHT = 46;
    private static final int CODE_LENGTH = 4;

    @GetMapping
    public void getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");

        String code = createCode();
        HttpSession session = request.getSession();
        session.setAttribute("verifyCode", code);

        BufferedImage image = drawCodeImage(code);
        ImageIO.write(image, "png", response.getOutputStream());
    }

    private String createCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    private BufferedImage drawCodeImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setPaint(new GradientPaint(0, 0, new Color(245, 247, 250), WIDTH, HEIGHT, new Color(220, 236, 232)));
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 10, 10);

        drawNoise(g);
        drawText(g, code);

        g.dispose();
        return image;
    }

    private void drawNoise(Graphics2D g) {
        g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 5; i++) {
            g.setColor(randomSoftColor(95, 160));
            int y = 8 + RANDOM.nextInt(HEIGHT - 16);
            int x2 = WIDTH - RANDOM.nextInt(18);
            g.drawLine(RANDOM.nextInt(16), y, x2, y + RANDOM.nextInt(13) - 6);
        }

        for (int i = 0; i < 34; i++) {
            g.setColor(randomSoftColor(130, 210));
            int size = 1 + RANDOM.nextInt(3);
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), size, size);
        }
    }

    private void drawText(Graphics2D g, String code) {
        Font font = new Font("Arial", Font.BOLD, 30);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int charWidth = WIDTH / CODE_LENGTH;

        for (int i = 0; i < code.length(); i++) {
            String text = String.valueOf(code.charAt(i));
            int x = i * charWidth + (charWidth - metrics.stringWidth(text)) / 2 + 2;
            int y = 33 + RANDOM.nextInt(5) - 2;
            int angle = RANDOM.nextInt(18) - 9;

            g.rotate(Math.toRadians(angle), x + 8, y - 10);
            g.setColor(randomStrongColor());
            g.drawString(text, x, y);
            g.rotate(Math.toRadians(-angle), x + 8, y - 10);
        }
    }

    private Color randomStrongColor() {
        Color[] colors = {
                new Color(31, 41, 55),
                new Color(17, 94, 89),
                new Color(29, 78, 216),
                new Color(126, 34, 206)
        };
        return colors[RANDOM.nextInt(colors.length)];
    }

    private Color randomSoftColor(int min, int max) {
        int red = min + RANDOM.nextInt(max - min);
        int green = min + RANDOM.nextInt(max - min);
        int blue = min + RANDOM.nextInt(max - min);
        return new Color(red, green, blue);
    }
}
