package com.www.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.FilenameUtils;

@Path("/file")
public class FileService {

    String USERNAME = "root";
    String PASSWORD = "wwwwww";
    String USERS_INFO = "jdbc:mysql://localhost:3306/users_info";
    String DB = "/var/lib/tomcat6/webapps/Server/DB";
    //String DB = "C:/Users/Manos/Desktop/Login";
    //String DX = "/opt/android-sdk/platform-tools/dx";
    //String DX = "C:/Program Files/Android/sdk/platform-tools/dx";
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "File service is ready.";
    }

    @GET
    @Path("download")
    public Response downloadFile() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String url = getFileUrl();
        ResponseBuilder response = Response.ok(null);
        if (url != null && !url.isEmpty()) {
            File file = returnFileFrom(url, ".java");
            String className = FilenameUtils.removeExtension(file.getName());
            file = returnFileFrom(url, ".zip");
            String fileName = FilenameUtils.removeExtension(file.getName());
            System.out.println("Downloading... FileName: " + fileName + " - ClassName: " + className);
            response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"" + "classname=\"" + className + "\"");
        }
        return response.build();
    }

    @GET
    @Path("check_download/{id}_{size}")
    public Response checkDownloadFile(@PathParam("id") String id, @PathParam("size") long size) throws IOException, ClassNotFoundException, SQLException {
        String response = "Oops!";
        String url = DB + "/" + getFileUrl(id) + "/" + id;
        System.out.println("Checking... " + id + "|" + size);
        if (size > 0) {
            if (seekFile(url + ".zip")) {
                response = checkFile(url + ".zip", size);
            }
            if (response.equals("OK")) {
                File part = new File(url + ".zip.part");
                if (part.exists()) {
                    part.delete();
                }
            }
        }
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("re_download/{id}_{size}")
    public Response reDownloadFile(@PathParam("id") String id, @PathParam("size") long size) throws IOException, ClassNotFoundException, SQLException {
        File part = null;
        String url = DB + "/" + getFileUrl(id) + "/" + id + ".zip";
        System.out.println("ReDownloading... " + id + "|" + size);
        if (size > 0) {
            if (seekFile(url) && checkFile(url, size).equals("Retry")) {
                part = returnPart(url, size);
            }
        }
        return Response.ok().entity(part).build();
    }

    @POST
    @Path("upload/{id}")
    public void uploadFile(@PathParam("id") String id, String log) throws IOException, ClassNotFoundException, SQLException {
        String url = DB + "/" + getFileUrl(id) + "/" + id;
        System.out.println("Uploading... " + id);
        writeToFile(url + ".log", log);
    }

    @GET
    @Path("check_upload/{id}_{size}")
    public Response checkUploadFile(@PathParam("id") String id, @PathParam("size") long size) throws IOException, ClassNotFoundException, SQLException {
        String response = "Oops!";
        String dir = DB + "/" + getFileUrl(id);
        String url = DB + "/" + getFileUrl(id) + "/" + id;
        System.out.println("CheckUpload... " + id + "|" + size);
        if (size > 0) {
            if (seekFile(dir)) {
                if (seekFile(url + ".log")) {
                    response = checkFile(url + ".log", size);
                } else {
                    response = "Retry";
                }
            }
        }
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("test")
    public Response test(@PathParam("id") String id) throws ClassNotFoundException, SQLException, IOException {
        String url = getFileUrl();
        ResponseBuilder response = Response.ok(null);
        if (url != null && !url.isEmpty()) {
            File file = returnFileFrom(url, ".java");
            String className = FilenameUtils.removeExtension(file.getName());
            file = returnFileFrom(url, ".zip");
            String fileName = FilenameUtils.removeExtension(file.getName());
            System.out.println("Downloading... FileName: " + fileName + " - ClassName: " + className);
            response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"" + "classname=\"" + className + "\"");
        }
        return response.build();
    }

    private String getFileUrl() throws ClassNotFoundException, SQLException {
        String url = "";
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(USERS_INFO, USERNAME, PASSWORD);
        ResultSet rs;
        Statement s = conn.createStatement();
        String sql = "SELECT id FROM down_up WHERE status='download'";
        String username = "";
        String id = "";
        rs = s.executeQuery(sql);
        rs.next();
        int i = Integer.parseInt(rs.getString("id")) + 1;
        String query = "SELECT * FROM compiled";
        rs = s.executeQuery(query);
        int j = 0;
        while (rs.next()) {
            j++;
            if (i == j) {
                System.out.println("i == j");
                username = rs.getString("username");
                id = rs.getString("id");
            }
        }
        if (id != null && !id.isEmpty()) {
            s.executeUpdate("UPDATE down_up SET id = (id + 1) WHERE status='download'");
            url = DB + "/" + username + "/" + id;
        }
        s.close();
        rs.close();
        conn.close();
        System.out.println(url);
        return url;
    }

    private String getFileUrl(String id) throws ClassNotFoundException, SQLException {
        String username;
        String connectionURL = "jdbc:mysql://localhost:3306/users_info";
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
        ResultSet rs;
        Statement s = connection.createStatement();
        String sql = "SELECT username FROM file_id WHERE id=" + id;
        rs = s.executeQuery(sql);
        rs.next();
        username = rs.getString("username");
        rs.close();
        s.close();
        connection.close();
        return username + "/" + id;
    }

    public void writeToFile(String path, String string) throws IOException {
        System.out.println("Writing... " + string);
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(string);
        close(fileWriter);
    }

    private File returnFileFrom(String url, String type) {
        File folder = new File(url);
        String fileName;
        File file = null;
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(type)) {
                    file = new File(folder + "/" + fileName);
                    break;
                }
            }
        }
        System.out.println("Returning " + file);
        return file;
    }

    private boolean seekFile(String url) {
        File file = new File(url);
        System.out.println("Seeking " + url + "...");
        if (file.exists()) {
            System.out.println("OK");
            return true;
        } else {
            System.out.println("Oops!");
            return false;
        }
    }

    private String checkFile(String url, Long size) {
        System.out.println("Checking " + url + "...");
        String response = "Oops!";
        File file = new File(url);
        if (size == file.length()) {
            response = "OK";
        } else if (size < file.length()) {
            response = "Retry";
        }
        System.out.println(response);
        return response;
    }

    public File returnPart(String url, long start) throws IOException {
        File file = new File(url);
        OutputStream output = new FileOutputStream(url + ".part");
        int read;
        byte[] buffer = new byte[1024];
        RandomAccessFile input;
        input = new RandomAccessFile(file, "r");
        input.seek(start);
        while ((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(input);
        output.flush();
        close(output);
        File part = new File(url + ".part");
        return part;
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
            }
        }
    }
}
