package agent.monitoring.model;

import com.google.gson.annotations.SerializedName;

public record MetricSample(
        // Usamos @SerializedName para asegurar que el JSON salga con los nombres que espera el server
        @SerializedName("timestamp") String tsUtc,

        //uso del CPU
        @SerializedName("cpuUsage") double cpuUsage,

        //temperatura del cpu
        @SerializedName("cpuTemp") double cpuTemp,

        //uso de la RAM
        @SerializedName("ramUsage") double ramUsage,

        // ---------------------------------------------------
        //                       Disco
        // ---------------------------------------------------

        //porcentaje del disco usado
        @SerializedName("diskPercent") double diskPercent,

        //lectura del disco
        @SerializedName("diskReadRate") double diskReadRate,

        //escritura del disco
        @SerializedName("diskWriteRate") double diskWriteRate






) {}