package com.crni99.qrcodegenerator.controller;

import java.io.IOException;
import java.util.Base64;

import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.crni99.qrcodegenerator.service.QRCodeService;

@Controller
public class MainController {

	private QRCodeService qrCodeService;

	public MainController(QRCodeService qrCodeService) {
		this.qrCodeService = qrCodeService;
	}

	@GetMapping("/")
	public String home() {
		return "index";
	}

	@PostMapping("/generate")
	public String generateQRCode(@RequestParam("text") String text, Model model,
			RedirectAttributes redirectAttributes) {
		if (text == null || text.isBlank() || text.isEmpty()) {
			return "redirect:/";
		}
		String qrCode = qrCodeService.getQRCode(text);
		model.addAttribute("text", text);
		model.addAttribute("qrcode", qrCode);
		return "index";
	}

	@PostMapping("/generateQRCodePDF")
	public String generateQRCodePDF(@RequestParam("text") String text, Model model) {
		try {
			byte[] pdfData = QRCodeService.getQRCodePDF(text); // Cambia los valores 200 y 200 según el ancho y alto del código QR que necesites
			String qrPdf = Base64.getEncoder().encodeToString(pdfData);
			model.addAttribute("text", text);
			model.addAttribute("qrcode", qrPdf);
			return "index";
		} catch (IOException | WriterException e) {
			// Manejo de errores
			return "error";
		}
	}


	@GetMapping("/decode")
	public String decodeQRCode() {
		return "decode";
	}

	@GetMapping("/downloadQRCodePDF")
	public ResponseEntity<byte[]> downloadQRCodePDF() {
		try {
			String text = "Texto del código QR"; // Reemplaza esto con el texto para generar el código QR
			byte[] pdfData = QRCodeService.getQRCodePDF(text); // Ajusta el tamaño según sea necesario
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.setContentDispositionFormData("filename", "QRCode.pdf");
			headers.setContentLength(pdfData.length);
			return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
		} catch (IOException | WriterException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@PostMapping("/uploadQrCode")
	public String uploadQrCode(@RequestParam("qrCodeFile") MultipartFile qrCodeFile,
			RedirectAttributes redirectAttributes) {
		if (qrCodeFile.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please choose file to upload.");
			return "redirect:/decode";
		}
		try {
			String qrContent = qrCodeService.decodeQR(qrCodeFile.getBytes());
			redirectAttributes.addFlashAttribute("qrContent", qrContent);
			return "redirect:/decode";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/decode";
	}

}
