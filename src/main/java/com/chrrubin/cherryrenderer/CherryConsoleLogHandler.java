package com.chrrubin.cherryrenderer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class CherryConsoleLogHandler extends StreamHandler {
    public CherryConsoleLogHandler() {
        super(System.out, new SimpleFormatter());
        setLevel(Level.ALL);
    }

    public void close() {
        flush();
    }

    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    public static class SimpleFormatter extends Formatter {
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(180);
            DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss,SS");

            builder.append("[").append(pad(Thread.currentThread().getName(), 20)).append("] ");
            builder.append(pad(record.getLevel().toString(), 7));
            builder.append(" - ");
            builder.append(pad(dateFormat.format(new Date(record.getMillis())), 12));
            builder.append(" - ");
            builder.append(toClassString(record.getSourceClassName()));
            builder.append('#');
            builder.append(record.getSourceMethodName());
            builder.append(": ");
            builder.append(formatMessage(record));

            builder.append("\n");

            Throwable throwable = record.getThrown();
            if (throwable != null) {
                StringWriter sink = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sink, true));
                builder.append(sink.toString());
            }

            return builder.toString();
        }

        private String pad(String string, int size) {
            int length = string.length();
            StringBuilder stringBuilder = new StringBuilder(string);
            if(length < size){
                for(int i = length; i < size; i++){
                    stringBuilder.append(" ");
                }
            }
            return stringBuilder.toString();
        }

        private String toClassString(String name) {
            return name.length() > 30 ? name.substring(name.length() - 30) : name;
        }
    }
}
