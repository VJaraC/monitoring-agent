package agent.monitoring;

import agent.monitoring.collect.Sampler;
import agent.monitoring.config.AgentConfig;
import agent.monitoring.model.BatchPayload;
import agent.monitoring.model.MetricSample;
import agent.monitoring.transport.HttpTransport; // <--- 1. Importante: Importar tu nueva clase
import agent.monitoring.transport.Transport;

import java.io.IOException;
import java.net.http.HttpClient;
import agent.monitoring.util.TimeOffset;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AgentMain {

    private final AgentConfig cfg;
    private final Sampler sampler;
    private final Transport transport;

    // buffer de muestras (para 1 minuto aprox)
    private final Deque<MetricSample> ring = new ArrayDeque<>();
    private final ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);

    public AgentMain(AgentConfig cfg, Sampler sampler, Transport transport) {
        this.cfg = cfg;
        this.sampler = sampler;
        this.transport = transport;
    }

    public void start() {
        // TAREA 1: Captura cada X segundos (por defecto 15s)
        ses.scheduleAtFixedRate(() -> {
            try {
                MetricSample s = sampler.sample();
                synchronized (ring) {
                    ring.addLast(s);
                    while (ring.size() > 60) ring.removeFirst(); // Límite de seguridad aumentado un poco
                }
                System.out.println("[Agent] sample @ " + Instant.now() + " -> " + s);
            } catch (Exception e) {
                System.err.println("[Agent] Error al capturar: " + e.getMessage());
            }
        }, 0, cfg.sampleIntervalSeconds(), TimeUnit.SECONDS);

        // TAREA 2: Envío cada Y segundos (por defecto 60s)
        ses.scheduleAtFixedRate(() -> {
            List<MetricSample> batch;
            synchronized (ring) {
                if (ring.isEmpty()) return;
                batch = new ArrayList<>(ring);
                ring.clear();
            }
            try {
                // Construye el paquete con la info del Host y la lista de métricas
                BatchPayload payload = BatchPayload.of(cfg.agentKey(), cfg.hostInfo(), batch);

                // Envía usando el transporte configurado (ahora es HttpTransport)
                transport.send(payload);

                System.out.println("[Agent] batch sent: " + batch.size() + " samples");
            } catch (Exception e) {
                System.err.println("[Agent] send failed: " + e.getMessage());
                // Rollback: Devuelve las métricas al buffer para reintentar luego
                synchronized (ring) {
                    // Solo devolvemos si hay espacio para evitar desbordamiento
                    if(ring.size() < 100) batch.forEach(ring::addFirst);
                }
            }
        }, cfg.sendIntervalSeconds(), cfg.sendIntervalSeconds(), TimeUnit.SECONDS);
    }
    private static void synchronizeTime(String timeUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(timeUrl))
                    .GET()
                    .build();

            // Medir el tiempo local ANTES de la petición (T1)
            long t1 = System.currentTimeMillis();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Medir el tiempo local DESPUÉS de la petición (T3)
            long t3 = System.currentTimeMillis();

            if (response.statusCode() == 200) {
                String serverTimeString = response.body().trim();
                // Tiempo que el servidor dijo que era (T2)
                long t2ServerTime = Instant.parse(serverTimeString).toEpochMilli();

                // Cálculo de latencia promedio (Round Trip Time, T3-T1)
                long latencia = (t3 - t1) / 2;

                // El offset real es: (Tiempo del Servidor) - (Tiempo Local Promedio)
                long offset = t2ServerTime - t3 + latencia;
                TimeOffset.setOffset(offset);

                System.out.println("✅ Sincronización exitosa. Offset aplicado: " + offset + "ms.");

            } else {
                System.err.println("❌ Error al sincronizar hora. Servidor respondió: " + response.statusCode());
            }
        } catch (DateTimeParseException | InterruptedException | IOException e) {
            System.err.println("❌ Fallo en la sincronización de tiempo. Usando reloj local. Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 1. Cargar configuración (Variables de entorno o valores por defecto)
        AgentConfig cfg = AgentConfig.fromEnvOrDefaults();

        String timeUrl = cfg.ingestUrl().replace("/metrics", "/time");
        synchronizeTime(timeUrl);

        // 2. Inicializar el recolector de métricas
        Sampler sampler = new Sampler();

        // 3. Inicializar el Transporte REAL
        //    Si quieres cambiar la URL, configura la variable de entorno INGEST_URL
        Transport transport = new HttpTransport(cfg.ingestUrl());

        System.out.println("Iniciando Agente...");
        System.out.println(" -> Server URL: " + cfg.ingestUrl());
        System.out.println(" -> Agent Key:  " + cfg.agentKey());

        // 4. Arrancar el agente
        new AgentMain(cfg, sampler, transport).start();
    }
}