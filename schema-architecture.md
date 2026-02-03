Sección 1: Resumen de la arquitectura

Esta aplicación de Spring Boot utiliza tanto controladores MVC como REST. Se utilizan plantillas de Thymeleaf para los paneles de administración y de doctor, mientras que las API REST sirven a todos los demás módulos. La aplicación interactúa con dos bases de datos: MySQL (para datos de pacientes, doctores, citas y administración) y MongoDB (para recetas). Todos los controladores dirigen las solicitudes a través de una capa de servicio común, que a su vez delega en los repositorios apropiados. MySQL utiliza entidades JPA mientras que MongoDB utiliza modelos de documentos.

Sección 2: Flujo numerado de datos y control

1. Capa de Interfaz de Usuario
El sistema soporta múltiples tipos de usuarios y patrones de interacción. Los usuarios pueden acceder a la aplicación a través de:

Tableros web basados en Thymeleaf como AdminDashboard y DoctorDashboard. Estas son páginas HTML tradicionales renderizadas en el servidor y entregadas al navegador.
Clientes de API REST como aplicaciones móviles o módulos frontend (por ejemplo, Appointments, PatientDashboard y PatientRecord) que interactúan con el backend a través de HTTP y reciben respuestas en JSON.
Esta separación permite que el sistema soporte tanto vistas interactivas en el navegador como integraciones escalables basadas en API.

2. Capa del Controlador
Cuando un usuario interactúa con la aplicación (por ejemplo, haciendo clic en un botón o enviando un formulario), la solicitud se dirige a un controlador en el backend según la ruta de la URL y el método HTTP.

Las solicitudes para vistas renderizadas en el servidor son manejadas por Controladores de Thymeleaf, que devuelven plantillas .html que se llenarán con datos dinámicos y se renderizarán en el navegador.
Las solicitudes de los consumidores de API son manejadas por Controladores REST, que procesan la entrada, llaman a la lógica del backend y devuelven respuestas en formato JSON.
Estos controladores sirven como puntos de entrada a la lógica de la aplicación en el backend, aplicando la validación de solicitudes y coordinando el flujo de solicitud/respuesta.

3. Capa de Servicio
Todos los controladores delegan la lógica a la Capa de Servicio, que actúa como el corazón del sistema backend. Esta capa:

Aplica reglas de negocio y validaciones
Coordina flujos de trabajo entre múltiples entidades (por ejemplo, verificando la disponibilidad del doctor antes de programar una cita)
Asegura una clara separación entre la lógica del controlador y el acceso a datos
Al aislar la lógica de negocio aquí, la aplicación se vuelve más mantenible, testeable y más fácil de escalar.

4. Capa de Repositorio
La capa de servicio se comunica con la Capa de Repositorio para realizar operaciones de acceso a datos. Esta capa incluye dos tipos de repositorios:

Repositorios MySQL, que utilizan Spring Data JPA para gestionar datos relacionales estructurados como pacientes, doctores, citas y registros administrativos.
Repositorio MongoDB, que utiliza Spring Data MongoDB para gestionar registros basados en documentos como recetas.
Los repositorios abstraen la lógica de acceso a la base de datos y exponen una interfaz simple y declarativa para recuperar y persistir datos.

5. Acceso a la base de datos
Cada repositorio se conecta directamente con el motor de base de datos subyacente:

MySQL almacena todas las entidades centrales que se benefician de un esquema relacional normalizado y restricciones, como usuarios, roles y citas.
MongoDB almacena estructuras de datos flexibles y anidadas, como recetas, que pueden variar en formato y permiten una rápida evolución del esquema.
Esta configuración de doble base de datos aprovecha las fortalezas de ambos enfoques de almacenamiento de datos estructurados y no estructurados.

6. Vinculación de modelos
Una vez que se recuperan los datos de la base de datos, se mapean en clases de modelo de Java con las que la aplicación puede trabajar. Este proceso se conoce como vinculación de modelos.

En el caso de MySQL, los datos se convierten en entidades JPA, que representan filas en tablas relacionales y están anotadas con @Entity.
Para MongoDB, los datos se cargan en objetos de documento, típicamente anotados con @Document, que se mapean a estructuras BSON/JSON en colecciones.
Estas clases de modelo proporcionan una representación consistente y orientada a objetos de los datos a través de las capas de la aplicación.

7. Modelos de aplicación en uso
Finalmente, los modelos vinculados se utilizan en la capa de respuesta:

En flujos MVC, los modelos se pasan del controlador a las plantillas de Thymeleaf, donde se renderizan como HTML dinámico para el navegador.
En flujos REST, los mismos modelos (o DTOs transformados) se serializan en JSON y se envían de vuelta al cliente como parte de una respuesta HTTP.
Esto marca el final del ciclo de solicitud-respuesta, entregando ya sea una página web completa o datos de API estructurados, dependiendo del consumidor.
