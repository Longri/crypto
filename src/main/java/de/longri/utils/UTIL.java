package de.longri.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class UTIL {

    public static HashMap<String, String> getSystemInfo() {

        HashMap<String, String> map = new HashMap<>();

        if (SystemType.getSystemType() == SystemType.MAC) {

            /**
             *Hardware:
             *
             *     Hardware Overview:
             *
             *       Model Name: MacBook Pro
             *       Model Identifier: MacBookPro16,1
             *       Processor Name: 6-Core Intel Core i7
             *       Processor Speed: 2,6 GHz
             *       Number of Processors: 1
             *       Total Number of Cores: 6
             *       L2 Cache (per Core): 256 KB
             *       L3 Cache: 12 MB
             *       Hyper-Threading Technology: Enabled
             *       Memory: 16 GB
             *       Boot ROM Version: 1037.147.4.0.0 (iBridge: 17.16.16610.0.0,0)
             *       Serial Number (system): C02CX3K3MD6R
             *       Hardware UUID: BF367C4B-1CF8-5101-8E90-7CDA5E046A6D
             *       Activation Lock Status: Enabled
             */
            try {
                String result = execCmd("system_profiler SPHardwareDataType");

                String[] lines = result.split("\n");

                boolean ser = false;
                boolean mem = false;
                boolean pro = false;
                boolean mod = false;

                for (String line : lines) {
                    if (!ser && line.contains("Serial Number")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("serialNumber", line.substring(pos).trim());
                        ser = true;
                    } else if (!mem && line.contains("Memory")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("memory", line.substring(pos).trim());
                        mem = true;
                    } else if (!pro && line.contains("Processor Name")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("processor", line.substring(pos).trim());
                        pro = true;
                    } else if (!pro && line.contains("Chip:")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("processor", line.substring(pos).trim());
                        pro = true;
                    } else if (!mod && line.contains("Model Identifier")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("model", line.substring(pos).trim());
                        mod = true;
                    }
                }


                result = execCmd("system_profiler SPSoftwareDataType");

                lines = result.split("\n");

                boolean os = false;


                for (String line : lines) {
                    if (!os && line.contains("System Version")) {
                        int pos = line.indexOf(':') + 1;
                        map.put("operatingSystem", line.substring(pos).trim());
                        os = true;
                    }
                }

                map.put("manufacturer", "Apple");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (SystemType.getSystemType() == SystemType.WIN) {

            /** WMIC COMPUTERSYSTEM get Manufacturer,Model,Caption,TotalPhysicalMemory
             * Caption   Manufacturer  Model        TotalPhysicalMemory
             * BM-NB162  Dell Inc.     XPS 13 9370  8388562944
             */

            /**  WMIC OS get Caption
             * Caption
             * Microsoft Windows 10 Pro
             */

            /** WMIC BIOS get SerialNumber
             * SerialNumber
             * JB1TQN2
             */

            /** WMIC CPU get Name
             * Name
             * Intel(R) Core(TM) i5-8250U CPU @ 1.60GHz
             */
            try {
                String result = execCmd("WMIC COMPUTERSYSTEM get Manufacturer");
                String[] lines = result.split("\r\n");
                map.put("manufacturer", lines[1].trim());

                result = execCmd("WMIC COMPUTERSYSTEM get Model");
                lines = result.split("\r\n");
                map.put("model", lines[1].trim());

                result = execCmd("WMIC BIOS get SerialNumber");
                lines = result.split("\r\n");
                map.put("serialNumber", lines[1].trim());

                result = execCmd("wmic memorychip get capacity");
                lines = result.split("\r\n");
                long bytes = Long.parseLong(lines[1].trim());
                if (lines.length > 2 && !lines[2].trim().isEmpty()) bytes += Long.parseLong(lines[2].trim());
                if (lines.length > 3 && !lines[3].trim().isEmpty()) bytes += Long.parseLong(lines[3].trim());
                if (lines.length > 4 && !lines[4].trim().isEmpty()) bytes += Long.parseLong(lines[4].trim());
                if (lines.length > 5 && !lines[5].trim().isEmpty()) bytes += Long.parseLong(lines[5].trim());
                map.put("memory", humanReadableByteCount(bytes));

                result = execCmd("WMIC CPU get Name");
                lines = result.split("\r\n");
                map.put("processor", lines[1].trim());

                result = execCmd("WMIC OS get Caption");
                lines = result.split("\r\n");
                map.put("operatingSystem", lines[1].trim());
                result = execCmd("WMIC COMPUTERSYSTEM get Model");
                lines = result.split("\r\n");
                map.put("model", lines[1].trim());


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return map;
    }

    public static String humanReadableByteCount(final long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }

    public static String execCmd(String cmd) throws IOException {
        if (cmd == null || cmd.isEmpty()) return "";
        Process child = Runtime.getRuntime().exec(cmd);

        InputStream in = child.getInputStream();
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
        }
        in.close();
        return sb.toString();
    }

    static public enum SystemType {
        MAC, WIN, LINUX, UNKNOWN;

        public static SystemType getSystemType() {
            Properties sysprops = System.getProperties();
            String osName = ((String) sysprops.get("os.name")).toLowerCase();
            if (osName.matches(".*win.*")) return SystemType.WIN;
            if (osName.matches(".*mac.*")) return SystemType.MAC;
            if (osName.matches(".*Linux.*")) return SystemType.LINUX;
            SystemType unknown = SystemType.UNKNOWN;
            return unknown;
        }

        public static boolean isWindows() {
            return getSystemType() == WIN;
        }

        public static boolean isLinux() {
            return getSystemType() == LINUX;
        }

        public static boolean isMac() {
            return getSystemType() == MAC;
        }

        public static boolean isUnknown() {
            return getSystemType() == UNKNOWN;
        }
    }
}
