package com.bbj.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servlet to handle build and deploy requests from admin dashboard.
 * Runs Maven build, stops Tomcat, copies WAR, and restarts Tomcat.
 */
public class BuildDeployServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        // Check authentication and authorization
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can build and deploy\"}");
            return;
        }
        
        StringBuilder output = new StringBuilder();
        try {
            output.append(timestamp()).append("Starting build and deploy process...\n");
            
            // Step 1: Build with Maven
            output.append(timestamp()).append("Running Maven build...\n");
            String buildOutput = runCommand(new String[]{"mvn", "clean", "package", "-q"}, 
                "c:\\Users\\Buckman\\Desktop\\BBJDigitalChurchManager\\fresh_app");
            output.append(buildOutput);
            
            // Step 2: Check if WAR was created
            String warPath = "c:\\Users\\Buckman\\Desktop\\BBJDigitalChurchManager\\fresh_app\\target\\fresh_app-1.0.0.war";
            if (!Files.exists(Paths.get(warPath))) {
                output.append(timestamp()).append("ERROR: WAR file not created at ").append(warPath).append("\n");
                response.getWriter().print("{\"output\":\"" + escapeJson(output.toString()) + "\"}");
                return;
            }
            output.append(timestamp()).append("✓ WAR created successfully\n");
            
            // Step 3: Stop Tomcat
            output.append(timestamp()).append("Stopping Tomcat...\n");
            String stopOutput = runCommand(new String[]{"taskkill", "/F", "/IM", "java.exe"}, null);
            output.append(timestamp()).append("✓ Tomcat stopped\n");
            Thread.sleep(1000); // Give Tomcat time to shut down
            
            // Step 4: Copy WAR to Tomcat
            output.append(timestamp()).append("Copying WAR to Tomcat webapps...\n");
            String deployTarget = "c:\\apache-tomcat-9.0\\webapps\\fresh_app.war";
            Files.copy(Paths.get(warPath), Paths.get(deployTarget), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            output.append(timestamp()).append("✓ WAR deployed to Tomcat\n");
            
            // Step 5: Start Tomcat
            output.append(timestamp()).append("Starting Tomcat...\n");
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "c:\\apache-tomcat-9.0\\bin\\startup.bat");
            pb.start();
            Thread.sleep(3000); // Wait for Tomcat to start
            output.append(timestamp()).append("✓ Tomcat started\n");
            
            output.append(timestamp()).append("Build and deploy completed successfully!\n");
            response.getWriter().print("{\"output\":\"" + escapeJson(output.toString()) + "\"}");
            
        } catch (Exception e) {
            output.append(timestamp()).append("ERROR: ").append(e.getMessage()).append("\n");
            response.getWriter().print("{\"output\":\"" + escapeJson(output.toString()) + "\",\"error\":\"" 
                + escapeJson(e.toString()) + "\"}");
        }
    }
    
    private String runCommand(String[] command, String workDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workDir != null) {
            pb.directory(new java.io.File(workDir));
        }
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            output.append("Command exited with code ").append(exitCode).append("\n");
        }
        return output.toString();
    }
    
    private String timestamp() {
        return "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ";
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
