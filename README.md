# MotionDetector

**MotionDetector** es una aplicación desarrollada en Android Studio que detecta y clasifica la magnitud del movimiento (leve, medio o fuerte) a partir de los datos proporcionados por el sensor **acelerómetro**. La app está diseñada para ejecutarse en diferentes tipos de dispositivos Android, incluyendo **Wear OS**, **Mobile** y **Android TV**, permitiendo la comunicación entre ellos mediante diferentes protocolos.



## 🧠 Descripción del Proyecto

La aplicación simula diferentes **actividades físicas** (caminar, correr, saltar, caídas, etc.) a través de botones que activan un `SensorEventListener` conectado al **acelerómetro** del dispositivo. En base a los valores de aceleración, la app evalúa la **gravedad del impacto** y clasifica el movimiento en tres niveles:

- `leve`
- `medio`
- `fuerte`

La arquitectura de comunicación se adapta a las particularidades de cada plataforma:

- **Wear ↔ Mobile**: Comunicación mediante la API de Nodos de Android.
- **Mobile ↔ TV**: Comunicación implementada utilizando **Ktor** con un servidor **CIO**.



## ⚙️ Tecnologías Utilizadas

| Componente       | Tecnología / Herramienta |
|------------------|--------------------------|
| IDE              | Android Studio           |
| Lenguaje         | Kotlin                   |
| Comunicación Wear ↔ Mobile | Android Nodos API      |
| Comunicación Mobile ↔ TV | [Ktor](https://ktor.io/) con servidor CIO |
| Sensor de movimiento | SensorManager (Acelerómetro) |
| Emuladores usados | Wear OS, Mobile, Android TV |
| Arquitectura     | Multi-dispositivo, eventos simulados con botones |


## 📱 Dispositivos Simulados

La app ha sido probada y ejecutada en los siguientes emuladores de Android Studio:

- ⌚ **Wear OS Emulator**
- 📱 **Mobile Emulator**
- 📺 **Android TV Emulator**


## 🧪 Actividades Simuladas

Las siguientes actividades físicas se pueden simular desde la app para probar la respuesta del sensor:

- Caminar
- Correr
- Saltar
- Subir escaleras
- Agitar el dispositivo
- Simular caída
- Golpear el dispositivo

Cada actividad produce un valor de aceleración (`mag`) que se interpreta para determinar la gravedad del movimiento.


## 👥 Roles del Equipo

| Nombre                        | Rol                                | Descripción                                                                                      |
| ----------------------------- | ---------------------------------- | ------------------------------------------------------------------------------------------------ |
| Jorge Luis González Llamas    | Arquitecto de software y Tech Lead | Diseñó la arquitectura del sistema, coordinó el desarrollo, definió tecnologías y estructura.    |
| Francisco David Valencia Vega | Backend                            | Implementó el servidor Ktor con CIO para comunicación Mobile ↔ TV y manejó el protocolo backend. |
| Angel Salvador Martinez Rubio | Frontend                           | Construyó las interfaces en Mobile y Wear, enlazó botones a eventos del sensor.                  |
| Adrián Pereida Romero         | Tester                             | Realizó pruebas funcionales y de integración en los distintos emuladores y escenarios simulados. |



## 🏁 Clasificación de Impacto

```kotlin
val gravity = when {
    mag > 18 -> "fuerte"
    mag > 14 -> "medio"
    mag > 11 -> "leve"
    else -> ""
}
