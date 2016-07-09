package com.slurpeh.servercore.practice.util;

import com.slurpeh.servercore.practice.KohiPractice;
import com.sun.corba.se.impl.presentation.rmi.ExceptionHandlerImpl;
import com.sun.xml.internal.ws.developer.Serialization;

import java.io.*;
import java.util.Date;

/**
 * Created by Bradley on 5/16/16.
 */
public class LogFileWriter {
    File f;
    FileWriter fw;
    PrintWriter pw;
    public LogFileWriter(KohiPractice plugin) throws Exception {
        if (!new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "logs").exists()) {
            new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "logs").mkdirs();
        }
        this.f = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + new Date().toString() + ".log");
        f.createNewFile();
        this.fw = new FileWriter(f);
        this.pw = new PrintWriter(fw);
    }

    public void write(Object o, String description) {
        pw.write(description + " - ");
        pw.write("  " + o.toString());
    }
}
