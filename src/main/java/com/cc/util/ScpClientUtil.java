package com.cc.util;

import ch.ethz.ssh2.*;

import java.io.File;
import java.io.IOException;

/**
 * 远程数据传输
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午5:50:11
 */
public class ScpClientUtil {

    private final String usr;
    private final String pwd;
    private final String remoteHostIp;

    /**
     * @param serverIp remote server IP
     * @param username scp.username
     * @param password scp.password
     */
    public ScpClientUtil(String serverIp, String username, String password) {
        this.usr = username;
        this.pwd = password;
        this.remoteHostIp = serverIp;
    }

    /**
     * 把给定的本地文件或者目录(包括里面所有的内容)上传到远程服务器上
     *
     * @param localFilePath 本地目录名称或文件名
     * @param remotePath 要上传的远程路径
     * @throws IOException
     */
    public void doUpload(String localFilePath, String remotePath) throws IOException {
        // 为了防止上传空字符串文件名,出现过类似问题
        localFilePath = localFilePath.trim();
        remotePath = remotePath.trim();

        if ("".equals(localFilePath)) {
            throw new IOException("Local file path is required");
        }
        if ("".equals(remotePath)) {
            throw new IOException("Local file path is required");
        }

        Connection conn = null;
        try {
            conn = new Connection(remoteHostIp);            
            conn.connect();    //可以设定连接超时时间
            boolean isAuthed = conn.authenticateWithPassword(usr, pwd);
            System.out.println("isAuthed: " + isAuthed);

            File localPath = new File(localFilePath);
            if (localPath.isDirectory()) {
                uploadDir(conn, localFilePath, remotePath, "0644");
            } else {
                System.out.println("uploading: " + localFilePath + " to " + remotePath);
                SCPClient client = conn.createSCPClient();
                client.put(localFilePath, remotePath);
                System.out.println(" -> OK");
            }
        } catch (IOException e) {
            System.out.println("Fail to upload: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * 上传一个目录到远处服务器上 (包括目录中的所有文件和子目录)
     *
     * @param conn
     * @param localDirName 本地目录名称
     * @param remotePath 要上传的远程路径
     * @param mode 文件权限
     * @throws IOException
     */
    private void uploadDir(Connection conn,
            String localDirName,
            String remotePath,
            String mode) throws IOException {

        File localDir = new File(localDirName);
        String remoteDir = remotePath + "/" + localDir.getName();
        createDir(conn, remoteDir);

        File[] files = localDir.listFiles();
        for (File localFile : files) {
            if (localFile.isDirectory()) {
                // 如果是目录,则递归进入此目录进行操作
                uploadDir(conn, localFile.getAbsolutePath(), remoteDir, mode);
            } else {
                // 如果是文件,则直接上传
                SCPClient client = conn.createSCPClient();
                System.out.print("uploading: " + localFile.getAbsolutePath() + " to " + remoteDir);
                client.put(localFile.getAbsolutePath(), remoteDir, mode);
                System.out.println(" -> OK");
            }

        }

    }
    
    /**
	 * Create a remote file and copy the contents of the passed byte array into it.
	 * Uses mode 0600 for creating the remote file.
	 * 
	 * @param data the data to be copied into the remote file.
	 * @param remoteFileName The name of the file which will be created in the remote target directory.
	 * @param remoteTargetDirectory Remote target directory. Use an empty string to specify the default directory.
	 * @throws IOException
	 */
    public void doUpload(byte[] data, String remoteFileName, String remoteTargetDirectory)
            throws IOException {
        // 为了防止上传空字符串文件名,出现过类似问题
    	remoteFileName = remoteFileName.trim();

        if ("".equals(remoteFileName)) {
            throw new IOException("File name is required");
        }
        if ("".equals(remoteTargetDirectory)) {
            throw new IOException("Remote target directory is required");
        }

        Connection conn = null;
        try {
//            conn = new Connection(remoteHostIp, Integer.parseInt(uploadPathVO.getServerPort()));
            conn = new Connection(remoteHostIp);
            conn.connect();    //可以设定连接超时时间
            boolean isAuthed = conn.authenticateWithPassword(usr, pwd);
            System.out.println("isAuthed: " + isAuthed);
                        
            System.out.println("uploading: " + remoteFileName
                    + " to " + remoteTargetDirectory);
            SCPClient client = conn.createSCPClient();
            client.put(data, remoteFileName, remoteTargetDirectory);
            System.out.println(" -> OK");
        } catch (IOException e) {
            System.out.println("Fail to upload: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Create one directory in the remote server.
     *
     * @param conn
     * @param remoteDir
     * @throws IOException
     */
    private void createDir(Connection conn, String remoteDir)
            throws IOException {
        Session s = conn.openSession();
        try {
            System.out.println("create dir: " + remoteDir);
            s.execCommand("mkdir -p -m 755 " + remoteDir);
            s.waitForCondition(ChannelCondition.EOF, 0);
        } finally {
            s.close();
        }

    }

}
