package de.longri.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class UTIL {

    static final String SERIAL_NUMBER = getSystemInfo().get("serialNumber");

    static int debugcounter = 0;

    static HashMap<String, String> map;

    public static HashMap<String, String> getSystemInfo() {

        if (map != null) return map;

        map = new HashMap<>();

        debugcounter=debugcounter+1;
        System.out.println("Count = "+ debugcounter);

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

//            change
            String manufacturer = "";
            String model;
            String serialNumber;
            String cpuName;
            String osCaption;
            String totalMemoryBytesStr;

            try {
                String cmd =
                        "powershell -NoProfile -Command " +
                                "\"$cs = Get-CimInstance Win32_ComputerSystem; " +
                                "$bios = Get-CimInstance Win32_BIOS; " +
                                "$cpu = Get-CimInstance Win32_Processor; " +
                                "$os = Get-CimInstance Win32_OperatingSystem; " +
                                "Write-Output (" +
                                "$cs.Manufacturer + '|' +" +
                                "$cs.Model + '|' +" +
                                "$bios.SerialNumber + '|' +" +
                                "$cpu.Name + '|' +" +
                                "$os.Caption + '|' +" +
                                "$cs.TotalPhysicalMemory" +
                                ")\"";

                String output = execCmd(cmd).trim();

                // Ergebnis: manufacturer|model|serial|cpuName|osCaption|totalMemory
                String[] parts = output.split("\\|", -1);
                if (parts.length >= 6) {
                    manufacturer        = parts[0].trim();
                    model               = parts[1].trim();
                    serialNumber        = parts[2].trim();
                    cpuName             = parts[3].trim();
                    osCaption           = parts[4].trim();
                    totalMemoryBytesStr = parts[5].trim();
                } else {
                    throw new RuntimeException("Unexpected output from PowerShell: " + output);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println(manufacturer);
            System.out.println(model);
            System.out.println(serialNumber);
            System.out.println(cpuName);
            System.out.println(osCaption);
            System.out.println(totalMemoryBytesStr);


            String result;
            String[] lines;
            map.put("manufacturer", manufacturer);
            map.put("model", model);
            map.put("serialNumber", serialNumber);
            long totalMemoryBytes = Long.parseLong(totalMemoryBytesStr);
            System.out.println("Mem:" + humanReadableByteCount(totalMemoryBytes));
            map.put("memory", humanReadableByteCount(totalMemoryBytes));

            map.put("processor", cpuName);
            map.put("operatingSystem", osCaption);
            map.put("model", "-");


        }else if (SystemType.getSystemType() == SystemType.LINUX){
            try {
                // Serial Number
                String result = execCmd("sudo dmidecode -s system-serial-number");
                map.put("serialNumber", result.trim());

                // Memory
                result = execCmd("grep MemTotal /proc/meminfo");
                if (result.contains("MemTotal")) {
                    int pos = result.indexOf(':') + 1;
                    map.put("memory", result.substring(pos).trim());
                }

                // Processor
                result = execCmd("lscpu | grep 'Model name'");
                if (result.contains("Model name")) {
                    int pos = result.indexOf(':') + 1;
                    map.put("processor", result.substring(pos).trim());
                }

                // Model Identifier
                result = execCmd("sudo dmidecode -s system-product-name");
                map.put("model", result.trim());

                // Operating System
                result = execCmd("uname -a");
                map.put("operatingSystem", result.trim());

                // Manufacturer
                result = execCmd("sudo dmidecode -s system-manufacturer");
                map.put("manufacturer", result.trim());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Printing the information
            map.forEach((key, value) -> System.out.println(key + ": " + value));
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
            if (osName.matches(".*linux.*")) return SystemType.LINUX;
            if (osName.matches(".*debian.*")) return SystemType.LINUX;
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
