package org.deltaproject.manager.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deltaproject.manager.utils.AlwaysAllowingConsoleKnownHostsKeyVerification;


/**
 * Created by seungsoo on 7/4/16.
 */
public class J2sshCient {
    private Log log = LogFactory.getLog(this.getClass());
    private SshClient ssh = null;
    private SessionChannelClient session = null;
    private String hostPrompt = null;

    /**
     * @param server
     * @param userid
     * @param pwd
     * @throws Exception
     */
    public J2sshCient(String server, String userid, String password) throws Exception {
        PasswordAuthenticationClient auth = null;

        try {
            if (server == null || userid == null || password == null) {
                System.out.println("Parameter is null!");
            }

            ssh = new SshClient();
            ssh.setSocketTimeout(30000);
            SshConnectionProperties properties = new SshConnectionProperties();
            properties.setHost(server);
            properties.setPort(22);

            // Connect to the host
            ssh.connect(properties, new AlwaysAllowingConsoleKnownHostsKeyVerification());

            PasswordAuthenticationClient authClient = new PasswordAuthenticationClient();
            authClient.setUsername(userid);
            authClient.setPassword(password);

            int result = ssh.authenticate(authClient);

            if (result != AuthenticationProtocolState.COMPLETE) {
                throw new Exception("Login failed");
            }

            session = ssh.openSessionChannel();
            session.requestPseudoTerminal("vt100", 80, 25, 0, 0, "");
            session.startShell();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String exec(String cmd) throws Exception {
        StringBuffer returnValue = null;
        boolean promptReturned = false;
        byte[] buffer = null;
        OutputStream out = null;
        InputStream in = null;
        int read;
        String response = null;
        int i = 0;

        try {
            if (session == null) {
                throw new Exception("Session is not connected!");
            }

            out = session.getOutputStream();
            in = session.getInputStream();

            cmd = cmd + "\n";
            out.write(cmd.getBytes());
            out.flush();

            buffer = new byte[255];
            returnValue = new StringBuffer(300);

            while (promptReturned == false && (read = in.read(buffer)) > 0) {
                response = new String(buffer, 0, read);

                if (i == 1)
                    returnValue.append(response.toString());
                if (!StringUtils.isEmpty(response) && response.indexOf(this.hostPrompt) >= 0) {
                    ++i;
                    if (i >= 2) {
                        promptReturned = true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue.toString();
    }

    public boolean isClosed() throws Exception {
        boolean rtn = true;
        try {

            if (session != null)
                rtn = session.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public boolean logout() throws Exception {
        boolean rtn = false;

        try {
            if (session != null) {
                session.getOutputStream().write("exit\n".getBytes());
                session.close();
            }
            if (ssh != null)
                ssh.disconnect();
            rtn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getHostprompt() {

        return this.hostPrompt;
    }

    public void setHostprompt(String hostPrompt) {

        this.hostPrompt = hostPrompt;
    }
}