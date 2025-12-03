# 🚀 UNAP Monitoring Agent

Agente de monitoreo para los **Laboratorios UNAP**, encargado de capturar métricas del sistema y enviarlas al servidor central (**MetricsServer**) mediante HTTPS.  
El agente es completamente **portable**, incluye su propio **JRE**, y no requiere ninguna instalación de Java en el sistema.

---

## 📌 Características principales

- 🖥 **Monitoreo en tiempo real**
  - CPU  
  - RAM  
  - Disco  
  - Usuario activo / Sesiones  
  - Reinicios y nuevas ejecuciones  

- 🔗 **Comunicación segura**
  - Envío de métricas vía HTTPS
  - Compatible con dominios propios o túneles ngrok

- ⚙ **Portable**
  - Incluye JRE portable (no requiere instalación)
  - Ejecutable `.exe` generado con Launch4j
  - `start.bat` para definir variables y lanzar el agente

- 📦 **Estructura del paquete**
  ```text
  monitoring-agent/
    monitoring-agent.exe
    monitoring-agent-1.0.0-jar-with-dependencies.jar
    start.bat
    jre/
      bin/java.exe
      lib/
## 🧪 Requisitos

- Windows 10 o Windows 11 (64-bit)
- Permiso para ejecutar archivos `.exe`
- Conexión a Internet para el envío de métricas
- No requiere Java instalado  
  → el paquete incluye un **JRE portable** dentro de la carpeta `jre/`


## 🧡 Créditos

Proyecto desarrollado para los **Laboratorios UNAP**.  
Realizado por  @dani-pp ,  @AHidalgoG  y  @VJaraC.


