package agent.monitoring.model;

import com.google.gson.annotations.SerializedName;

public record MetricSample(
        // Usamos @SerializedName para asegurar que el JSON salga con los nombres que espera el server
        @SerializedName("timestamp") String tsUtc,

        //uso del CPU
        @SerializedName("cpuUsage") double cpuUsage,

        //uso de la RAM
        @SerializedName("ramUsage") double ramUsage,

        // ---------------------------------------------------
        //                       Disco
        // ---------------------------------------------------

        //porcentaje del disco usado
        @SerializedName("diskPercent") double diskPercent,

        // GB usados
        @SerializedName("diskUsedGb") double diskUsedGb,

        @SerializedName("diskTotalGb") double diskTotalGb


) {}