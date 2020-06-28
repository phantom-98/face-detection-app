package com.facecoolalert.resources.cpu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CPUUsage {

    private static double previousTotalCpuTime = 0.0;
    private static double previousAppCpuTime = 0.0;

    public static Double[] readUsage() {


        return getCPUUsage();

    }

    public static Double[] getCPUUsage() {
        StringBuilder result = new StringBuilder();
        Process process = null;
        try {
            process = new ProcessBuilder("ps", "-o", "%cpu,cmd").start();

            Double totalUsage=0.0;
            Double currUsage=0.0;
            // Capture standard output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
//                    System.out.println("processing"+line);
//                result.append(line).append("\n");
                    String[] toks = line.trim().split("\\s+");
                    String name = toks[1];
                    String usage = toks[0];
//                    System.out.println("usage "+usage+" "+ Arrays.toString(toks));
                    Double cur = Double.parseDouble(usage)/10.0;
//                currUsage=Double.parseDouble(line);
                    if (name.contains("facecool.alert")) {
                        currUsage = cur;
                    }
                    totalUsage += cur;
                }catch (Exception es)
                {
                    es.printStackTrace();
                }

            }


            process.waitFor();
            return new Double[]{currUsage,totalUsage};

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return new Double[]{0.0,0.0};
    }



}
