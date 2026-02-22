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
 * In cloud environments (Railway/Heroku), deployment is handled by git push.
 * In local development, provides build instructions.
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
            // Detect environment
            boolean isCloudEnv = System.getenv("RAILWAY_ENVIRONMENT") != null || 
                               System.getenv("DYNO") != null ||
                               System.getenv("HEROKU_DYNO_TYPE") != null;
            
            output.append(timestamp()).append("Build and Deploy Information\n");
            output.append("----------------------------\n");
            
            if (isCloudEnv) {
                // Cloud environment (Railway, Heroku, etc.)
                output.append(timestamp()).append("✓ Cloud environment detected\n");
                output.append(timestamp()).append("Deployment is automatic via Git push:\n");
                output.append("  1. Commit your changes: git add . && git commit -m \"your message\"\n");
                output.append("  2. Push to main: git push\n");
                output.append("  3. Cloud platform automatically builds and deploys\n");
                output.append(timestamp()).append("To view logs: Check your cloud platform dashboard\n");
                response.getWriter().print("{\"status\":\"running_on_cloud\",\"output\":\"" + escapeJson(output.toString()) + "\"}");
            } else {
                // Local development
                output.append(timestamp()).append("✓ Local development environment\n");
                output.append(timestamp()).append("Running Maven build...\n");
                
                String buildOutput = runCommand(new String[]{"mvn", "clean", "package", "-q"});
                output.append(buildOutput);
                
                // Check if WAR was created
                if (Files.exists(Paths.get("target/fresh_app-1.0.0.war"))) {
                    output.append(timestamp()).append("✓ WAR created successfully\n");
                    output.append(timestamp()).append("WAR is ready for local deployment\n");
                    output.append(timestamp()).append("To restart local Tomcat:\n");
                    output.append("  1. Stop Tomcat: taskkill /F /IM java.exe\n");
                    output.append("  2. Copy WAR to Tomcat webapps\n");
                    output.append("  3. Restart Tomcat\n");
                } else {
                    output.append(timestamp()).append("ERROR: WAR file not created\n");
                }
                
                response.getWriter().print("{\"status\":\"local_build\",\"output\":\"" + escapeJson(output.toString()) + "\"}");
            }
            
        } catch (Exception e) {
            output.append(timestamp()).append("ERROR: ").append(e.getMessage()).append("\n");
            response.getWriter().print("{\"error\":\"" + escapeJson(e.toString()) + "\"}");
        }
    }
    
    
    private String runCommand(String[] command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
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
            output.append(timestamp()).append("Command exited with code ").append(exitCode).append("\n");
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
