package com.nexum.commons;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Contexto mínimo para los *IT (la librería no tiene aplicación propia). Entidades y repositorios se detectan
 * por el paquete de esta clase; el escaneo de componentes apunta a un paquete vacío para que las clases de la
 * librería solo lleguen por la autoconfiguración, como en una aplicación real.
 */
@SpringBootApplication(scanBasePackages = "com.nexum.commons.testapp")
public class CommonsTestApplication {
}
