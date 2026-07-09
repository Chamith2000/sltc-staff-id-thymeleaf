package com.oexil.staffid.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PngGenerationService {

    @Value("${server.port:8091}")
    private String serverPort;

    @Value("${png.output.directory:${java.io.tmpdir}/signature-png}")
    private String outputDirectory;

    public String generateSignaturePng(Long employeeId, String employeeName, String employeeNo) throws Exception {
        
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(outputDirectory);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Generate filename
        String filename = String.format("%s_%s_signature.png", 
            employeeName.replaceAll("\\s+", "_"), employeeNo);
        Path outputPath = outputDir.resolve(filename);

        // Build the Node.js Puppeteer script command
        String url = "http://localhost:" + serverPort + "/staff/generate-eSignature/" + employeeId;
        
        String script = String.format("""
            const puppeteer = require('puppeteer');
            
            (async () => {
              const browser = await puppeteer.launch({
                headless: true,
                args: [
                  '--no-sandbox',
                  '--disable-setuid-sandbox',
                  '--disable-dev-shm-usage',
                  '--disable-gpu',
                  '--disable-web-security',
                  '--force-device-scale-factor=3'
                ]
              });
              
              const page = await browser.newPage();
              
              // Set viewport to match card dimensions with high DPI
              await page.setViewport({
                width: 1200,
                height: 800,
                deviceScaleFactor: 3
              });
              
              try {
                console.log('Navigating to: %s');
                await page.goto('%s', { 
                  waitUntil: 'networkidle0',
                  timeout: 30000
                });
                
                // Wait for fonts to load with error handling
                try {
                  await page.evaluate(() => {
                    return Promise.race([
                      Promise.all([
                        document.fonts.load('12px Lexend').catch(() => console.log('Lexend 12px failed')),
                        document.fonts.load('27pt Lexend').catch(() => console.log('Lexend 27pt failed')),
                        document.fonts.load('14pt Lexend').catch(() => console.log('Lexend 14pt failed')),
                        document.fonts.load('12px MuseoSans').catch(() => console.log('MuseoSans failed')),
                        document.fonts.load('12px "Open Sans"').catch(() => console.log('Open Sans failed'))
                      ]).catch(() => console.log('Some fonts failed to load')),
                      new Promise(resolve => setTimeout(resolve, 2000)) // Timeout after 2 seconds
                    ]);
                  });
                } catch (fontError) {
                  console.log('Font loading failed, continuing anyway:', fontError.message);
                }
                
                // Additional wait for font rendering
                await new Promise(resolve => setTimeout(resolve, 3000));
                
                // Wait for signature card element
                await page.waitForSelector('.id-card', { timeout: 10000 });
                
                // Get element for screenshot
                const element = await page.$('.id-card');
                
                if (!element) {
                  throw new Error('Signature card element not found');
                }
                
                // Take screenshot of the element
                await element.screenshot({
                  path: '%s',
                  type: 'png'
                });
                
                console.log('PNG generated successfully: %s');
                
              } catch (error) {
                console.error('Error:', error);
                process.exit(1);
              } finally {
                await browser.close();
              }
            })();
            """, url, url, outputPath.toString(), outputPath.toString());

        // Write the script to a temporary file
        Path scriptPath = Files.createTempFile("puppeteer-script", ".js");
        Files.write(scriptPath, script.getBytes());

        try {
            // Execute the Node.js script from project root where node_modules/puppeteer is installed
            String projectRoot = System.getProperty("user.dir");
            ProcessBuilder pb = new ProcessBuilder("node", scriptPath.toString());
            pb.directory(new File(projectRoot));
            pb.environment().put("NODE_PATH", projectRoot + "/node_modules");
            
            // Redirect error stream to capture error output
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Capture output for debugging
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Puppeteer output: {}", line);
                }
            }
            
            // Wait for completion with timeout
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Puppeteer script timed out");
            }
            
            if (process.exitValue() != 0) {
                log.error("Puppeteer script failed with output: {}", output.toString());
                throw new RuntimeException("Puppeteer script failed with exit code: " + process.exitValue() + 
                    ". Output: " + output.toString());
            }
            
            // Verify the file was created
            if (!Files.exists(outputPath)) {
                throw new RuntimeException("PNG file was not created");
            }
            
            log.info("PNG generated successfully using Puppeteer: {}", outputPath);
            return outputPath.toString();
            
        } finally {
            // Clean up the temporary script file
            try {
                Files.deleteIfExists(scriptPath);
            } catch (IOException e) {
                log.warn("Could not delete temporary script file: {}", scriptPath, e);
            }
        }
    }
    
    public String generateSignaturePngWithClipping(Long employeeId, String employeeName, String employeeNo) throws Exception {
        log.info("Starting PNG generation for employee ID: {}, name: '{}', empNo: '{}'", employeeId, employeeName, employeeNo);
        
        // Validate input parameters
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee number cannot be null or empty");
        }
        
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(outputDirectory);
        if (!Files.exists(outputDir)) {
            log.info("Creating output directory: {}", outputDir);
            Files.createDirectories(outputDir);
        }

        // Generate filename (sanitize input)
        String sanitizedName = employeeName.trim().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
        String sanitizedEmpNo = employeeNo.trim().replaceAll("[^a-zA-Z0-9]", "");
        String filename = String.format("%s_%s_signature.png", sanitizedName, sanitizedEmpNo);
        Path outputPath = outputDir.resolve(filename);
        
        log.info("Generated filename: {}, output path: {}", filename, outputPath);

        // Build the Node.js Puppeteer script for high-quality capture
        String url = "http://localhost:" + serverPort + "/staff/generate-eSignature/" + employeeId;
        
        String script = String.format("""
            const puppeteer = require('puppeteer');
            
            (async () => {
              const browser = await puppeteer.launch({
                headless: true,
                args: [
                  '--no-sandbox',
                  '--disable-setuid-sandbox',
                  '--disable-dev-shm-usage',
                  '--disable-gpu',
                  '--disable-web-security',
                  '--force-device-scale-factor=3',
                  '--disable-features=VizDisplayCompositor'
                ]
              });
              
              const page = await browser.newPage();
              
              // Set high DPI viewport for pixel-perfect capture
              await page.setViewport({
                width: 1400,
                height: 1000,
                deviceScaleFactor: 3
              });
              
              try {
                console.log('Loading page: %s');
                await page.goto('%s', { 
                  waitUntil: 'networkidle0',
                  timeout: 45000
                });
                
                // Load fonts with error handling
                try {
                  await page.evaluate(() => {
                    return Promise.race([
                      Promise.all([
                        document.fonts.load('12px Lexend').catch(() => console.log('Lexend 12px failed')),
                        document.fonts.load('27pt Lexend').catch(() => console.log('Lexend 27pt failed')),
                        document.fonts.load('14pt Lexend').catch(() => console.log('Lexend 14pt failed')),
                        document.fonts.load('12px MuseoSans').catch(() => console.log('MuseoSans failed')),
                        document.fonts.load('12px "Open Sans"').catch(() => console.log('Open Sans failed'))
                      ]).catch(() => console.log('Some fonts failed to load')),
                      new Promise(resolve => setTimeout(resolve, 3000)) // Timeout after 3 seconds
                    ]);
                  });
                } catch (fontError) {
                  console.log('Font loading failed, continuing anyway:', fontError.message);
                }
                
                // Extended wait for complete font rendering
                await new Promise(resolve => setTimeout(resolve, 5000));
                
                // Wait for signature card element to be fully loaded
                await page.waitForSelector('.id-card', { 
                  timeout: 15000,
                  visible: true 
                });
                
                // Wait for all images and CSS to be fully rendered
                await page.evaluate(() => {
                  return new Promise((resolve) => {
                    if (document.readyState === 'complete') {
                      setTimeout(resolve, 2000);
                    } else {
                      window.addEventListener('load', () => setTimeout(resolve, 2000));
                    }
                  });
                });
                
                // Get the signature card element
                const element = await page.$('.id-card');
                
                if (!element) {
                  throw new Error('Signature card element (.id-card) not found');
                }
                
                // Take pixel-perfect screenshot of just the card
                await element.screenshot({
                  path: '%s',
                  type: 'png',
                  omitBackground: false
                });
                
                console.log('Pixel-perfect PNG generated: %s');
                
              } catch (error) {
                console.error('Puppeteer error:', error);
                process.exit(1);
              } finally {
                await browser.close();
              }
            })();
            """, url, url, outputPath.toString(), outputPath.toString());

        // Write the script to a temporary file
        Path scriptPath = Files.createTempFile("puppeteer-hq-script", ".js");
        Files.write(scriptPath, script.getBytes());

        try {
            // Execute the Node.js script from project root
            String projectRoot = System.getProperty("user.dir");
            log.info("Executing Puppeteer script from project root: {}", projectRoot);
            log.info("Target URL: {}", url);
            
            ProcessBuilder pb = new ProcessBuilder("node", scriptPath.toString());
            pb.directory(new File(projectRoot));
            pb.environment().put("NODE_PATH", projectRoot + "/node_modules");
            
            // Redirect error stream to capture error output
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Capture output for debugging
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Puppeteer output: {}", line);
                }
            }
            
            // Extended timeout for high-quality generation
            boolean finished = process.waitFor(90, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("High-quality Puppeteer script timed out");
            }
            
            if (process.exitValue() != 0) {
                log.error("Puppeteer script failed with output: {}", output.toString());
                throw new RuntimeException("High-quality Puppeteer script failed with exit code: " + process.exitValue() + 
                    ". Output: " + output.toString());
            }
            
            // Verify the file was created
            if (!Files.exists(outputPath)) {
                throw new RuntimeException("PNG file was not created");
            }
            
            log.info("High-quality PNG generated successfully using Puppeteer: {}", outputPath);
            return outputPath.toString();
            
        } finally {
            // Clean up the temporary script file
            try {
                Files.deleteIfExists(scriptPath);
            } catch (IOException e) {
                log.warn("Could not delete temporary script file: {}", scriptPath, e);
            }
        }
    }
}