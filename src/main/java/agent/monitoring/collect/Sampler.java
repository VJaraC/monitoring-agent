package agent.monitoring.collect;

import agent.monitoring.model.MetricSample;
import agent.monitoring.util.TimeOffset;
import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;

public class Sampler {
    private final OperatingSystemMXBean osBean;
    private final HardwareAbstractionLayer hardware;

    // Variables para calcular Deltas
    private long prevReadBytes = 0;
    private long prevWriteBytes = 0;
    private long prevTransferTime = 0; // Para calcular el % de actividad
    private long prevTimeStamp = 0;

    public Sampler() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        SystemInfo si = new SystemInfo();
        this.hardware = si.getHardware();
    }

    public MetricSample sample() {
        // 1. CPU
        double cpuLoad = osBean.getCpuLoad() * 100.0;
        if (Double.isNaN(cpuLoad)) cpuLoad = 0.0;

        // Devuelve grados Celsius.
        double cpuTemp = hardware.getSensors().getCpuTemperature();

        // 2. RAM
        long totalMem = osBean.getTotalMemorySize();
        long usedMem = totalMem - osBean.getFreeMemorySize();
        double ramPercent = (totalMem > 0) ? ((double) usedMem / totalMem) * 100.0 : 0.0;

        // 3. DATOS DE DISCO (IO y Actividad)
        // Inicializamos en 0
        double readRateKb = 0.0;
        double writeRateKb = 0.0;
        double diskActivityPercent = 0.0; // Esto reemplazará al espacio usado

        // Datos de espacio (Opcional: Si quieres seguir mandando los GB totales/usados aunque el % sea actividad)
        File disco = new File("C:\\");
        double diskUsedGb = bytesToGb(disco.getTotalSpace() - disco.getFreeSpace());
        double diskTotalGb = bytesToGb(disco.getTotalSpace());

        List<HWDiskStore> disks = hardware.getDiskStores();
        if (!disks.isEmpty()) {
            HWDiskStore disk = disks.get(0); // Disco principal
            disk.updateAttributes(); // IMPORTANTE: Actualizar contadores

            long currentRead = disk.getReadBytes();
            long currentWrite = disk.getWriteBytes();
            long currentTransfer = disk.getTransferTime(); // ms que el disco estuvo ocupado
            long now = System.currentTimeMillis();

            if (prevTimeStamp > 0 && now > prevTimeStamp) {
                long timeDiffMs = now - prevTimeStamp;

                // A. Calcular Velocidad en KB/s
                long deltaRead = currentRead - prevReadBytes;
                long deltaWrite = currentWrite - prevWriteBytes;

                readRateKb = (double) deltaRead * 1000 / timeDiffMs / 1024.0;
                writeRateKb = (double) deltaWrite * 1000 / timeDiffMs / 1024.0;

                // B. Calcular % de Actividad (Active Time)
                long deltaTransfer = currentTransfer - prevTransferTime;

                // Validación: no puede haber estado activo más tiempo del que pasó
                if (deltaTransfer > timeDiffMs) deltaTransfer = timeDiffMs;

                diskActivityPercent = ((double) deltaTransfer / timeDiffMs) * 100.0;
            }

            // Actualizar estado previo
            prevReadBytes = currentRead;
            prevWriteBytes = currentWrite;
            prevTransferTime = currentTransfer;
            prevTimeStamp = now;
        }

        return new MetricSample(
                TimeOffset.getSynchronizedTime().toString(),
                cpuLoad,
                cpuTemp,
                ramPercent,
                diskActivityPercent,
                readRateKb,
                writeRateKb
        );
    }

    private double bytesToGb(long bytes) {
        return (double) bytes / (1024L * 1024L * 1024L);
    }
}