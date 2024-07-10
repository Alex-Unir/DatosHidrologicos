# Herramienta PBI para la visualización personalizable de datos Hidrológicos

Este programa permite la descarga de datos procedentes de MITECO relativos al agua embalsada en España desde el inicio de los registro hasta la última semana completa (lunes de cada semana).
El código java modela los datos y los preconfigura para ingresarlos en la BBDD.
En caso de realizar cambios en los parametros a trabajar, se deben de editar las sentencias SQL de alta y baja de tablas.

## 1º Instalar Docker Desktop
Configura un contenedor docker instalando "Docker Desktop" en tú PC y ejecutando los siguientes comandos sin "comillas":

	docker pull mysql
	docker run -p 3306:3306 --name %nombrequequieras% -e MYSQL_ROOT_PASSWORD=%tupassword% -d mysql:latest

## 2º Montar la BBDD MySQL
Para crear la BBDD, desde la consola powershell o cmd, ejecuta cd %ruta del proytecto java\mysql% [ruta del proyecto Java], lanza el siguiente comando:
	docker cp .\embalses.sql <<containerId>>:/embalses.sql
Una vez tengamos copiado el fichero al contenedor, abrimos la consola cli del contenedor, abriendo docker desktop, y seleccionando los tres puntitos en el contenedor creado "Open in terminal". Ejecutamos

	mysql -u root -p < embalses.sql

Indica la contraseña que pusiste anteriormente y se creara la BBDD.

Ahora ya se puede ejecutar el código java.

NOTA: Se recomienda quitar la clave en texto plano del código java e incluirla como variable de entorno en la ejecución por seguridad.

## 3º Manejo de BI.
En la carpeta BI-Datos se encuentra el modelo Power Bi Desktop base, el cual trae la contraseña por defecto de instalación (reflejada en texto plano en el código).
Este modelo de datos es flexible y permite relacionar los datos de embalses con otros origenes de datos, como pueden ser coordenadas, pluviómetros, etc.