# Simulador de movimiento parabólico

Proyecto final de Física — Ingeniería de Software
Universidad Cooperativa de Colombia, Campus Pasto

## Cómo ejecutarlo

Necesitas tener instalado Java 8 o superior. Para comprobarlo, abre una terminal y escribe `java -version`.

### Opción 1: desde la terminal

```
cd codigo
javac *.java
java Main
```

### Opción 2: desde un entorno de desarrollo

Abre la carpeta `codigo` como proyecto en NetBeans, IntelliJ IDEA o Eclipse y ejecuta la clase `Main`.

No hace falta instalar ninguna biblioteca externa: todo está hecho con Swing y AWT, que vienen incluidos en Java.

## Cómo usarlo

1. Elige el cuerpo celeste. Al seleccionarlo se ajustan automáticamente la gravedad y la densidad de la atmósfera.
2. Escribe la velocidad inicial, el ángulo y la altura inicial. Puedes usar coma o punto para los decimales.
3. Presiona **Simular**. El objeto empieza a moverse y va dejando dibujada su trayectoria.
4. Revisa el panel de resultados, que está siempre visible debajo de la zona de simulación.
5. Cambia los parámetros y vuelve a presionar **Simular** las veces que quieras.

### Botones

| Botón | Qué hace |
|---|---|
| Simular | Calcula todo y arranca la animación |
| Pausar / Reanudar / Repetir | Controla la animación |
| Reiniciar | Vuelve a los valores por defecto y limpia todo |
| Guardar para comparar | Deja la trayectoria actual dibujada de fondo para compararla con las siguientes |
| Borrar comparaciones | Quita las trayectorias guardadas |
| Exportar CSV | Guarda la tabla de datos en un archivo que abre en Excel |

### Pestañas

- **Escenario**: el lanzador, el objeto en movimiento, su vector velocidad descompuesto, la altura máxima y el alcance.
- **Altura vs distancia (y-x)**: la forma real de la parábola.
- **Distancia vs tiempo (x-t)**: sale una línea recta, porque en el eje x el movimiento es uniforme.
- **Altura vs tiempo (y-t)**: sale una parábola, porque en el eje y el movimiento es acelerado.
- **Tabla de datos**: posición y velocidad en tiempos exactos (cada 1 s por defecto; se puede cambiar a 0,1 / 0,25 / 0,5 / 2 o 5 s). Siempre agrega resaltadas la fila de la altura máxima y la del impacto con el suelo.

## Opciones adicionales

**Resistencia del aire.** Al activarla el programa resuelve el modelo con arrastre cuadrático usando Runge-Kutta de cuarto orden, y el panel de resultados muestra las dos columnas lado a lado con la diferencia porcentual. Los valores que vienen por defecto corresponden a una pelota de béisbol.

**Blanco.** Puedes poner un objetivo a cierta distancia y el programa te dice si le acertaste, con qué margen y por cuánto te pasaste o te quedaste corto.

## Estructura del código

| Archivo | Responsabilidad |
|---|---|
| `Main.java` | Punto de entrada |
| `MotorFisico.java` | Todas las ecuaciones físicas, numeradas en los comentarios |
| `ParametrosLanzamiento.java` | Datos de entrada |
| `PuntoTrayectoria.java` | Estado del proyectil en un instante |
| `ResultadoSimulacion.java` | Trayectoria completa y magnitudes derivadas |
| `VentanaSimulador.java` | Interfaz gráfica |
| `PanelEscenario.java` | Dibujo animado del lanzamiento |
| `PanelGrafica.java` | Las tres gráficas |
| `PanelTablaDatos.java` | Tabla de datos en tiempos exactos con filas destacadas |
| `ExportadorCSV.java` | Exportación de datos |
| `Planeta.java` | Gravedad y densidad de cada cuerpo celeste |
| `Paleta.java` | Colores de la interfaz |
| `Formato.java` | Formato y validación de números |

## Verificación

Casos comprobados a mano contra el programa (error del 0 %):

| Caso | Magnitud | Cálculo manual | Programa |
|---|---|---|---|
| v0 = 25 m/s, θ = 45°, y0 = 0 | Tiempo de vuelo | 3,604 s | 3,604 s |
| | Altura máxima | 15,928 m | 15,928 m |
| | Alcance | 63,710 m | 63,710 m |
| | Velocidad final | 25,000 m/s | 25,000 m/s |
| v0 = 10 m/s, θ = 0°, y0 = 20 m | Tiempo de vuelo | 2,019 s | 2,019 s |
| | Alcance | 20,193 m | 20,193 m |
| v0 = 25 m/s, θ = 45°, Luna | Alcance | 385,80 m | 385,80 m |
