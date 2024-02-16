package com.crni99.qrcodegenerator.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Hashtable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.*;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QRCodeService {

	private static final int QR_CODE_WIDTH = 200;
	private static final int QR_CODE_HEIGHT = 200;

	public static String getQRCode(String text) {
		try {
			byte[] pdfData = getQRCodePDF(text);
			String qrPdf = Base64.getEncoder().encodeToString(pdfData);
			return qrPdf;
		} catch (IOException | WriterException e) {
			throw new RuntimeException("Error al generar el PDF del código QR", e);
		}
	}

	public static byte[] getQRCodePDF(String text) throws IOException, WriterException {
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(new PDRectangle(200, 200));
			document.addPage(page);

			BitMatrix bitMatrix = generateQRCode(text);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
				PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, qrImage);
				contentStream.drawImage(pdImageXObject, 1, 1, QR_CODE_WIDTH, QR_CODE_HEIGHT);
			}

			document.save(pdfOutputStream);
		}

		return pdfOutputStream.toByteArray();
	}


	private static BitMatrix generateQRCode(String text) throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L);
		return new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, hints);
	}

	// Método para generar el PDF del ticket
	public static byte[] getTicketPDF(String text) throws IOException {
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA, 12);
				contentStream.newLineAtOffset(1, 1);
				contentStream.showText(text);
				contentStream.endText();
			}

			document.save(pdfOutputStream);
		}

		return pdfOutputStream.toByteArray();
	}
	public String decodeQR(byte[] qrCodeBytes) {
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qrCodeBytes);
			BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
			BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(bufferedImage);
			HybridBinarizer hybridBinarizer = new HybridBinarizer(bufferedImageLuminanceSource);
			BinaryBitmap binaryBitmap = new BinaryBitmap(hybridBinarizer);
			MultiFormatReader multiFormatReader = new MultiFormatReader();
			Result result = multiFormatReader.decode(binaryBitmap);
			return result.getText();
		} catch (NotFoundException e) {
			return "QR Code Not Found In This Image!";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
