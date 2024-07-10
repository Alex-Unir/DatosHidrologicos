# DatosHidrologicos

Primeros pasos
====================
## Datos necesarios
La información manejada en está BBDD puede ser descargada desde MITECO en el siguiente enlace:
https://www.miteco.gob.es/content/dam/miteco/es/agua/temas/evaluacion-de-los-recursos-hidricos/boletin-hidrologico/Historico-de-embalses/BD-Embalses.zip
## Docker
Configura un contenedor docker instalando "Docker Desktop" en tú PC y ejecutando los siguientes comandos sin "comillas":

	docker pull mysql
	docker run -p 3306:3306 --name %nombrequequieras% -e MYSQL_ROOT_PASSWORD=%tupassword% -d mysql:latest

## MySQL
Para crear la BBDD, desde la consola powershell o cmd, ejecuta cd %ruta del proytecto java\mysql% [ruta del proyecto Java], lanza el siguiente comando:
	docker cp .\embalses.sql <<containerId>>:/embalses.sql
Una vez tengamos copiado el fichero al contenedor, abrimos la consola cli del contenedor, abriendo docker desktop, y seleccionando los tres puntitos en el contenedor creado "Open in terminal". Ejecutamos

	mysql -u root -p < embalses.sql

Indica la contraseña que pusiste anteriormente y se creara la BBDD.

Ahora ya se puede ejecutar el código java.