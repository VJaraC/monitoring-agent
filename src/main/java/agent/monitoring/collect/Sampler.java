package agent.monitoring.collect;

import agent.monitoring.model.MetricSample;

import java.io.File;
import java.lang.management.ManagementFactory;

import agent.monitoring.util.TimeOffset;
import com.sun.management.OperatingSystemMXBean;
import java.time.Instant;

public class Sampler {
    private final OperatingSystemMXBean osBean;

    public Sampler() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public MetricSample sample() {
        // 1. CPU
        double cpuLoad = osBean.getCpuLoad() * 100.0; // MX manda un numero entre 0 y 1, se multiplica por 100 para convertirlo en porcentaje
        if (Double.isNaN(cpuLoad)) cpuLoad = 0.0;

        // 2. RAM
        long totalMem = osBean.getTotalMemorySize(); //obtiene la RAM total
        long usedMem = totalMem - osBean.getFreeMemorySize(); //obtiene la RAM libre, se resta la total con la libre para tener la usada
        double ramPercent = (totalMem > 0) ? ((double) usedMem / totalMem) * 100.0 : 0.0; //convertir la RAM usada en porcentaje

        // 3. Disco (porcentaje + GB usados / GB totales)
        File disco = new File("C:\\"); // "/" en Linux; puedes parametrizar esto si quieres
        long totalBytes = disco.getTotalSpace();
        long freeBytes = disco.getFreeSpace();
        long usedBytes = totalBytes - freeBytes;

        double diskPercent = (totalBytes > 0)
                ? ((double) usedBytes / totalBytes) * 100.0
                : 0.0;

        double diskUsedGb = bytesToGb(usedBytes);
        double diskTotalGb = bytesToGb(totalBytes);

        return new MetricSample(
                TimeOffset.getSynchronizedTime().toString(), //momento exacto de la lectura
                cpuLoad,
                ramPercent,
                diskPercent,
                diskUsedGb,
                diskTotalGb
        );
    }
    private double bytesToGb(long bytes) {
        final long BYTES_PER_GB = 1024L * 1024L * 1024L;
        return (double) bytes / BYTES_PER_GB;
    }
}